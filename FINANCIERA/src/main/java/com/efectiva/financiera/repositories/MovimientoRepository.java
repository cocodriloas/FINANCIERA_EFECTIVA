package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, UUID> {
    // Nos servirá para mostrar el estado de cuenta ordenado del más reciente al más antiguo
    List<Movimiento> findByCuentaIdOrderByFechaDesc(UUID cuentaId);
}