package com.efectiva.financiera.controllers;

import com.efectiva.financiera.dto.SolicitudPrestamoDto;
import com.efectiva.financiera.models.SolicitudPrestamo;
import com.efectiva.financiera.repositories.SolicitudPrestamoRepository;
import com.efectiva.financiera.models.CronogramaPago;
import com.efectiva.financiera.repositories.CronogramaPagoRepository;
import com.efectiva.financiera.models.Cuenta;
import com.efectiva.financiera.repositories.CuentaRepository;
import com.efectiva.financiera.models.Movimiento;
import com.efectiva.financiera.repositories.MovimientoRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/api/prestamos")
@CrossOrigin(origins = "*")
public class PrestamoController {

    private final SolicitudPrestamoRepository solicitudRepo;
    private final CronogramaPagoRepository cronogramaRepo;
    private final CuentaRepository cuentaRepo;
    private final MovimientoRepository movimientoRepo;

    public PrestamoController(SolicitudPrestamoRepository solicitudRepo,
                              CronogramaPagoRepository cronogramaRepo,
                              CuentaRepository cuentaRepo,
                              MovimientoRepository movimientoRepo) {
        this.solicitudRepo = solicitudRepo;
        this.cronogramaRepo = cronogramaRepo;
        this.cuentaRepo = cuentaRepo;
        this.movimientoRepo = movimientoRepo;
    }

