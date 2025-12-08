package com.arcbank.switchtransaccional.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaEstadoTransaccionResponse {

    @JsonProperty("IdInstruccion")
    private Integer idInstruccion;

    @JsonProperty("EndToEnd")
    private String endToEnd;

    @JsonProperty("TraceId")
    private String traceId;

    @JsonProperty("EstadoActual")
    private String estadoActual;

    @JsonProperty("CodigoRespuestaFinal")
    private String codigoRespuestaFinal;

    @JsonProperty("FechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime fechaCreacion;

    @JsonProperty("Monto")
    private BigDecimal monto;

    @JsonProperty("BancoOrigen")
    private String bancoOrigen;

    @JsonProperty("BancoDestino")
    private String bancoDestino;
}
