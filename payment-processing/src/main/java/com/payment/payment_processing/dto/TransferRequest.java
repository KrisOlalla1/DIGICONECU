package com.payment.payment_processing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull private String instructionId;
    @NotNull private String endToEndId;
    @NotNull private String bancoOrigen;
    @NotNull private String cuentaOrigen;
    @NotNull private String cuentaDestino;
    @NotNull private BigDecimal monto;
    @NotNull private String moneda;
    private String concepto;
}