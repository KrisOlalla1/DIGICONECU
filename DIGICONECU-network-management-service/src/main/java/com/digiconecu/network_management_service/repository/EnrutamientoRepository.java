package com.digiconecu.network_management_service.repository;

import com.digiconecu.network_management_service.model.Enrutamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrutamientoRepository extends JpaRepository<Enrutamiento, UUID> {

    // RF-02: Busca el rango que incluye el BIN de la cuenta
    // Solo considera rangos activos y bancos en estado 'Activo'
    @Query("SELECT e FROM Enrutamiento e " +
            "WHERE :bin >= e.binInicio AND :bin <= e.binFin " +
            "AND e.activo = true " +
            "AND e.banco.estado = 'Activo'")
    Optional<Enrutamiento> findByBinInRange(@Param("bin") String bin);

    // Verifica si existe superposición de rangos
    @Query("SELECT COUNT(e) > 0 FROM Enrutamiento e " +
            "WHERE (e.binInicio <= :binFin AND e.binFin >= :binInicio)")
    boolean existsOverlappingRange(@Param("binInicio") String binInicio, @Param("binFin") String binFin);

    // Buscar por código de banco
    @Query("SELECT e FROM Enrutamiento e WHERE e.banco.codigo = :bancoCodigo")
    List<Enrutamiento> findByBancoCodigo(@Param("bancoCodigo") String bancoCodigo);
}