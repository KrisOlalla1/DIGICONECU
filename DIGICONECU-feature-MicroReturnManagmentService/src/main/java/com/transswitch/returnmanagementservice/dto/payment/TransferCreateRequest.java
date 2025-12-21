package com.transswitch.returnmanagementservice.dto.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferCreateRequest {
    private String instructionId;
    private String endToEndId;
    private String bancoOrigen;
    private String cuentaOrigen;
    private String cuentaDestino;
    private BigDecimal monto;
    private String moneda;
    private String concepto;
}
