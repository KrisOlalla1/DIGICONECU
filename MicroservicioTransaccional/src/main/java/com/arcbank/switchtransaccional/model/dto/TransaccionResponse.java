package com.arcbank.switchtransaccional.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuestas del API de transacciones.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
// (Opcional) solo para que el orden visual sea igual al que quieres
@JsonPropertyOrder({ "success", "IdInstruccion", "TraceId", "EstadoActual", "Mensaje" })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionResponse {

    // success
    @JsonProperty("success")
    private Boolean exitoso;

    // IdInstruccion
    @JsonProperty("IdInstruccion")
    private Integer idInstruccion;

    // TraceId
    @JsonProperty("TraceId")
    private String traceId;

    // EstadoActual
    @JsonProperty("EstadoActual")
    private String estadoActual;

    // Mensaje
    @JsonProperty("Mensaje")
    private String mensaje;

    // ---- CAMPOS EXTRA (quedarán en null y NO saldrán en el JSON) ----

    private String endToEnd;

    private Integer idBancoOrigen;

    private Integer idBancoDestino;

    private String cuentaOrigen;

    private String cuentaDestino;

    private BigDecimal monto;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime fechaProcesamiento;

    private String codigoRespuestaFinal;
}
