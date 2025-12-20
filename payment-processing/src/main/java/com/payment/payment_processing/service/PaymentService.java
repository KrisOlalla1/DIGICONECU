package com.payment.payment_processing.service;

import com.payment.payment_processing.dto.TransferRequest;
import com.payment.payment_processing.dto.TransferResponse;
import com.payment.payment_processing.model.IdempotenciaCache;
import com.payment.payment_processing.model.Transaccion;
import com.payment.payment_processing.repository.IdempotenciaRepository;
import com.payment.payment_processing.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
            // En un caso real, devolveríamos el JSON guardado.
            // Por simplicidad, reconstruimos respuesta de éxito.
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

        try {
            // =============================================================
            // AQUÍ IRÍAN LAS LLAMADAS A LOS OTROS MICROSERVICIOS (RestClient)
            // =============================================================
            // 3. Network Management -> Resolver Banco
            String bancoDestinoSimulado = "GUAYAQUIL"; // Simulado

            // 4. Account Balance -> Verificar y Congelar
            // 5. Notification Service -> Webhook
            // 6. Account Balance -> Completar

            // Simulamos éxito directo para cumplir el MVP
            tx.setEstado("COMPLETADA");
            tx.setFechaActualizacion(LocalDateTime.now());
            txRepo.save(tx);

            // 7. Guardar Idempotencia
            IdempotenciaCache cache = new IdempotenciaCache();
            cache.setInstructionId(req.getInstructionId());
            cache.setRespuestaJson("JSON_PLACEHOLDER"); // Guardaríamos el json real
            cache.setFechaRegistro(LocalDateTime.now());
            idemRepo.save(cache);

            return construirRespuestaExito(req.getInstructionId(), "Completada", bancoDestinoSimulado);

        } catch (Exception e) {
            log.error("Error procesando pago", e);
            tx.setEstado("FALLIDA");
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
                            .fechaCreacion(tx.getFechaCreacion())
                            .build())
                    .build();
        } else {
            return TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder().code("404").message("No encontrada").build())
                    .build();
        }
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