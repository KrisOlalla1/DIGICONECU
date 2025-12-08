package com.arcbank.switchtransaccional.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"Transaccion\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdInstruccion\"")
    private Integer idInstruccion;

    @Column(name = "\"EndToEnd\"", nullable = false, unique = true, length = 50)
    private String endToEnd;

    @Column(name = "\"TraceId\"", length = 50)
    private String traceId;

    @Column(name = "\"IdBancoOrigen\"", nullable = false)
    private Integer idBancoOrigen;

    @Column(name = "\"IdBancoDestino\"", nullable = false)
    private Integer idBancoDestino;

    @Column(name = "\"CuentaOrigen\"", nullable = false, length = 20)
    private String cuentaOrigen;

    @Column(name = "\"CuentaDestino\"", nullable = false, length = 20)
    private String cuentaDestino;

    @Column(name = "\"Monto\"", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "\"Mensaje\"", length = 200)
    private String mensaje;

    @Column(name = "\"EstadoActual\"", nullable = false, length = 20)
    private String estadoActual;

    @Column(name = "\"FechaCreacion\"", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "\"FechaProcesamiento\"")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "\"CodigoRespuestaFinal\"", length = 10)
    private String codigoRespuestaFinal;

    @Column(name = "\"MensajeRespuesta\"", length = 255)
    private String mensajeRespuesta;
}
