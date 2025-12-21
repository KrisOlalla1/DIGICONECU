package com.payment.payment_processing.integration.dto;

import lombok.Data;

@Data
public class EnrutamientoResponse {
    private String bancoCodigo;
    private String bancoNombre;
    private String puntoEnlace;
    private String estado;
}
