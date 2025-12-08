package com.arcbank.switchtransaccional.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionBaseResponse {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("IdInstruccion")
    private Integer idInstruccion;

    @JsonProperty("TraceId")
    private String traceId;

    @JsonProperty("EstadoActual")
    private String estadoActual;

    @JsonProperty("Mensaje")
    private String mensaje;
}

//TransaccionResponse intacto por si Persona 2 lo usa en el enrutamiento o en otros endpoints (por ejemplo, el futuro GET de estado).