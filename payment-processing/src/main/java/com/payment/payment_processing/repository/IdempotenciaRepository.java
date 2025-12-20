package com.payment.payment_processing.repository;

import com.payment.payment_processing.model.IdempotenciaCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotenciaRepository extends JpaRepository<IdempotenciaCache, String> {
}