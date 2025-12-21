package com.nexus.ms_transacciones.controller;

import com.nexus.ms_transacciones.dto.SwitchTransaccionWrapper;
import com.nexus.ms_transacciones.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/switch")
@RequiredArgsConstructor
@Tag(name = "Switch Webhook")
public class TransaccionInterbancariaController {

    private final TransaccionService service;

    @Operation(summary = "Recibir pago externo")
    @PostMapping("/callback")
    public ResponseEntity<Void> recibir(@Valid @RequestBody SwitchTransaccionWrapper wrapper) {
        service.procesarPagoEntrante(wrapper.getTransaccion());
        return ResponseEntity.ok().build();
    }
}