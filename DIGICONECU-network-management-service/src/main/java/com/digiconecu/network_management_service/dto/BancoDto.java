package com.digiconecu.network_management_service.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class BancoDto {
    private UUID id;
    private String codigo;
    private String nombre;
    private String puntoEnlace; // Mapeado desde 'endpoint' en la base de datos
    private String estado;

    public BancoDto() {
    }
}