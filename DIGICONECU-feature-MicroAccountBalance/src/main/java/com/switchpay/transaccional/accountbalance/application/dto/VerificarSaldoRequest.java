package com.switchpay.transaccional.accountbalance.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VerificarSaldoRequest {

    @NotBlank
    private String bancoCodigo;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monto;
}
