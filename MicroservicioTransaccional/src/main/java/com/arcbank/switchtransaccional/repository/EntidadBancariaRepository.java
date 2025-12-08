package com.arcbank.switch.repository;

import com.arcbank.switch.model.entity.EntidadBancariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad EntidadBancaria.
 * PERSONA 2: Usa este repositorio para obtener información de bancos y endpoints.
 */
@Repository
public interface EntidadBancariaRepository extends JpaRepository<EntidadBancariaEntity, Integer> {
    
}
