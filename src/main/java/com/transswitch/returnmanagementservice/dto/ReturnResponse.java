package com.transswitch.returnmanagementservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnResponse {
    private String returnInstructionId;
    private String originalInstructionId;
    private String estado;
    private String timestamp;

    public ReturnResponse() {
    }
}
