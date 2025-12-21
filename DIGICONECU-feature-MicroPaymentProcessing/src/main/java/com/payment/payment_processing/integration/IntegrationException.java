package com.payment.payment_processing.integration;

import lombok.Getter;

/**
 * Excepción para errores de integración con servicios externos
 */
@Getter
public class IntegrationException extends RuntimeException {
    private final String code;
    private final String message;

    public IntegrationException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public IntegrationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
