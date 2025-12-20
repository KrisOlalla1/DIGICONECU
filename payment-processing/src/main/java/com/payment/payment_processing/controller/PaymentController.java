package com.payment.payment_processing.controller;

import com.payment.payment_processing.dto.TransferRequest;
import com.payment.payment_processing.dto.TransferResponse;
import com.payment.payment_processing.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/transfers")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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
}