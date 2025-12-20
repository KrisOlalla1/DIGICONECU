package com.payment.payment_processing.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Transacciones")
public class Transaccion {
    @Id
    @Column(name = "InstructionId", length = 50)
    private String instructionId;

    @Column(name = "EndToEndId")
    private String endToEndId;

    @Column(name = "BancoOrigen")
    private String bancoOrigen;

    @Column(name = "CuentaOrigen")
    private String cuentaOrigen;

    @Column(name = "BancoDestino")
    private String bancoDestino;

    @Column(name = "CuentaDestino")
    private String cuentaDestino;

    @Column(name = "Monto", precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "Moneda")
    private String moneda;

    @Column(name = "Concepto")
    private String concepto;

    // Estados: RECIBIDA, ENRUTADA, ESPERANDO_RESPUESTA, COMPLETADA, RECHAZADA,
    // FALLIDA, TIMEOUT, REVERTIDA
    @Column(name = "Estado")
    private String estado;

    @Column(name = "CodigoError")
    private String codigoError;

    @Column(name = "MensajeError")
    private String mensajeError;

    @Column(name = "FechaCreacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "FechaActualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}