package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, UUID> {
    List<Cuenta> findByUsuarioId(UUID usuarioId);
}