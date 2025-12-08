package com.arcbank.switchtransaccional.repository;

import com.arcbank.switchtransaccional.model.entity.EntidadBancariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntidadBancariaRepository extends JpaRepository<EntidadBancariaEntity, Integer> {
    // findById ya viene por defecto
}
