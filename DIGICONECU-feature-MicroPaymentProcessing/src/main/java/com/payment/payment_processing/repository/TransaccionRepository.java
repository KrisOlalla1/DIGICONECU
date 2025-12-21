package com.payment.payment_processing.repository;

import com.payment.payment_processing.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, UUID> {

    Optional<Transaccion> findByInstructionId(UUID instructionId);

    List<Transaccion> findByEstado(String estado);

    @Query("SELECT t FROM Transaccion t WHERE CAST(t.fechaCreacion AS LocalDate) = :fecha")
    List<Transaccion> findByFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT t FROM Transaccion t WHERE t.estado = :estado AND CAST(t.fechaCreacion AS LocalDate) = :fecha")
    List<Transaccion> findByEstadoAndFecha(@Param("estado") String estado, @Param("fecha") LocalDate fecha);

    List<Transaccion> findByBancoOrigenCodigo(String bancoOrigenCodigo);

    List<Transaccion> findByBancoDestinoCodigo(String bancoDestinoCodigo);
}