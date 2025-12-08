package com.arcbank.switchtransaccional.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransaccionRequest {

    @NotNull(message = "El campo Transaccion es obligatorio")
    @Valid
    @JsonProperty("Transaccion")
    private Transaccion transaccion;

    @Data
    public static class Transaccion {

        @JsonProperty("IdInstruccion")
        private Long idInstruccion;

        @JsonProperty("EndToEnd")
        private String endToEnd;

        @NotNull
        @JsonProperty("IdBancoOrigen")
        private Integer idBancoOrigen;

        @NotNull
        @JsonProperty("IdBancoDestino")
        private Integer idBancoDestino;

        @NotNull
        @JsonProperty("CuentaOrigen")
        private String cuentaOrigen;

        @NotNull
        @JsonProperty("CuentaDestino")
        private String cuentaDestino;

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @JsonProperty("Monto")
        private BigDecimal monto;

        @JsonProperty("Mensaje")
        private String mensaje;

        @NotNull
        @JsonProperty("EstadoActual")
        private String estadoActual;

        @NotNull
        @JsonProperty("FechaCreacion")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime fechaCreacion;

        @JsonProperty("CodigoRespuestaFinal")
        private String codigoRespuestaFinal;
    }
}
