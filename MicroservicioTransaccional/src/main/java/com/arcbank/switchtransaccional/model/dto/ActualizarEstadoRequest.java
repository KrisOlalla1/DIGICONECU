package com.arcbank.switchtransaccional.model.dto;

import lombok.Data;

@Data
public class ActualizarEstadoRequest {

    private String nuevoEstado;
    private String codigoRespuestaFinal;
}
