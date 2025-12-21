package com.payment.payment_processing.integration.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VerificarSaldoResponse {
    private boolean aprobado;
    private BigDecimal saldoDisponible;
    private String mensaje;
    private String codigoError;
}
