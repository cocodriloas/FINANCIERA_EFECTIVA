package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.SolicitudPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SolicitudPrestamoRepository extends JpaRepository<SolicitudPrestamo, UUID> {
    List<SolicitudPrestamo> findByUsuarioIdOrderByFechaSolicitudDesc(UUID usuarioId);
    List<SolicitudPrestamo> findByEstadoOrderByFechaSolicitudDesc(String estado);
}