    @PostMapping("/solicitar")
    @Transactional
    public ResponseEntity<?> solicitarPrestamo(@RequestBody SolicitudPrestamoDto dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Filtro robusto anti-caídas
            List<SolicitudPrestamo> solicitudesActivas = solicitudRepo.findAll().stream()
                    .filter(s -> s.getUsuarioId() != null && s.getUsuarioId().toString().equals(dto.getUsuarioId()))
                    .filter(s -> s.getEstado().equals("PENDIENTE") ||
                            s.getEstado().equals("APROBADO_PROVISIONAL") ||
                            s.getEstado().equals("EN_REVISION"))
                    .toList();

            if (!solicitudesActivas.isEmpty()) {
                response.put("success", false);
                response.put("message", "Ya tienes una solicitud activa. Espera que sea procesada.");
                return ResponseEntity.badRequest().body(response);
            }

            if (dto.getMonto() > 30000) {
                response.put("success", false);
                response.put("message", "El monto máximo permitido es S/ 30,000.");
                return ResponseEntity.badRequest().body(response);
            }

            double tem = dto.isTieneSeguro() ? 0.0290 : 0.0308;
            BigDecimal cuota = calcularCuotaExacta(dto.getMonto(), dto.getPlazoMeses(), tem);

            int scoring = calcularScoring(dto, cuota);
            String estado;
            String mensaje;
            if (scoring >= 70) {
                estado = "APROBADO_PROVISIONAL";
                mensaje = "¡Pre-aprobado! Un asesor confirmará el desembolso.";
            } else if (scoring >= 40) {
                estado = "EN_REVISION";
                mensaje = "Tu solicitud pasa a evaluación de Comité.";
            } else {
                estado = "RECHAZADO";
                mensaje = "No cumple los requisitos mínimos empresariales.";
            }

            SolicitudPrestamo solicitud = new SolicitudPrestamo();
            solicitud.setUsuarioId(UUID.fromString(dto.getUsuarioId()));
            solicitud.setMonto(BigDecimal.valueOf(dto.getMonto()));
            solicitud.setPlazoMeses(dto.getPlazoMeses());
            solicitud.setProposito(dto.getProposito());
            solicitud.setCuotaMensual(cuota);
            solicitud.setSeguroDesgravamen(BigDecimal.ZERO);
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

            LocalDate fechaVencimiento = LocalDate.now().plusMonths(1);
            try {
                fechaVencimiento = fechaVencimiento.withDayOfMonth(dto.getDiaPago());
            } catch (Exception e) {
                fechaVencimiento = YearMonth.from(fechaVencimiento).atEndOfMonth();
            }
            solicitud.setFechaVencimiento(fechaVencimiento);
            solicitud.setFechaSolicitud(java.time.LocalDateTime.now());

            SolicitudPrestamo guardada = solicitudRepo.save(solicitud);

            if (estado.equals("APROBADO_PROVISIONAL")) {
                generarCronograma(guardada);
            }

            response.put("success", true);
            response.put("estado", estado);
            response.put("mensaje", mensaje);
            response.put("scoring", scoring);
            response.put("cuotaMensual", cuota);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error real en la consola de IntelliJ
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", "Error interno al solicitar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> getSolicitudesUsuario(@PathVariable String usuarioId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Consulta 100% blindada para que el cliente siempre vea sus créditos
            List<SolicitudPrestamo> solicitudes = solicitudRepo.findAll().stream()
                    .filter(s -> s.getUsuarioId() != null && s.getUsuarioId().toString().equals(usuarioId))
                    .sorted((a, b) -> {
                        if (a.getFechaSolicitud() == null) return 1;
                        if (b.getFechaSolicitud() == null) return -1;
                        return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                    })
                    .toList();

            response.put("success", true);
            response.put("solicitudes", solicitudes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al cargar créditos: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<?> getTodasSolicitudes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SolicitudPrestamo> solicitudes = solicitudRepo.findAll().stream()
                    .sorted((a, b) -> {
                        if (a.getFechaSolicitud() == null) return 1;
                        if (b.getFechaSolicitud() == null) return -1;
                        return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                    })
                    .toList();
            response.put("success", true);
            response.put("solicitudes", solicitudes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}/cronograma")
    public ResponseEntity<?> getCronograma(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Consulta blindada al cronograma
            List<CronogramaPago> cronograma = cronogramaRepo.findAll().stream()
                    .filter(c -> c.getSolicitudId() != null && c.getSolicitudId().toString().equals(id))
                    .sorted(Comparator.comparingInt(CronogramaPago::getNumeroCuota))
                    .toList();

            response.put("success", true);
            response.put("cronograma", cronograma);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/evaluar")
    @Transactional
    public ResponseEntity<?> evaluarSolicitud(@PathVariable String id, @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            SolicitudPrestamo solicitud = solicitudRepo.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            String nuevoEstado = body.get("estado");
            String estadoAnterior = solicitud.getEstado() != null ? solicitud.getEstado() : "";

            solicitud.setEstado(nuevoEstado);
            solicitud.setEvaluadoPor(body.get("evaluadoPor"));
            solicitud.setFechaEvaluacion(java.time.LocalDateTime.now());

            if ((nuevoEstado.equals("APROBADO") || nuevoEstado.equals("APROBADO_PROVISIONAL"))
                    && !estadoAnterior.contains("APROBADO") && !estadoAnterior.equals("DESEMBOLSADO")) {

                List<CronogramaPago> existentes = cronogramaRepo.findAll().stream()
                        .filter(c -> c.getSolicitudId() != null && c.getSolicitudId().toString().equals(id))
                        .toList();

                if(existentes.isEmpty()) {
                    generarCronograma(solicitud);
                }
            }

            if (nuevoEstado.equals("DESEMBOLSADO") && !estadoAnterior.equals("DESEMBOLSADO")) {
                List<CronogramaPago> existentes = cronogramaRepo.findAll().stream()
                        .filter(c -> c.getSolicitudId() != null && c.getSolicitudId().toString().equals(id))
                        .toList();

                if (existentes.isEmpty()) {
                    generarCronograma(solicitud);
                }

                List<Cuenta> cuentas = cuentaRepo.findByUsuarioId(solicitud.getUsuarioId());
                if (!cuentas.isEmpty()) {
                    Cuenta cuentaCliente = cuentas.get(0);
                    cuentaCliente.setSaldo(cuentaCliente.getSaldo().add(solicitud.getMonto()));
                    cuentaRepo.save(cuentaCliente);

                    Movimiento ingreso = new Movimiento();
                    ingreso.setCuentaId(cuentaCliente.getId());
                    ingreso.setTipo("INGRESO");
                    ingreso.setMonto(solicitud.getMonto());
                    ingreso.setDescripcion("Desembolso Préstamo N° " + solicitud.getId().toString().substring(0,8).toUpperCase());
                    ingreso.setSaldoDespues(cuentaCliente.getSaldo());
                    movimientoRepo.save(ingreso);
                }
            }

            solicitudRepo.save(solicitud);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void generarCronograma(SolicitudPrestamo prestamo) {
        List<CronogramaPago> existentes = cronogramaRepo.findAll().stream()
                .filter(c -> c.getSolicitudId() != null && c.getSolicitudId().equals(prestamo.getId()))
                .toList();

        if (!existentes.isEmpty()) return;

        double saldoCapital = prestamo.getMonto().doubleValue();
        int plazo = prestamo.getPlazoMeses();
        double cuotaFija = prestamo.getCuotaMensual().doubleValue();

        double tem = 0.0308;
        double temSeguro = 0.0290;
        double cuotaPrueba = saldoCapital * (temSeguro * Math.pow(1 + temSeguro, plazo)) / (Math.pow(1 + temSeguro, plazo) - 1);
        if (Math.abs(cuotaPrueba - cuotaFija) < 1.0) {
            tem = temSeguro;
        }

        LocalDate fechaVencimiento = prestamo.getFechaVencimiento() != null ? prestamo.getFechaVencimiento() : LocalDate.now().plusMonths(1);
        List<CronogramaPago> listaCuotas = new ArrayList<>();

        for (int i = 1; i <= plazo; i++) {
            double interes = saldoCapital * tem;
            double amortizacion = cuotaFija - interes;

            CronogramaPago cuota = new CronogramaPago();

            // Asignación manual de UUID para evitar errores de Hibernate
            try {
                cuota.getClass().getMethod("setId", UUID.class).invoke(cuota, UUID.randomUUID());
            } catch (Exception ignored) {}

            cuota.setSolicitudId(prestamo.getId());
            cuota.setUsuarioId(prestamo.getUsuarioId());
            cuota.setNumeroCuota(i);
            cuota.setFechaVencimiento(fechaVencimiento);
            cuota.setSaldoInicial(BigDecimal.valueOf(saldoCapital).setScale(2, RoundingMode.HALF_UP));
            cuota.setInteres(BigDecimal.valueOf(interes).setScale(2, RoundingMode.HALF_UP));
            cuota.setAmortizacion(BigDecimal.valueOf(amortizacion).setScale(2, RoundingMode.HALF_UP));
            cuota.setCuota(BigDecimal.valueOf(cuotaFija).setScale(2, RoundingMode.HALF_UP));

            saldoCapital -= amortizacion;
            cuota.setSaldoFinal(BigDecimal.valueOf(Math.max(0, saldoCapital)).setScale(2, RoundingMode.HALF_UP));
            cuota.setEstado("PENDIENTE");

            listaCuotas.add(cuota);
            fechaVencimiento = fechaVencimiento.plusMonths(1);
        }

        // Guardar todo de una sola vez
        cronogramaRepo.saveAll(listaCuotas);
    }

    private int calcularScoring(SolicitudPrestamoDto dto, BigDecimal cuota) {
        int score = 0;
        if (dto.getIngresoMensual() > 0) {
            double ratio = cuota.doubleValue() / dto.getIngresoMensual();
            if (ratio <= 0.35)      score += 35;
            else if (ratio <= 0.40) score += 25;
            else if (ratio <= 0.50) score += 15;
        }
        if (dto.isTieneHistorialEfectiva()) score += 25;
        if (dto.getMesesTrabajo() >= 24)      score += 20;
        else if (dto.getMesesTrabajo() >= 12) score += 12;
        else if (dto.getMesesTrabajo() >= 6)  score += 6;
        if (dto.isTieneReciboServicios()) score += 10;
        if (!dto.isTieneDeudaSbs()) score += 10;
        return Math.min(100, score);
    }

    private BigDecimal calcularCuotaExacta(double monto, int plazoMeses, double tem) {
        double cuota = monto * (tem * Math.pow(1 + tem, plazoMeses)) / (Math.pow(1 + tem, plazoMeses) - 1);
        return BigDecimal.valueOf(cuota).setScale(2, RoundingMode.HALF_UP);
    }
}