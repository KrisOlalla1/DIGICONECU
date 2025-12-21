//ubi: src/main/java/com/ecusol/ventanilla/dto/CuentaResumenDTO.java
package com.ecusol.ventanilla.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CuentaResumenDTO {
    private Integer cuentaId;
    private String numeroCuenta;
    private BigDecimal saldo;
    private String estado;
    private Integer tipoCuentaId;
    
    // Helper visual para el front
    public String getTipo() {
        return tipoCuentaId == 1 ? "Ahorros" : "Corriente";
    }
}