package com.transswitch.clearingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClearingExecuteRequest {

    @NotBlank
    private String fechaCiclo;

    public ClearingExecuteRequest() {
    }
}
