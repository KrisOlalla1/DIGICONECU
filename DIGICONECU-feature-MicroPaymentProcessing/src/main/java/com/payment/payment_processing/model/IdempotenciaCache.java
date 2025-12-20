package com.payment.payment_processing.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "IdempotenciaCache")
public class IdempotenciaCache {
    @Id
    @Column(length = 50)
    private String instructionId;

    @Column(columnDefinition = "TEXT")
    private String respuestaJson; // Guardamos la respuesta exacta que dimos

    private LocalDateTime fechaRegistro;
}