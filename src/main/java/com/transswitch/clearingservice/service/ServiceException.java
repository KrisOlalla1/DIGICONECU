package com.transswitch.clearingservice.service;

public class ServiceException extends RuntimeException {

    private final String httpCode;
    private final String code;
    private final String details;

    public ServiceException(String httpCode, String code, String details) {
        super(code);
        this.httpCode = httpCode;
        this.code = code;
        this.details = details;
    }

    public String getHttpCode() {
        return httpCode;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }
}
