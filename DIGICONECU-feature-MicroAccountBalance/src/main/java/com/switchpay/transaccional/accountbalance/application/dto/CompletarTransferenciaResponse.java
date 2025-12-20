package com.switchpay.transaccional.accountbalance.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletarTransferenciaResponse {
    private boolean exito;
    private String mensaje;
}
