package com.transswitch.returnmanagementservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;
    private String timestamp;

    public ApiResponse() {
    }

    public static <T> ApiResponse<T> ok(T data, String timestamp) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        r.timestamp = timestamp;
        return r;
    }

    public static <T> ApiResponse<T> fail(String code, String message, String details, String timestamp) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        ApiError e = new ApiError();
        e.setCode(code);
        e.setMessage(message);
        e.setDetails(details);
        r.error = e;
        r.timestamp = timestamp;
        return r;
    }
}
