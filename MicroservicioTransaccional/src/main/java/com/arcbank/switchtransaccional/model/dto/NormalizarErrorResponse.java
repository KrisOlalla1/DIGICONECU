package com.arcbank.switchtransaccional.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO solo para probar la Tarea 1.3 con Postman.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalizarErrorResponse {

    private String codigoRespuesta;
    private String descripcion;
}
