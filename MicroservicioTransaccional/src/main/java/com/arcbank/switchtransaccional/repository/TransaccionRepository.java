package com.arcbank.switchtransaccional.repository;

import com.arcbank.switchtransaccional.model.entity.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Integer> {

    Optional<TransaccionEntity> findByEndToEnd(String endToEnd);

    boolean existsByEndToEnd(String endToEnd);
}
