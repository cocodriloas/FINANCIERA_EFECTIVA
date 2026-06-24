package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.KpiCarteraMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KpiCarteraMensualRepository extends JpaRepository<KpiCarteraMensual, UUID> {
    // Nos asegura que el gráfico se dibuje en orden cronológico (Ej: Enero 2023 -> Mayo 2026)
    List<KpiCarteraMensual> findAllByOrderByAnioAscMesAsc();
}