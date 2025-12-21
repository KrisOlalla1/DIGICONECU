package com.transswitch.returnmanagementservice.dto.payment;

import lombok.Data;

@Data
public class TransferCreateResponse {
    private boolean success;
    private DataBody data;
    private ErrorBody error;

    @Data
    public static class DataBody {
        private String instructionId;
        private String estado;
        private String bancoDestino;
        private String timestamp;
    }

    @Data
    public static class ErrorBody {
        private String code;
        private String message;
    }
}
