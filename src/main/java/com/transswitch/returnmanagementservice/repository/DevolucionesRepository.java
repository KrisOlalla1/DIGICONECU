package com.transswitch.returnmanagementservice.repository;

import com.transswitch.returnmanagementservice.model.Devoluciones;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucionesRepository extends JpaRepository<Devoluciones, UUID> {
    Optional<Devoluciones> findByReturnInstructionId(UUID returnInstructionId);
    boolean existsByTransaccionOriginalInstructionId(UUID transaccionOriginalInstructionId);
}
