package com.payment.payment_processing.integration.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VerificarSaldoRequest {
    private String bancoCodigo;
    private BigDecimal monto;
}
