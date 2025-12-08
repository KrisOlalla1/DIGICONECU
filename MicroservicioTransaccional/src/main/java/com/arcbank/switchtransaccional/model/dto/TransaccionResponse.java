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


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "IdInstruccion", "TraceId", "EstadoActual", "Mensaje" })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionResponse {

    @JsonProperty("success")
    private Boolean exitoso;

    @JsonProperty("IdInstruccion")
    private Integer idInstruccion;

    @JsonProperty("TraceId")
    private String traceId;

    @JsonProperty("EstadoActual")
    private String estadoActual;

    @JsonProperty("Mensaje")
    private String mensaje;

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
