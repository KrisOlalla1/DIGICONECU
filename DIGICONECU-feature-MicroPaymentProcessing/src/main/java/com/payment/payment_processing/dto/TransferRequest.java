package com.payment.payment_processing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull(message = "instructionId es requerido")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "instructionId debe ser alfanumérico o UUID")
    private String instructionId;

    @NotNull(message = "endToEndId es requerido")
    private String endToEndId;

    @NotBlank(message = "bancoOrigen es requerido")
    private String bancoOrigen;

    @NotBlank(message = "cuentaOrigen es requerida")
    @Size(min = 6, max = 20, message = "cuentaOrigen debe tener entre 6 y 20 caracteres")
    private String cuentaOrigen;

    @NotBlank(message = "cuentaDestino es requerida")
    @Size(min = 6, max = 20, message = "cuentaDestino debe tener entre 6 y 20 caracteres")
    private String cuentaDestino;

    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.01", message = "monto debe ser mayor a 0")
    @Digits(integer = 15, fraction = 2, message = "monto inválido")
    private BigDecimal monto;

    @NotBlank(message = "moneda es requerida")
    @Pattern(regexp = "^(USD|EUR|PEN|COP|MXN)$", message = "moneda debe ser USD, EUR, PEN, COP o MXN")
    private String moneda;

    private String concepto;
}