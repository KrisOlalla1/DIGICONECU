package com.transswitch.returnmanagementservice.dto.payment;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferGetResponse {
    private boolean success;
    private TransferData data;
    private Object error;
    private String timestamp;

    public TransferGetResponse() {
    }

    @Getter
    @Setter
    public static class TransferData {
        private String instructionId;
        private String estado;
        private BigDecimal monto;
        private String bancoOrigen;
        private String bancoDestino;
        private String cuentaOrigen;
        private String cuentaDestino;
        private String moneda;
        private String fechaCreacion;

        public TransferData() {
        }
    }
}
