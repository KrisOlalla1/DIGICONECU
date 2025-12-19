package com.microservices.errormapping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private String bancoOrigen;
    private String codigoExterno;
    private String codigoInterno;
    private String mensaje;
}
