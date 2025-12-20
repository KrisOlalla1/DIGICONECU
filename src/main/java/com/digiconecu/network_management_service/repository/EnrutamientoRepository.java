package com.digiconecu.network_management_service.repository;

import com.digiconecu.network_management_service.model.Enrutamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrutamientoRepository extends JpaRepository<Enrutamiento, UUID> {

    // RF-02: Busca el rango que incluye el BIN de la cuenta
    // Solo considera rangos activos y bancos en estado 'Activo' [cite: 162, 163]
    @Query("SELECT e FROM Enrutamiento e " +
            "WHERE :bin >= e.binInicio AND :bin <= e.binFin " +
            "AND e.activo = true " +
            "AND e.banco.estado = 'Activo'")
    Optional<Enrutamiento> findByBinInRange(@Param("bin") String bin);
}