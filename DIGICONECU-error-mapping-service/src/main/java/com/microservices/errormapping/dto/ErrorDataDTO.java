package com.microservices.errormapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDataDTO {
    private String codigoISO;
    private String descripcion;
    private String mensajeEstandar;
}
