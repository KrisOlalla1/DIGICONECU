package com.transswitch.clearingservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClearingCycleResponse {
    private String fechaCiclo;
    private String horaCorte;
    private Integer totalTransacciones;
    private Object posicionesNetas;
    private String archivoLiquidacionUrl;

    public ClearingCycleResponse() {
    }
}
