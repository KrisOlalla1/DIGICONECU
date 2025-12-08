package com.arcbank.switch.repository;

import com.arcbank.switch.model.entity.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Transaccion.
 * PERSONA 1: Usa este repositorio para CRUD de transacciones.
 * PERSONA 2: Usa este repositorio para consultar transacciones existentes.
 */
@Repository
public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Integer> {
    
    /**
     * Busca una transacción por su EndToEnd único.
     * Útil para validar duplicados.
     */
    Optional<TransaccionEntity> findByEndToEnd(String endToEnd);
    
    /**
     * Verifica si existe una transacción con el EndToEnd dado.
     */
    boolean existsByEndToEnd(String endToEnd);
}
