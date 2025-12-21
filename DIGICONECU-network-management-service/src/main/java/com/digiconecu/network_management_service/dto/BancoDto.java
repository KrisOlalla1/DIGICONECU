package com.digiconecu.network_management_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class BancoDto {
    private UUID id;
    private String codigo;
    private String nombre;

    @JsonAlias({ "puntoEnlace", "endpoint" })
    private String endpoint;

    private String estado;

    public BancoDto() {
    }
}