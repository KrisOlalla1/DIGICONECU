package com.transswitch.returnmanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnCreateRequest {

    @NotBlank
    private String returnInstructionId;

    @NotBlank
    private String originalInstructionId;

    @NotBlank
    private String bancoIniciador;

    @NotBlank
    private String motivo;

    @NotNull
    private BigDecimal monto;

    public ReturnCreateRequest() {
    }
}
