package com.payment.payment_processing.service;

import com.payment.payment_processing.dto.TransferRequest;
import com.payment.payment_processing.dto.TransferResponse;
import com.payment.payment_processing.model.IdempotenciaCache;
import com.payment.payment_processing.model.Transaccion;
import com.payment.payment_processing.repository.IdempotenciaRepository;
import com.payment.payment_processing.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransaccionRepository txRepo;
    private final IdempotenciaRepository idemRepo;

    @Transactional
    public TransferResponse procesarTransferencia(TransferRequest req) {
        // 1. Validar Idempotencia
        if (idemRepo.existsById(req.getInstructionId())) {
            log.info("Intento duplicado para TX: {}", req.getInstructionId());
            Optional<Transaccion> txExistente = txRepo.findById(req.getInstructionId());
            if (txExistente.isPresent()) {
                Transaccion tx = txExistente.get();
                return construirRespuestaExito(tx.getInstructionId(), tx.getEstado(), tx.getBancoDestino());
            }
            return construirRespuestaExito(req.getInstructionId(), "Completada", "CACHE-HIT");
        }

        // 2. Guardar estado inicial (RECIBIDA)
        Transaccion tx = new Transaccion();
        tx.setInstructionId(req.getInstructionId());
        tx.setEndToEndId(req.getEndToEndId());
        tx.setBancoOrigen(req.getBancoOrigen());
        tx.setCuentaOrigen(req.getCuentaOrigen());
        tx.setCuentaDestino(req.getCuentaDestino());
        tx.setMonto(req.getMonto());
        tx.setMoneda(req.getMoneda());
        tx.setConcepto(req.getConcepto());
        tx.setEstado("RECIBIDA");
        txRepo.save(tx);
        log.info("TX {} creada con estado RECIBIDA", req.getInstructionId());

        try {
            // 3. Simular enrutamiento - Network Management Service
            tx.setEstado("ENRUTADA");
            String bancoDestinoSimulado = resolverBancoDestino(req.getCuentaDestino());
            tx.setBancoDestino(bancoDestinoSimulado);
            txRepo.save(tx);
            log.info("TX {} enrutada a banco {}", req.getInstructionId(), bancoDestinoSimulado);

            // 4. Simular verificación y congelamiento - Account Balance Service
            tx.setEstado("ESPERANDO_RESPUESTA");
            txRepo.save(tx);
            log.info("TX {} en estado ESPERANDO_RESPUESTA", req.getInstructionId());

            // 5. Simular envío al banco destino y respuesta exitosa
            tx.setEstado("COMPLETADA");
            txRepo.save(tx);
            log.info("TX {} COMPLETADA exitosamente", req.getInstructionId());

            // 6. Guardar Idempotencia
            IdempotenciaCache cache = new IdempotenciaCache();
            cache.setInstructionId(req.getInstructionId());
            cache.setRespuestaJson("{\"success\":true,\"estado\":\"Completada\"}");
            cache.setFechaRegistro(LocalDateTime.now());
            idemRepo.save(cache);

            return construirRespuestaExito(req.getInstructionId(), "Completada", bancoDestinoSimulado);

        } catch (Exception e) {
            log.error("Error procesando pago TX: {}", req.getInstructionId(), e);
            tx.setEstado("FALLIDA");
            tx.setCodigoError("MS03");
            tx.setMensajeError(e.getMessage());
            txRepo.save(tx);
            return TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder()
                            .code("MS03")
                            .message("Error interno procesando pago")
                            .build())
                    .build();
        }
    }

    public TransferResponse consultarEstado(String instructionId) {
        Optional<Transaccion> txOpt = txRepo.findById(instructionId);
        if (txOpt.isPresent()) {
            Transaccion tx = txOpt.get();
            return TransferResponse.builder()
                    .success(true)
                    .data(TransferResponse.DataBody.builder()
                            .instructionId(tx.getInstructionId())
                            .estado(tx.getEstado())
                            .monto(tx.getMonto())
                            .bancoOrigen(tx.getBancoOrigen())
                            .bancoDestino(tx.getBancoDestino())
                            .fechaCreacion(tx.getFechaCreacion())
                            .build())
                    .build();
        } else {
            return TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder().code("404").message("Transacción no encontrada")
                            .build())
                    .build();
        }
    }

    public List<TransferResponse> listarTransacciones(String estado, String fecha) {
        List<Transaccion> transacciones;

        if (estado != null && fecha != null) {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            transacciones = txRepo.findByEstadoAndFecha(estado, fechaLocal);
        } else if (estado != null) {
            transacciones = txRepo.findByEstado(estado);
        } else if (fecha != null) {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            transacciones = txRepo.findByFecha(fechaLocal);
        } else {
            transacciones = txRepo.findAll();
        }

        return transacciones.stream()
                .map(tx -> TransferResponse.builder()
                        .success(true)
                        .data(TransferResponse.DataBody.builder()
                                .instructionId(tx.getInstructionId())
                                .estado(tx.getEstado())
                                .monto(tx.getMonto())
                                .bancoOrigen(tx.getBancoOrigen())
                                .bancoDestino(tx.getBancoDestino())
                                .fechaCreacion(tx.getFechaCreacion())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Limpia registros de idempotencia mayores a 24 horas.
     * Se ejecuta cada hora.
     */
    @Scheduled(fixedRate = 3600000) // Cada hora
    @Transactional
    public void limpiarCacheIdempotencia() {
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        int eliminados = idemRepo.deleteByFechaRegistroBefore(limite);
        if (eliminados > 0) {
            log.info("Limpieza de idempotencia: {} registros eliminados", eliminados);
        }
    }

    private String resolverBancoDestino(String cuentaDestino) {
        // Simulación: extraer BIN y mapear a banco
        if (cuentaDestino.startsWith("100"))
            return "PICHINCHA";
        if (cuentaDestino.startsWith("250"))
            return "GUAYAQUIL";
        if (cuentaDestino.startsWith("300"))
            return "PACIFICO";
        return "BANCO_DESTINO";
    }

    private TransferResponse construirRespuestaExito(String id, String estado, String bancoDest) {
        return TransferResponse.builder()
                .success(true)
                .data(TransferResponse.DataBody.builder()
                        .instructionId(id)
                        .estado(estado)
                        .bancoDestino(bancoDest)
                        .timestamp(LocalDateTime.now())
                        .build())
                .build();
    }
}