package com.payment.payment_processing.integration.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompletarTransferenciaResponse {
    private boolean exito;
    private BigDecimal saldoOrigenActualizado;
    private BigDecimal saldoDestinoActualizado;
    private String mensaje;
}
