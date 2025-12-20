package com.digiconecu.network_management_service.repository;

import com.digiconecu.network_management_service.model.Banco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BancoRepository extends JpaRepository<Banco, UUID> {

    // Busca un banco por su código inmutable (ej: PICHINCHA)
    Optional<Banco> findByCodigo(String codigo);

    // Verifica si un código ya existe para evitar duplicados en el registro
    boolean existsByCodigo(String codigo);
}