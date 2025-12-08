package com.arcbank.switch.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad JPA para la tabla EntidadBancaria en PostgreSQL.
 * Contiene información de los bancos participantes en el switch.
 * PERSONA 2: Usa esta entidad para obtener endpoints de bancos.
 */
@Entity
@Table(name = "\"EntidadBancaria\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntidadBancariaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdBanco\"")
    private Integer idBanco;
    
    @Column(name = "\"Nombre\"", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "\"EndpointProduccion\"", length = 255)
    private String endpointProduccion;
    
    @Column(name = "\"Estado\"", length = 20)
    private String estado;
    
    @Column(name = "\"Saldo\"", precision = 15, scale = 2)
    private BigDecimal saldo;
}
