package com.digiconecu.network_management_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrutamientoRespuestaDto {
    private String bancoCodigo;   // Ejemplo: "PICHINCHA" [cite: 107]
    private String bancoNombre;   // Ejemplo: "Banco Pichincha"
    private String puntoEnlace;   // El endpoint/URL del banco destino [cite: 92, 107]
    private String estado;        // Activo, Suspendido o Mantenimiento

    public EnrutamientoRespuestaDto() {
    }
}