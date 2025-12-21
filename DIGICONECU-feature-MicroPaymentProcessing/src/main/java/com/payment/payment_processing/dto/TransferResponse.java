package com.payment.payment_processing.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransferResponse {
    private boolean success;
    private DataBody data;
    private ErrorBody error;

    @Data
    @Builder
    public static class DataBody {
        private UUID instructionId;
        private String estado;
        private String bancoDestino;
        private LocalDateTime timestamp;
        private BigDecimal monto;
        private String bancoOrigen;
        private LocalDateTime fechaCreacion;
    }

    @Data
    @Builder
    public static class ErrorBody {
        private String code;
        private String message;
    }
}