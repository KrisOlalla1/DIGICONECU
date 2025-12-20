package com.switchpay.transaccional.accountbalance.application.service;

import com.switchpay.transaccional.accountbalance.application.dto.*;
import com.switchpay.transaccional.accountbalance.domain.entity.CuentaPrefondeo;
import com.switchpay.transaccional.accountbalance.domain.entity.OperacionPrefondeo;
import com.switchpay.transaccional.accountbalance.domain.enums.TipoOperacion;
import com.switchpay.transaccional.accountbalance.domain.repository.CuentaPrefondeoRepository;
import com.switchpay.transaccional.accountbalance.domain.repository.OperacionPrefondeoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

        private final CuentaPrefondeoRepository cuentaRepository;
        private final OperacionPrefondeoRepository operacionRepository;

        public VerificarSaldoResponse verificarSaldo(VerificarSaldoRequest request) {
                log.info("Verificando saldo para banco: {}, monto: {}", request.getBancoCodigo(), request.getMonto());
                return cuentaRepository.findByBancoCodigo(request.getBancoCodigo())
                                .map(cuenta -> {
                                        boolean aprobado = cuenta.getSaldoDisponible()
                                                        .compareTo(request.getMonto()) >= 0;
                                        return VerificarSaldoResponse.builder()
                                                        .aprobado(aprobado)
                                                        .saldoDisponible(cuenta.getSaldoDisponible())
                                                        .mensaje(aprobado ? "Saldo suficiente" : "Saldo insuficiente")
                                                        .codigoError(aprobado ? null : "AM04")
                                                        .build();
                                })
                                .orElse(VerificarSaldoResponse.builder()
                                                .aprobado(false)
                                                .saldoDisponible(BigDecimal.ZERO)
                                                .mensaje("Banco no encontrado")
                                                .codigoError("AC01")
                                                .build());
        }

        @Transactional
        public CongelarFondosResponse congelarFondos(CongelarFondosRequest request) {
                log.info("Congelando fondos para banco: {}, monto: {}, instructionId: {}",
                                request.getBancoCodigo(), request.getMonto(), request.getInstructionId());
                return cuentaRepository.findByBancoCodigo(request.getBancoCodigo())
                                .map(cuenta -> {
                                        if (cuenta.getSaldoDisponible().compareTo(request.getMonto()) < 0) {
                                                log.warn("Saldo insuficiente para congelar. Disponible: {}, Requerido: {}",
                                                                cuenta.getSaldoDisponible(), request.getMonto());
                                                return CongelarFondosResponse.builder()
                                                                .exito(false)
                                                                .saldoCongelado(cuenta.getSaldoCongelado())
                                                                .mensaje("Saldo insuficiente para congelar")
                                                                .build();
                                        }

                                        BigDecimal saldoAnterior = cuenta.getSaldoDisponible();
                                        cuenta.setSaldoDisponible(
                                                        cuenta.getSaldoDisponible().subtract(request.getMonto()));
                                        cuenta.setSaldoCongelado(cuenta.getSaldoCongelado().add(request.getMonto()));
                                        cuentaRepository.save(cuenta);

                                        registrarOperacion(cuenta, request.getInstructionId(), TipoOperacion.Congelar,
                                                        request.getMonto(), saldoAnterior, cuenta.getSaldoDisponible());

                                        log.info("Fondos congelados exitosamente. Nuevo saldo disponible: {}, congelado: {}",
                                                        cuenta.getSaldoDisponible(), cuenta.getSaldoCongelado());
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
                log.info("Completando transferencia: {} -> {}, monto: {}",
                                request.getBancoOrigen(), request.getBancoDestino(), request.getMonto());
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

                log.info("Transferencia completada exitosamente");
                return CompletarTransferenciaResponse.builder()
                                .exito(true)
                                .mensaje("Transferencia completada")
                                .build();
        }

        @Transactional
        public Map<String, Object> liberarFondos(LiberarFondosRequest request) {
                log.info("Liberando fondos para banco: {}, monto: {}", request.getBancoCodigo(), request.getMonto());
                Map<String, Object> response = new HashMap<>();

                var cuentaOpt = cuentaRepository.findByBancoCodigo(request.getBancoCodigo());
                if (cuentaOpt.isEmpty()) {
                        response.put("exito", false);
                        response.put("mensaje", "Banco no encontrado");
                        return response;
                }

                CuentaPrefondeo cuenta = cuentaOpt.get();
                if (cuenta.getSaldoCongelado().compareTo(request.getMonto()) < 0) {
                        response.put("exito", false);
                        response.put("mensaje", "Saldo congelado insuficiente para liberar");
                        return response;
                }

                BigDecimal saldoAnterior = cuenta.getSaldoDisponible();
                cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().add(request.getMonto()));
                cuenta.setSaldoCongelado(cuenta.getSaldoCongelado().subtract(request.getMonto()));
                cuentaRepository.save(cuenta);

                registrarOperacion(cuenta, request.getInstructionId(), TipoOperacion.Liberar,
                                request.getMonto(), saldoAnterior, cuenta.getSaldoDisponible());

                log.info("Fondos liberados exitosamente");
                response.put("exito", true);
                response.put("mensaje", "Fondos liberados exitosamente");
                response.put("saldoDisponible", cuenta.getSaldoDisponible());
                response.put("saldoCongelado", cuenta.getSaldoCongelado());
                return response;
        }

        @Transactional
        public Map<String, Object> recargarSaldo(RecargarSaldoRequest request) {
                log.info("Recargando saldo para banco: {}, monto: {}", request.getBancoCodigo(), request.getMonto());
                Map<String, Object> response = new HashMap<>();

                var cuentaOpt = cuentaRepository.findByBancoCodigo(request.getBancoCodigo());
                if (cuentaOpt.isEmpty()) {
                        response.put("exito", false);
                        response.put("mensaje", "Banco no encontrado");
                        return response;
                }

                CuentaPrefondeo cuenta = cuentaOpt.get();
                BigDecimal saldoAnterior = cuenta.getSaldoDisponible();
                cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().add(request.getMonto()));
                cuentaRepository.save(cuenta);

                registrarOperacion(cuenta, null, TipoOperacion.Recargar,
                                request.getMonto(), saldoAnterior, cuenta.getSaldoDisponible());

                log.info("Saldo recargado exitosamente. Nuevo saldo: {}", cuenta.getSaldoDisponible());
                response.put("exito", true);
                response.put("mensaje", "Saldo recargado exitosamente");
                response.put("saldoDisponible", cuenta.getSaldoDisponible());
                return response;
        }

        public Map<String, Object> consultarSaldo(String bancoCodigo) {
                Map<String, Object> response = new HashMap<>();
                var cuentaOpt = cuentaRepository.findByBancoCodigo(bancoCodigo);

                if (cuentaOpt.isEmpty()) {
                        response.put("encontrado", false);
                        response.put("mensaje", "Banco no encontrado");
                        return response;
                }

                CuentaPrefondeo cuenta = cuentaOpt.get();
                response.put("encontrado", true);
                response.put("bancoCodigo", cuenta.getBancoCodigo());
                response.put("saldoDisponible", cuenta.getSaldoDisponible());
                response.put("saldoCongelado", cuenta.getSaldoCongelado());
                response.put("saldoTotal", cuenta.getSaldoDisponible().add(cuenta.getSaldoCongelado()));
                response.put("moneda", cuenta.getMoneda());
                response.put("fechaActualizacion", cuenta.getFechaActualizacion());
                return response;
        }

        public List<Map<String, Object>> historialOperaciones(String bancoCodigo) {
                var cuentaOpt = cuentaRepository.findByBancoCodigo(bancoCodigo);
                if (cuentaOpt.isEmpty()) {
                        return List.of();
                }

                CuentaPrefondeo cuenta = cuentaOpt.get();
                List<OperacionPrefondeo> operaciones = operacionRepository
                                .findByCuentaIdOrderByFechaOperacionDesc(cuenta.getId());

                return operaciones.stream().map(op -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", op.getId());
                        map.put("tipoOperacion", op.getTipoOperacion().name());
                        map.put("monto", op.getMonto());
                        map.put("saldoAnterior", op.getSaldoAnterior());
                        map.put("saldoPosterior", op.getSaldoPosterior());
                        map.put("instructionId", op.getInstructionId());
                        map.put("fechaOperacion", op.getFechaOperacion());
                        return map;
                }).collect(Collectors.toList());
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
                log.debug("Operación registrada: tipo={}, monto={}, saldoAnterior={}, saldoPosterior={}",
                                tipo, monto, saldoAnterior, saldoPosterior);
        }
}
