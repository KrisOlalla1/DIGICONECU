package com.switchpay.transaccional.accountbalance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"CuentasPrefondeo\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaPrefondeo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "\"Id\"")
    private UUID id;

    @Column(name = "\"BancoCodigo\"", unique = true, nullable = false, length = 20)
    private String bancoCodigo;

    @Column(name = "\"SaldoDisponible\"", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(name = "\"SaldoCongelado\"", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoCongelado;

    @Column(name = "\"Moneda\"", nullable = false, length = 3)
    private String moneda;

    @Column(name = "\"FechaActualizacion\"")
    private LocalDateTime fechaActualizacion;

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
