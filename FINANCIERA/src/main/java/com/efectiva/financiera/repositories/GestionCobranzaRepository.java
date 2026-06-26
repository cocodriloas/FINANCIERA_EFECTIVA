package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.GestionCobranza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GestionCobranzaRepository extends JpaRepository<GestionCobranza, UUID> {
    List<GestionCobranza> findBySolicitudIdOrderByFechaGestionDesc(UUID solicitudId);
}