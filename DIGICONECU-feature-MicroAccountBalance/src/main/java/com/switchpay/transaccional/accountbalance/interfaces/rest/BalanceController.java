package com.switchpay.transaccional.accountbalance.interfaces.rest;

import com.switchpay.transaccional.accountbalance.application.dto.*;
import com.switchpay.transaccional.accountbalance.application.service.BalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @PostMapping("/verificar")
    public ResponseEntity<VerificarSaldoResponse> verificarSaldo(@Valid @RequestBody VerificarSaldoRequest request) {
        return ResponseEntity.ok(balanceService.verificarSaldo(request));
    }

    @PostMapping("/congelar")
    public ResponseEntity<CongelarFondosResponse> congelarFondos(@Valid @RequestBody CongelarFondosRequest request) {
        return ResponseEntity.ok(balanceService.congelarFondos(request));
    }

    @PostMapping("/completar")
    public ResponseEntity<CompletarTransferenciaResponse> completarTransferencia(
            @Valid @RequestBody CompletarTransferenciaRequest request) {
        return ResponseEntity.ok(balanceService.completarTransferencia(request));
    }

    @PostMapping("/liberar")
    public ResponseEntity<Map<String, Object>> liberarFondos(@Valid @RequestBody LiberarFondosRequest request) {
        return ResponseEntity.ok(balanceService.liberarFondos(request));
    }

    @PostMapping("/recargar")
    public ResponseEntity<Map<String, Object>> recargarSaldo(@Valid @RequestBody RecargarSaldoRequest request) {
        return ResponseEntity.ok(balanceService.recargarSaldo(request));
    }

    @GetMapping("/saldo/{bancoCodigo}")
    public ResponseEntity<Map<String, Object>> consultarSaldo(@PathVariable String bancoCodigo) {
        return ResponseEntity.ok(balanceService.consultarSaldo(bancoCodigo));
    }

    @GetMapping("/operaciones/{bancoCodigo}")
    public ResponseEntity<List<Map<String, Object>>> historialOperaciones(@PathVariable String bancoCodigo) {
        return ResponseEntity.ok(balanceService.historialOperaciones(bancoCodigo));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "account-balance-service",
                "timestamp", java.time.OffsetDateTime.now().toString()));
    }
}
