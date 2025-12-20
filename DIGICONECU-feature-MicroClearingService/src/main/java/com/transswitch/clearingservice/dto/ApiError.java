package com.transswitch.clearingservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiError {
    private String code;
    private String message;
    private String details;

    public ApiError() {
    }
}
