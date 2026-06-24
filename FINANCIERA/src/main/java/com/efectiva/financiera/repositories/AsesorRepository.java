package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.Asesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsesorRepository extends JpaRepository<Asesor, UUID> {
    Optional<Asesor> findByCorreo(String correo);
}