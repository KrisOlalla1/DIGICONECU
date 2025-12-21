package com.digiconecu.network_management_service.exception;

public class RecursoNoEncontradoExcepcion extends ExcepcionNegocio {
    public RecursoNoEncontradoExcepcion(String mensaje) {
        super(mensaje, "AC01"); // AC01: Cuenta incorrecta o no existe
    }

    public RecursoNoEncontradoExcepcion(String codigo, String mensaje) {
        super(mensaje, codigo);
    }
}