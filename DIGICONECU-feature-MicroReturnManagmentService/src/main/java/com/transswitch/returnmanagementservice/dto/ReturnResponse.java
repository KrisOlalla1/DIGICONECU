package com.transswitch.returnmanagementservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReturnResponse {
    private String returnInstructionId;
    private String originalInstructionId;
    private String estado;
    private String motivo;
    private BigDecimal monto;
    private String bancoIniciador;
    private String timestamp;

    public ReturnResponse() {
    }
}
