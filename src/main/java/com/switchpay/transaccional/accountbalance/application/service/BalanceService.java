package com.switchpay.transaccional.accountbalance.application.service;

import com.switchpay.transaccional.accountbalance.application.dto.*;
import com.switchpay.transaccional.accountbalance.domain.entity.CuentaPrefondeo;
import com.switchpay.transaccional.accountbalance.domain.entity.OperacionPrefondeo;
import com.switchpay.transaccional.accountbalance.domain.enums.TipoOperacion;
import com.switchpay.transaccional.accountbalance.domain.repository.CuentaPrefondeoRepository;
import com.switchpay.transaccional.accountbalance.domain.repository.OperacionPrefondeoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final CuentaPrefondeoRepository cuentaRepository;
    private final OperacionPrefondeoRepository operacionRepository;

    public VerificarSaldoResponse verificarSaldo(VerificarSaldoRequest request) {
        return cuentaRepository.findByBancoCodigo(request.getBancoCodigo())
                .map(cuenta -> {
                    boolean aprobado = cuenta.getSaldoDisponible().compareTo(request.getMonto()) >= 0;
                    return VerificarSaldoResponse.builder()
                            .aprobado(aprobado)
                            .saldoDisponible(cuenta.getSaldoDisponible())
                            .mensaje(aprobado ? "Saldo suficiente" : "Saldo insuficiente")
                            .build();
                })
                .orElse(VerificarSaldoResponse.builder()
                        .aprobado(false)
                        .saldoDisponible(BigDecimal.ZERO)
                        .mensaje("Banco no encontrado")
                        .build());
    }

    @Transactional
    public CongelarFondosResponse congelarFondos(CongelarFondosRequest request) {
        return cuentaRepository.findByBancoCodigo(request.getBancoCodigo())
                .map(cuenta -> {
                    if (cuenta.getSaldoDisponible().compareTo(request.getMonto()) < 0) {
                        return CongelarFondosResponse.builder()
                                .exito(false)
                                .saldoCongelado(cuenta.getSaldoCongelado())
                                .mensaje("Saldo insuficiente para congelar")
                                .build();
                    }

                    BigDecimal saldoAnterior = cuenta.getSaldoDisponible();
                    cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().subtract(request.getMonto()));
                    cuenta.setSaldoCongelado(cuenta.getSaldoCongelado().add(request.getMonto()));
                    cuentaRepository.save(cuenta);

                    registrarOperacion(cuenta, request.getInstructionId(), TipoOperacion.Congelar,
                            request.getMonto(), saldoAnterior, cuenta.getSaldoDisponible());

                    return CongelarFondosResponse.builder()
                            .exito(true)
                            .saldoCongelado(cuenta.getSaldoCongelado())
                            .mensaje("Fondos congelados exitosamente")
                            .build();
                })
                .orElse(CongelarFondosResponse.builder()
                        .exito(false)
                        .saldoCongelado(BigDecimal.ZERO)
                        .mensaje("Banco no encontrado")
                        .build());
    }

    @Transactional
    public CompletarTransferenciaResponse completarTransferencia(CompletarTransferenciaRequest request) {
        var cuentaOrigen = cuentaRepository.findByBancoCodigo(request.getBancoOrigen());
        var cuentaDestino = cuentaRepository.findByBancoCodigo(request.getBancoDestino());

        if (cuentaOrigen.isEmpty()) {
            return CompletarTransferenciaResponse.builder()
                    .exito(false)
                    .mensaje("Banco origen no encontrado")
                    .build();
        }

        if (cuentaDestino.isEmpty()) {
            return CompletarTransferenciaResponse.builder()
                    .exito(false)
                    .mensaje("Banco destino no encontrado")
                    .build();
        }

        CuentaPrefondeo origen = cuentaOrigen.get();
        CuentaPrefondeo destino = cuentaDestino.get();

        if (origen.getSaldoCongelado().compareTo(request.getMonto()) < 0) {
            return CompletarTransferenciaResponse.builder()
                    .exito(false)
                    .mensaje("Fondos congelados insuficientes en banco origen")
                    .build();
        }

        BigDecimal saldoAnteriorOrigen = origen.getSaldoCongelado();
        origen.setSaldoCongelado(origen.getSaldoCongelado().subtract(request.getMonto()));
        cuentaRepository.save(origen);

        registrarOperacion(origen, request.getInstructionId(), TipoOperacion.Debitar,
                request.getMonto(), saldoAnteriorOrigen, origen.getSaldoCongelado());

        BigDecimal saldoAnteriorDestino = destino.getSaldoDisponible();
        destino.setSaldoDisponible(destino.getSaldoDisponible().add(request.getMonto()));
        cuentaRepository.save(destino);

        registrarOperacion(destino, request.getInstructionId(), TipoOperacion.Acreditar,
                request.getMonto(), saldoAnteriorDestino, destino.getSaldoDisponible());

        return CompletarTransferenciaResponse.builder()
                .exito(true)
                .mensaje("Transferencia completada")
                .build();
    }

    private void registrarOperacion(CuentaPrefondeo cuenta, java.util.UUID instructionId,
            TipoOperacion tipo, BigDecimal monto,
            BigDecimal saldoAnterior, BigDecimal saldoPosterior) {
        OperacionPrefondeo operacion = OperacionPrefondeo.builder()
                .cuenta(cuenta)
                .instructionId(instructionId)
                .tipoOperacion(tipo)
                .monto(monto)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .build();
        operacionRepository.save(operacion);
    }
}
