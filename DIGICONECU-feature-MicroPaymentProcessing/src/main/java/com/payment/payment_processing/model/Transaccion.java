package com.payment.payment_processing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "\"Transacciones\"")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "\"Id\"")
    private UUID id;

    @Column(name = "\"InstructionId\"", unique = true, nullable = false)
    private UUID instructionId;

    @Column(name = "\"EndToEndId\"", length = 100)
    private String endToEndId;

    @Column(name = "\"BancoOrigenCodigo\"", length = 20, nullable = false)
    private String bancoOrigenCodigo;

    @Column(name = "\"BancoDestinoCodigo\"", length = 20, nullable = false)
    private String bancoDestinoCodigo;

    @Column(name = "\"Monto\"", precision = 18, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(name = "\"Moneda\"", length = 3, nullable = false)
    private String moneda = "USD";

    @Column(name = "\"CuentaOrigen\"", length = 50, nullable = false)
    private String cuentaOrigen;

    @Column(name = "\"CuentaDestino\"", length = 50, nullable = false)
    private String cuentaDestino;

    @Column(name = "\"Estado\"", length = 30, nullable = false)
    private String estado = "Recibida";

    @Column(name = "\"CodigoError\"", length = 10)
    private String codigoError;

    @Column(name = "\"MensajeError\"", length = 500)
    private String mensajeError;

    @Column(name = "\"FechaCreacion\"", nullable = false)
    private LocalDateTime fechaCreacion;

    public Transaccion() {
    }

    public Transaccion(UUID id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (instructionId == null) {
            instructionId = UUID.randomUUID();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Transaccion other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "Transaccion{" +
                "id=" + id +
                ", instructionId=" + instructionId +
                ", bancoOrigenCodigo='" + bancoOrigenCodigo + '\'' +
                ", bancoDestinoCodigo='" + bancoDestinoCodigo + '\'' +
                ", monto=" + monto +
                ", estado='" + estado + '\'' +
                '}';
    }
}