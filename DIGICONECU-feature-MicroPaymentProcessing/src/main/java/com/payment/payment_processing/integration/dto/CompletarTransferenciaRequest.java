package com.payment.payment_processing.integration.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CompletarTransferenciaRequest {
    private String bancoOrigen;
    private String bancoDestino;
    private BigDecimal monto;
    private UUID instructionId;
}
