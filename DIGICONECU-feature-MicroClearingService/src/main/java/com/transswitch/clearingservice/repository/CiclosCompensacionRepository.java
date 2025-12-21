package com.transswitch.clearingservice.repository;

import com.transswitch.clearingservice.model.CiclosCompensacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CiclosCompensacionRepository extends JpaRepository<CiclosCompensacion, UUID> {
    Optional<CiclosCompensacion> findByFechaCiclo(LocalDate fechaCiclo);

    boolean existsByFechaCiclo(LocalDate fechaCiclo);

    @Query("SELECT c FROM CiclosCompensacion c ORDER BY c.fechaCiclo DESC")
    List<CiclosCompensacion> findTopByOrderByFechaCicloDesc(Pageable pageable);

    Optional<CiclosCompensacion> findFirstByOrderByFechaCicloDesc();
}
