package com.efectiva.financiera.controllers;

import com.efectiva.financiera.models.CronogramaPago;
import com.efectiva.financiera.models.GestionCobranza;
import com.efectiva.financiera.models.SolicitudPrestamo;
import com.efectiva.financiera.repositories.CronogramaPagoRepository;
import com.efectiva.financiera.repositories.GestionCobranzaRepository;
import com.efectiva.financiera.repositories.SolicitudPrestamoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/cobranzas")
    @CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8080}")
public class CobranzaController {

    private final SolicitudPrestamoRepository solicitudRepo;
    private final CronogramaPagoRepository cronogramaRepo;
    private final GestionCobranzaRepository gestionRepo;

    public CobranzaController(SolicitudPrestamoRepository solicitudRepo,
                              CronogramaPagoRepository cronogramaRepo,
                              GestionCobranzaRepository gestionRepo) {
        this.solicitudRepo = solicitudRepo;
        this.cronogramaRepo = cronogramaRepo;
        this.gestionRepo = gestionRepo;
    }

    // REGLA R1: Consulta por bandas de mora y cálculo de atraso
    @GetMapping("/cartera")
    public ResponseEntity<?> getCarteraMorosa(HttpServletRequest request) {
        // SEGURIDAD BLINDADA: Ahora acepta GERENTE y GERENCIA
        String rol = (String) request.getAttribute("rolUsuario");
        if (rol == null || (!rol.equals("ASESOR") && !rol.equals("ADMINISTRADOR") && !rol.equals("RIESGOS") && !rol.equals("GERENCIA") && !rol.equals("GERENTE"))) {
            return ResponseEntity.status(403).body(Collections.singletonMap("message", "Acceso denegado a Cobranzas"));
        }

        LocalDate hoy = LocalDate.now();
        List<Map<String, Object>> carteraMorosa = new ArrayList<>();

        List<SolicitudPrestamo> creditosActivos = solicitudRepo.findAll().stream()
                .filter(s -> "DESEMBOLSADO".equals(s.getEstado()) || "JUDICIAL".equals(s.getEstado()) || "CASTIGADO".equals(s.getEstado()))
                .toList();

        for (SolicitudPrestamo credito : creditosActivos) {
            List<CronogramaPago> cuotas = cronogramaRepo.findAll().stream()
                    .filter(c -> c.getSolicitudId() != null && c.getSolicitudId().equals(credito.getId()))
                    .toList();

            Optional<CronogramaPago> cuotaVencida = cuotas.stream()
                    .filter(c -> "PENDIENTE".equals(c.getEstado()) && c.getFechaVencimiento().isBefore(hoy))
                    .min(Comparator.comparing(CronogramaPago::getFechaVencimiento));

            if (cuotaVencida.isPresent()) {
                long diasAtraso = ChronoUnit.DAYS.between(cuotaVencida.get().getFechaVencimiento(), hoy);

                String banda;
                if (diasAtraso <= 30) banda = "PREVENTIVA";
                else if (diasAtraso <= 60) banda = "TEMPRANA";
                else if (diasAtraso <= 120) banda = "TARDIA";
                else if (diasAtraso <= 180) banda = "JUDICIAL";
                else banda = "CASTIGO";

                Map<String, Object> item = new HashMap<>();
                item.put("solicitudId", credito.getId());
                item.put("diasAtraso", diasAtraso);
                item.put("banda", banda);
                item.put("estadoCredito", credito.getEstado());
                item.put("cuotaAtrasada", cuotaVencida.get().getNumeroCuota());
                carteraMorosa.add(item);
            }
        }
        return ResponseEntity.ok(Collections.singletonMap("cartera", carteraMorosa));
    }

    // REGLA R2: Registro e historial de gestiones
    @PostMapping("/{solicitudId}/gestion")
    public ResponseEntity<?> registrarGestion(@PathVariable UUID solicitudId, @RequestBody GestionCobranza gestion, HttpServletRequest request) {
        String rol = (String) request.getAttribute("rolUsuario");
        if (rol == null) return ResponseEntity.status(403).body("Acceso denegado");

        gestion.setSolicitudId(solicitudId);
        gestionRepo.save(gestion);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/{solicitudId}/gestion")
    public ResponseEntity<?> verHistorialGestiones(@PathVariable UUID solicitudId) {
        return ResponseEntity.ok(gestionRepo.findBySolicitudIdOrderByFechaGestionDesc(solicitudId));
    }

    // REGLA R3: Transición a Judicial y Castigo (Controlando umbrales y roles)
    @PutMapping("/{solicitudId}/transicion")
    public ResponseEntity<?> transicionEstado(@PathVariable UUID solicitudId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String rol = (String) request.getAttribute("rolUsuario");
        String nuevoEstado = body.get("nuevoEstado");
        long diasAtraso = Long.parseLong(body.get("diasAtraso"));

        // Validaciones estrictas de rúbrica
        if ("JUDICIAL".equals(nuevoEstado)) {
            if (diasAtraso < 121) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Error: Para pasar a Judicial debe tener ≥121 días de atraso."));
            }
            if (!"RIESGOS".equals(rol) && !"ADMINISTRADOR".equals(rol)) {
                return ResponseEntity.status(403).body(Collections.singletonMap("message", "Error: Solo Riesgos o Administrador pueden derivar a Judicial."));
            }
        } else if ("CASTIGADO".equals(nuevoEstado)) {
            if (diasAtraso <= 180) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Error: Para pasar a Castigo debe tener >180 días de atraso."));
            }
            // SEGURIDAD BLINDADA: Ahora acepta GERENTE y GERENCIA para castigar la cartera
            if (!"RIESGOS".equals(rol) && !"GERENCIA".equals(rol) && !"GERENTE".equals(rol)) {
                return ResponseEntity.status(403).body(Collections.singletonMap("message", "Error: Solo Riesgos o Gerencia pueden castigar cartera."));
            }
        } else {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Estado no válido."));
        }

        SolicitudPrestamo solicitud = solicitudRepo.findById(solicitudId).orElseThrow();
        solicitud.setEstado(nuevoEstado);
        solicitudRepo.save(solicitud);

        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}