package com.digiconecu.network_management_service.exception;

import lombok.Getter;

@Getter
public class ExcepcionNegocio extends RuntimeException {
    private final String codigo; // Código estándar (ej: AC01, MS03)

    public ExcepcionNegocio(String mensaje, String codigo) {
        super(mensaje);
        this.codigo = codigo;
    }
}