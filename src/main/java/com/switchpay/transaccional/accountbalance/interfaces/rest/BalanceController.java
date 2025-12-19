package com.switchpay.transaccional.accountbalance.interfaces.rest;

import com.switchpay.transaccional.accountbalance.application.dto.*;
import com.switchpay.transaccional.accountbalance.application.service.BalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
