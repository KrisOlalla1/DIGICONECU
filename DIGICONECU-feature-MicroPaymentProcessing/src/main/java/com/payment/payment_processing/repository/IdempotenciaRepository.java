package com.payment.payment_processing.repository;

import com.payment.payment_processing.model.IdempotenciaCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IdempotenciaRepository extends JpaRepository<IdempotenciaCache, String> {

    @Modifying
    @Query("DELETE FROM IdempotenciaCache i WHERE i.fechaRegistro < :limite")
    int deleteByFechaRegistroBefore(@Param("limite") LocalDateTime limite);
}