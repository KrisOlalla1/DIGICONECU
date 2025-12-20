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
    @Column(length = 50)
    private String instructionId;

    private String endToEndId;
    private String bancoOrigen;
    private String cuentaOrigen;
    private String cuentaDestino;

    @Column(precision = 19, scale = 2)
    private BigDecimal monto;

    private String moneda;
    private String concepto;

    // Estados: RECIBIDA, ENRUTADA, COMPLETADA, FALLIDA
    private String estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
}