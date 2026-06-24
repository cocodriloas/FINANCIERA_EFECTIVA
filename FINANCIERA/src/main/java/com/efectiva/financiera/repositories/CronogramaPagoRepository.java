package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.CronogramaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, UUID> {

    // Este es el método mágico que le faltaba a tu proyecto para poder mostrar el pop-up
    List<CronogramaPago> findBySolicitudIdOrderByNumeroCuotaAsc(UUID solicitudId);
}