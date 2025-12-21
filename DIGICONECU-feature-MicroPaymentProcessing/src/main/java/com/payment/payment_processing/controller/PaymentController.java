package com.payment.payment_processing.controller;

import com.payment.payment_processing.dto.TransferRequest;
import com.payment.payment_processing.dto.TransferResponse;
import com.payment.payment_processing.service.PaymentServiceV2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/transfers")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceV2 paymentService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "payment-processing"));
    }

    /**
     * Métricas del Switch Transaccional
     * - Total de transacciones procesadas
     * - Latencia promedio
     * - TPS objetivo
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(paymentService.obtenerMetricas());
    }

    @PostMapping
    public ResponseEntity<TransferResponse> iniciarTransferencia(@RequestBody @Valid TransferRequest request) {
        TransferResponse response = paymentService.procesarTransferencia(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Si es timeout, retornar 504
            if (response.getError() != null && "504".equals(response.getError().getCode())) {
                return ResponseEntity.status(504).body(response);
            }
            return ResponseEntity.status(422).body(response);
        }
    }

    @GetMapping("/{instructionId}")
    public ResponseEntity<TransferResponse> consultarEstado(@PathVariable String instructionId) {
        try {
            UUID uuid = UUID.fromString(instructionId);
            TransferResponse response = paymentService.consultarEstado(uuid);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(response);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder()
                            .code("INVALID_UUID")
                            .message("instructionId no es un UUID válido")
                            .build())
                    .build());
        }
    }

    @GetMapping
    public ResponseEntity<List<TransferResponse>> listarTransacciones(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fecha) {
        List<TransferResponse> transacciones = paymentService.listarTransacciones(estado, fecha);
        return ResponseEntity.ok(transacciones);
    }
}