package com.switchpay.transaccional.accountbalance.domain.repository;

import com.switchpay.transaccional.accountbalance.domain.entity.OperacionPrefondeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperacionPrefondeoRepository extends JpaRepository<OperacionPrefondeo, UUID> {
    List<OperacionPrefondeo> findByInstructionId(UUID instructionId);
}
