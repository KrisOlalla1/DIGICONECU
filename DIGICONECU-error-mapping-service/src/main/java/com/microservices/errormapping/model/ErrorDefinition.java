package com.microservices.errormapping.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDefinition {
    private String bancoOrigen;
    private String codigoExterno;
    private String codigoInterno;
    private String mensaje;
}
