package com.payment.payment_processing.integration.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CongelarFondosResponse {
    private boolean exito;
    private BigDecimal saldoCongelado;
    private String mensaje;
}
