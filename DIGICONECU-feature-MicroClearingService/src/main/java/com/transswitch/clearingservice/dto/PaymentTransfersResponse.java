package com.transswitch.clearingservice.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentTransfersResponse {
    private boolean success;
    private PaymentTransfersData data;
    private Object error;
    private String timestamp;

    public PaymentTransfersResponse() {
    }

    @Getter
    @Setter
    public static class PaymentTransfersData {
        private List<PaymentTransferItem> transacciones;
        private Integer total;

        public PaymentTransfersData() {
        }
    }
}
