package com.transswitch.clearingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "\"CiclosCompensacion\"")
@Getter
@Setter
public class CiclosCompensacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "\"Id\"", nullable = false)
    private UUID id;

    @Column(name = "\"FechaCiclo\"", nullable = false, unique = true)
    private LocalDate fechaCiclo;

    @Column(name = "\"HoraCorte\"", nullable = false)
    private LocalDateTime horaCorte;

    @Column(name = "\"TotalTransacciones\"", nullable = false)
    private Integer totalTransacciones;

    @Column(name = "\"PosicionesNetas\"", nullable = false, columnDefinition = "jsonb")
    private String posicionesNetas;

    @Column(name = "\"Estado\"", nullable = false, length = 20)
    private String estado;

    @Column(name = "\"ArchivoLiquidacionUrl\"", length = 500)
    private String archivoLiquidacionUrl;

    @Lob
    @Column(name = "\"ArchivoXmlContenido\"", columnDefinition = "TEXT")
    private String archivoXmlContenido;

    @Lob
    @Column(name = "\"ArchivoCsvContenido\"", columnDefinition = "TEXT")
    private String archivoCsvContenido;

    public CiclosCompensacion() {
    }

    public CiclosCompensacion(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CiclosCompensacion other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "CiclosCompensacion{" +
                "id=" + id +
                ", fechaCiclo=" + fechaCiclo +
                ", horaCorte=" + horaCorte +
                ", totalTransacciones=" + totalTransacciones +
                ", estado='" + estado + '\'' +
                ", archivoLiquidacionUrl='" + archivoLiquidacionUrl + '\'' +
                '}';
    }
}
