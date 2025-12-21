package com.digiconecu.network_management_service.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "\"Enrutamiento\"")
public class Enrutamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "\"Id\"", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"BancoId\"", nullable = false)
    private Banco banco;

    @Column(name = "\"BinInicio\"", nullable = false, length = 6)
    private String binInicio;

    @Column(name = "\"BinFin\"", nullable = false, length = 6)
    private String binFin;

    @Column(name = "\"Activo\"")
    private Boolean activo;

    public Enrutamiento() {
    }

    public Enrutamiento(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Enrutamiento that = (Enrutamiento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Enrutamiento{" +
                "id=" + id +
                ", banco=" + (banco != null ? banco.getId() : "null") +
                ", binInicio='" + binInicio + '\'' +
                ", binFin='" + binFin + '\'' +
                ", activo=" + activo +
                '}';
    }
}