package com.transswitch.clearingservice.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentTransferItem {
    private String bancoOrigen;
    private String bancoDestino;
    private BigDecimal monto;
    private String estado;

    public PaymentTransferItem() {
    }
}
