package com.payment.payment_processing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "\"IdempotenciaCache\"")
public class IdempotenciaCache {

    @Id
    @Column(name = "\"InstructionId\"")
    private UUID instructionId;

    @Column(name = "\"RespuestaJson\"", columnDefinition = "TEXT", nullable = false)
    private String respuestaJson;

    @Column(name = "\"FechaCreacion\"", nullable = false)
    private LocalDateTime fechaCreacion;

    public IdempotenciaCache() {
    }

    public IdempotenciaCache(UUID instructionId) {
        this.instructionId = instructionId;
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof IdempotenciaCache other))
            return false;
        return instructionId != null && instructionId.equals(other.instructionId);
    }

    @Override
    public int hashCode() {
        return instructionId == null ? 0 : instructionId.hashCode();
    }

    @Override
    public String toString() {
        return "IdempotenciaCache{" +
                "instructionId=" + instructionId +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}