package com.arcbank.switchtransaccional.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalizarErrorResponse {

    private String codigoRespuesta;
    private String descripcion;
}
