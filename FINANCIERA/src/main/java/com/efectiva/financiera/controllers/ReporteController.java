package com.efectiva.financiera.controllers;

import com.efectiva.financiera.models.KpiCarteraMensual;
import com.efectiva.financiera.repositories.KpiCarteraMensualRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final KpiCarteraMensualRepository kpiRepository;

    public ReporteController(KpiCarteraMensualRepository kpiRepository) {
        this.kpiRepository = kpiRepository;
    }

    @GetMapping("/kpis-historicos")
    public ResponseEntity<?> getKpisHistoricos() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<KpiCarteraMensual> kpis = kpiRepository.findAllByOrderByAnioAscMesAsc();
            response.put("success", true);
            response.put("data", kpis);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}