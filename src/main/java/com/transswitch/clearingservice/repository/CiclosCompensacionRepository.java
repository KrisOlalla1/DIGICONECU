package com.transswitch.clearingservice.repository;

import com.transswitch.clearingservice.model.CiclosCompensacion;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiclosCompensacionRepository extends JpaRepository<CiclosCompensacion, UUID> {
    Optional<CiclosCompensacion> findByFechaCiclo(LocalDate fechaCiclo);
    boolean existsByFechaCiclo(LocalDate fechaCiclo);
}
