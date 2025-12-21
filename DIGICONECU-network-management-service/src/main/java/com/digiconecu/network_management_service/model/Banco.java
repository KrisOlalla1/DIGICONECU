package com.digiconecu.network_management_service.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "\"Bancos\"")
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "\"Id\"", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "\"Codigo\"", unique = true, nullable = false, length = 20)
    private String codigo;

    @Column(name = "\"Nombre\"", nullable = false, length = 255)
    private String nombre;

    @Column(name = "\"Endpoint\"", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "\"Estado\"", nullable = false, length = 20)
    private String estado;

    @Column(name = "\"FallosConsecutivos\"")
    private Integer fallosConsecutivos = 0;

    @Column(name = "\"UltimoFallo\"")
    private OffsetDateTime ultimoFallo;

    @Column(name = "\"UltimoHealthCheck\"")
    private OffsetDateTime ultimoHealthCheck;

    @Column(name = "\"LatenciaPromedioMs\"")
    private Long latenciaPromedioMs = 0L;

    @Column(name = "\"EstadoCircuito\"", length = 20)
    private String estadoCircuito = "CLOSED";

    @Column(name = "\"CircuitoAbiertoDesde\"")
    private OffsetDateTime circuitoAbiertoDesde;

    public Banco() {
    }

    public Banco(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Banco banco = (Banco) o;
        return Objects.equals(id, banco.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Banco{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", estado='" + estado + '\'' +
                ", estadoCircuito='" + estadoCircuito + '\'' +
                ", fallosConsecutivos=" + fallosConsecutivos +
                '}';
    }
}