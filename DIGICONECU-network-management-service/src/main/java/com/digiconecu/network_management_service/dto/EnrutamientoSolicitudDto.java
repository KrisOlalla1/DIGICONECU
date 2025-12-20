package com.digiconecu.network_management_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrutamientoSolicitudDto {
    // Cuenta destino (de aquí extraeremos el BIN de 6 dígitos)
    private String cuentaDestino;

    public EnrutamientoSolicitudDto() {
    }
}