package com.switchpay.transaccional.accountbalance.domain.repository;

import com.switchpay.transaccional.accountbalance.domain.entity.CuentaPrefondeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CuentaPrefondeoRepository extends JpaRepository<CuentaPrefondeo, UUID> {
    Optional<CuentaPrefondeo> findByBancoCodigo(String bancoCodigo);
}
