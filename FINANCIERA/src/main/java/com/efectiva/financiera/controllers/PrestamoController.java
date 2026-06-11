package com.efectiva.financiera.controllers;

import com.efectiva.financiera.dto.SolicitudPrestamoDto;
import com.efectiva.financiera.models.SolicitudPrestamo;
import com.efectiva.financiera.repositories.SolicitudPrestamoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/prestamos")
@CrossOrigin(origins = "*")
public class PrestamoController {

    private final SolicitudPrestamoRepository solicitudRepo;

    public PrestamoController(SolicitudPrestamoRepository solicitudRepo) {
        this.solicitudRepo = solicitudRepo;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarPrestamo(@RequestBody SolicitudPrestamoDto dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verificar solicitud activa
            List<SolicitudPrestamo> solicitudesActivas = solicitudRepo
                    .findByUsuarioIdOrderByFechaSolicitudDesc(UUID.fromString(dto.getUsuarioId()))
                    .stream()
                    .filter(s -> s.getEstado().equals("PENDIENTE") ||
                            s.getEstado().equals("APROBADO_PROVISIONAL") ||
                            s.getEstado().equals("EN_REVISION"))
                    .toList();

            if (!solicitudesActivas.isEmpty()) {
                response.put("success", false);
                response.put("message", "Ya tienes una solicitud activa. Espera que sea procesada.");
                return ResponseEntity.badRequest().body(response);
            }

            if (dto.getMonto() > 6000) {
                response.put("success", false);
                response.put("message", "El monto máximo permitido es S/ 6,000.");
                return ResponseEntity.badRequest().body(response);
            }

            // Calcular scoring real de Financiera Efectiva
            int scoring = calcularScoring(dto);

            // Calcular cuota mensual
            BigDecimal cuota = calcularCuota(dto.getMonto(), dto.getPlazoMeses());

            // Determinar estado según scoring
            String estado;
            String mensaje;
            if (scoring >= 70) {
                estado = "APROBADO_PROVISIONAL";
                mensaje = "¡Pre-aprobado! Un asesor confirmará el desembolso.";
            } else if (scoring >= 40) {
                estado = "EN_REVISION";
                mensaje = "Tu solicitud pasa a evaluación del Jefe de Agencia.";
            } else {
                estado = "RECHAZADO";
                mensaje = "No cumples los requisitos mínimos. Puedes volver a intentarlo en 3 meses.";
            }

            // Guardar solicitud
            SolicitudPrestamo solicitud = new SolicitudPrestamo();
            solicitud.setUsuarioId(UUID.fromString(dto.getUsuarioId()));
            solicitud.setMonto(BigDecimal.valueOf(dto.getMonto()));
            solicitud.setPlazoMeses(dto.getPlazoMeses());
            solicitud.setProposito(dto.getProposito());
            solicitud.setCuotaMensual(cuota);
            solicitud.setSeguroDesgravamen(calcularSeguro(dto.getMonto()));
            solicitud.setCuotaTotal(cuota);
            solicitud.setPuntajeScoring(scoring);
            solicitud.setEstado(estado);
            solicitud.setIngresoMensual(BigDecimal.valueOf(dto.getIngresoMensual()));
            solicitud.setTieneHistorialEfectiva(dto.isTieneHistorialEfectiva());
            solicitud.setMesesTrabajo(dto.getMesesTrabajo());
            solicitud.setTieneReciboServicios(dto.isTieneReciboServicios());
            solicitud.setTieneDeudaSbs(dto.isTieneDeudaSbs());
            solicitud.setClasificacionSbs("NORMAL");
            solicitud.setSaldoCapital(BigDecimal.valueOf(dto.getMonto()));
            solicitud.setFechaVencimiento(java.time.LocalDate.now().plusMonths(dto.getPlazoMeses()));
            solicitudRepo.save(solicitud);

            response.put("success", true);
            response.put("estado", estado);
            response.put("mensaje", mensaje);
            response.put("scoring", scoring);
            response.put("cuotaMensual", cuota);
            response.put("monto", dto.getMonto());
            response.put("plazoMeses", dto.getPlazoMeses());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> getSolicitudesUsuario(@PathVariable String usuarioId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SolicitudPrestamo> solicitudes = solicitudRepo
                    .findByUsuarioIdOrderByFechaSolicitudDesc(UUID.fromString(usuarioId));
            response.put("success", true);
            response.put("solicitudes", solicitudes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<?> getTodasSolicitudes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SolicitudPrestamo> solicitudes = solicitudRepo.findAll().stream()
                    .sorted((a, b) -> b.getFechaSolicitud().compareTo(a.getFechaSolicitud()))
                    .toList();
            response.put("success", true);
            response.put("solicitudes", solicitudes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/evaluar")
    public ResponseEntity<?> evaluarSolicitud(@PathVariable String id,
                                              @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            SolicitudPrestamo solicitud = solicitudRepo.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            solicitud.setEstado(body.get("estado"));
            solicitud.setEvaluadoPor(body.get("evaluadoPor"));
            solicitud.setFechaEvaluacion(java.time.LocalDateTime.now());
            solicitudRepo.save(solicitud);
            response.put("success", true);
            response.put("message", "Solicitud actualizada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Scoring REAL de Financiera Efectiva
    private int calcularScoring(SolicitudPrestamoDto dto) {
        int score = 0;

        // 1. CAPACIDAD DE PAGO (35 puntos)
        if (dto.getIngresoMensual() > 0) {
            double cuota = calcularCuota(dto.getMonto(), dto.getPlazoMeses()).doubleValue();
            double ratio = cuota / dto.getIngresoMensual();
            if (ratio <= 0.20)      score += 35;
            else if (ratio <= 0.25) score += 25;
            else if (ratio <= 0.30) score += 15;
        }

        // 2. HISTORIAL EN EFECTIVA (25 puntos)
        if (dto.isTieneHistorialEfectiva()) score += 25;

        // 3. ESTABILIDAD LABORAL (20 puntos)
        if (dto.getMesesTrabajo() >= 24)      score += 20;
        else if (dto.getMesesTrabajo() >= 12) score += 12;
        else if (dto.getMesesTrabajo() >= 6)  score += 6;

        // 4. VERIFICACION DOMICILIARIA (10 puntos)
        if (dto.isTieneReciboServicios()) score += 10;

        // 5. CENTRAL DE RIESGOS SBS (10 puntos)
        if (!dto.isTieneDeudaSbs()) score += 10;

        return Math.min(100, score);
    }

    // Fórmula de cuota mensual
    // Fórmula de cuota mensual con seguro de desgravamen (real de Financiera Efectiva)
    private BigDecimal calcularCuota(double monto, int plazoMeses) {
        double tasaMensual = 0.035;
        // Cuota de crédito (amortización + interés)
        double cuotaCredito = monto * (tasaMensual * Math.pow(1 + tasaMensual, plazoMeses))
                / (Math.pow(1 + tasaMensual, plazoMeses) - 1);
        // Seguro de desgravamen (0.05% del monto original mensual)
        double seguro = monto * 0.0005;
        // Cuota total = cuota crédito + seguro
        double cuotaTotal = cuotaCredito + seguro;
        return BigDecimal.valueOf(cuotaTotal).setScale(2, RoundingMode.HALF_UP);
    }

    // Calcular solo el seguro de desgravamen
    private BigDecimal calcularSeguro(double monto) {
        return BigDecimal.valueOf(monto * 0.0005).setScale(2, RoundingMode.HALF_UP);
    }

    // Calcular mora (1.5x la tasa mensual por días de atraso)
    private BigDecimal calcularMora(double saldoCapital, int diasAtraso) {
        if (diasAtraso <= 0) return BigDecimal.ZERO;
        double tasaDiaria = 0.035 / 30; // tasa diaria normal
        double tasaMoratoria = tasaDiaria * 1.5; // penalidad del 50%
        double mora = saldoCapital * tasaMoratoria * diasAtraso;
        return BigDecimal.valueOf(mora).setScale(2, RoundingMode.HALF_UP);
    }
}