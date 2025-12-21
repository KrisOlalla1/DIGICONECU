package com.digiconecu.network_management_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionNegocio(ExcepcionNegocio ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("exito", false);
        respuesta.put("error", Map.of(
                "codigo", ex.getCodigo(),
                "mensaje", ex.getMessage()
        ));

        // AC01 -> 404 Not Found, otros -> 422 Unprocessable Entity [cite: 321]
        HttpStatus estado = ex.getCodigo().equals("AC01") ? HttpStatus.NOT_FOUND : HttpStatus.UNPROCESSABLE_ENTITY;
        return new ResponseEntity<>(respuesta, estado);
    }
}