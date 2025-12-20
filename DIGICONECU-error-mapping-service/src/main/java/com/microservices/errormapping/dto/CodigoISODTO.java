package com.microservices.errormapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoISODTO {
    private String codigo;
    private String descripcion;
    private String mensaje;
}
