package com.switchpay.transaccional.accountbalance.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificarSaldoResponse {
    private boolean aprobado;
    private BigDecimal saldoDisponible;
    private String mensaje;
    private String codigoError;
}
