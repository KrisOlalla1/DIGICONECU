package com.arcbank.switch.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para recibir transacciones en el endpoint REST.
 * Estructura basada en el formato JSON proporcionado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionRequest {
    
    @NotNull(message = "El campo Transaccion es obligatorio")
    @jakarta.validation.Valid
    private Transaccion Transaccion;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaccion {
        
        @NotBlank(message = "EndToEnd es obligatorio")
        @Size(min = 5, max = 50, message = "EndToEnd debe tener entre 5 y 50 caracteres")
        private String EndToEnd;
        
        @NotNull(message = "IdBancoOrigen es obligatorio")
        @Positive(message = "IdBancoOrigen debe ser positivo")
        private Integer IdBancoOrigen;
        
        @NotNull(message = "IdBancoDestino es obligatorio")
        @Positive(message = "IdBancoDestino debe ser positivo")
        private Integer IdBancoDestino;
        
        @NotBlank(message = "CuentaOrigen es obligatoria")
        @Size(min = 10, max = 20, message = "CuentaOrigen debe tener entre 10 y 20 caracteres")
        private String CuentaOrigen;
        
        @NotBlank(message = "CuentaDestino es obligatoria")
        @Size(min = 10, max = 20, message = "CuentaDestino debe tener entre 10 y 20 caracteres")
        private String CuentaDestino;
        
        @NotNull(message = "Monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @DecimalMax(value = "999999.99", message = "El monto no puede exceder 999999.99")
        private BigDecimal Monto;
        
        @Size(max = 200, message = "El mensaje no puede exceder 200 caracteres")
        private String Mensaje;
        
        @NotBlank(message = "EstadoActual es obligatorio")
        private String EstadoActual;
        
        @NotNull(message = "FechaCreacion es obligatoria")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime FechaCreacion;
    }
}
