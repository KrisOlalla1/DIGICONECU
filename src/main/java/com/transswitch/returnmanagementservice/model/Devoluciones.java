package com.transswitch.returnmanagementservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Devoluciones")
@Getter
@Setter
public class Devoluciones {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", nullable = false)
    private UUID id;

    @Column(name = "ReturnInstructionId", nullable = false, unique = true)
    private UUID returnInstructionId;

    @Column(name = "TransaccionOriginalInstructionId", nullable = false)
    private UUID transaccionOriginalInstructionId;

    @Column(name = "BancoIniciadorCodigo", nullable = false, length = 20)
    private String bancoIniciadorCodigo;

    @Column(name = "Motivo", nullable = false, length = 10)
    private String motivo;

    @Column(name = "Monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(name = "Estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "FechaCreacion")
    private LocalDateTime fechaCreacion;

    public Devoluciones() {
    }

    public Devoluciones(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Devoluciones other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "Devoluciones{" +
                "id=" + id +
                ", returnInstructionId=" + returnInstructionId +
                ", transaccionOriginalInstructionId=" + transaccionOriginalInstructionId +
                ", bancoIniciadorCodigo='" + bancoIniciadorCodigo + '\'' +
                ", motivo='" + motivo + '\'' +
                ", monto=" + monto +
                ", estado='" + estado + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
