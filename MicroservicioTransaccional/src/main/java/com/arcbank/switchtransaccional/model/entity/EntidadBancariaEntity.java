package com.arcbank.switchtransaccional.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "\"EntidadBancaria\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntidadBancariaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdBanco\"")
    private Integer idBanco;

    @Column(name = "\"Nombre\"", nullable = false, length = 100)
    private String nombre;

    @Column(name = "\"EndpointProduccion\"", length = 255)
    private String endpointProduccion;

    @Column(name = "\"Estado\"", nullable = false, length = 20)
    private String estado;

    @Column(name = "\"Saldo\"")
    private BigDecimal saldo;
}
