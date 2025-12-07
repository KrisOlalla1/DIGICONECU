package com.arcbank.switch.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuestas del API de transacciones.
 * Usado por ambas personas para retornar resultados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionResponse {
    
    private Integer idInstruccion;
    
    private String endToEnd;
    
    private String traceId;
    
    private Integer idBancoOrigen;
    
    private Integer idBancoDestino;
    
    private String cuentaOrigen;
    
    private String cuentaDestino;
    
    private BigDecimal monto;
    
    private String mensaje;
    
    private String estadoActual;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime fechaCreacion;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime fechaProcesamiento;
    
    private String codigoRespuestaFinal;
    
    private String mensajeRespuesta;
    
    /**
     * Indica si la transacción fue procesada exitosamente.
     */
    private Boolean exitoso;
}
