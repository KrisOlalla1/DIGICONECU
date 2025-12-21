package com.switchpay.transaccional.accountbalance.domain.entity;

import com.switchpay.transaccional.accountbalance.domain.enums.TipoOperacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "\"OperacionesPrefondeo\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperacionPrefondeo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "\"Id\"")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"CuentaId\"", nullable = false)
    private CuentaPrefondeo cuenta;

    @Column(name = "\"InstructionId\"")
    private UUID instructionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"TipoOperacion\"", nullable = false, length = 20)
    private TipoOperacion tipoOperacion;

    @Column(name = "\"Monto\"", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(name = "\"SaldoAnterior\"", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "\"SaldoPosterior\"", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "\"FechaOperacion\"")
    private LocalDateTime fechaOperacion;

    @PrePersist
    public void prePersist() {
        this.fechaOperacion = LocalDateTime.now();
    }
}
