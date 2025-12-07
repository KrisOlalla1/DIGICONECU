package com.arcbank.switch.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla Transaccion en PostgreSQL.
 * PERSONA 1: Maneja la persistencia de esta entidad.
 * PERSONA 2: Lee esta entidad para obtener información de bancos y enrutamiento.
 */
@Entity
@Table(name = "\"Transaccion\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"IdInstruccion\"")
    private Integer idInstruccion;
    
    @Column(name = "\"EndToEnd\"", unique = true, nullable = false, length = 50)
    private String endToEnd;
    
    @Column(name = "\"TraceId\"", length = 50)
    private String traceId;
    
    @Column(name = "\"IdBancoOrigen\"", nullable = false)
    private Integer idBancoOrigen;
    
    @Column(name = "\"IdBancoDestino\"", nullable = false)
    private Integer idBancoDestino;
    
    @Column(name = "\"CuentaOrigen\"", nullable = false, length = 20)
    private String cuentaOrigen;
    
    @Column(name = "\"CuentaDestino\"", nullable = false, length = 20)
    private String cuentaDestino;
    
    @Column(name = "\"Monto\"", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;
    
    @Column(name = "\"Mensaje\"", length = 200)
    private String mensaje;
    
    @Column(name = "\"EstadoActual\"", nullable = false, length = 20)
    private String estadoActual;
    
    @Column(name = "\"FechaCreacion\"", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "\"CodigoRespuestaFinal\"", length = 10)
    private String codigoRespuestaFinal;
    
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estadoActual == null) {
            estadoActual = "Enviado";
        }
    }
}
