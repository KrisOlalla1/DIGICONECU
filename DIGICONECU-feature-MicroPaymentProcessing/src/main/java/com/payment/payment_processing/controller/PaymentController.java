package com.payment.payment_processing.controller;

import com.payment.payment_processing.dto.TransferRequest;
import com.payment.payment_processing.dto.TransferResponse;
import com.payment.payment_processing.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/transfers")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "payment-processing"));
    }

    @PostMapping
    public ResponseEntity<TransferResponse> iniciarTransferencia(@RequestBody @Valid TransferRequest request) {
        TransferResponse response = paymentService.procesarTransferencia(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(422).body(response);
        }
    }

    @GetMapping("/{instructionId}")
    public ResponseEntity<TransferResponse> consultarEstado(@PathVariable String instructionId) {
        TransferResponse response = paymentService.consultarEstado(instructionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(response);
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