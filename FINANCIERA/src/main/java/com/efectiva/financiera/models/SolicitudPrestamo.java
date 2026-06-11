package com.efectiva.financiera.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitudes_prestamo")
@Data
public class SolicitudPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "tasa_interes", nullable = false)
    private BigDecimal tasaInteres = new BigDecimal("3.5");

    @Column(name = "cuota_mensual")
    private BigDecimal cuotaMensual;

    @Column(name = "proposito")
    private String proposito;

    @Column(nullable = false)
    private String estado = "PENDIENTE";

    @Column(name = "puntaje_scoring")
    private Integer puntajeScoring;

    @Column(name = "evaluado_por")
    private String evaluadoPor;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_evaluacion")
    private LocalDateTime fechaEvaluacion;

    // Campos del scoring real de Financiera Efectiva
    @Column(name = "ingreso_mensual")
    private BigDecimal ingresoMensual;

    @Column(name = "tiene_historial_efectiva")
    private Boolean tieneHistorialEfectiva = false;

    @Column(name = "meses_trabajo")
    private Integer mesesTrabajo = 0;

    @Column(name = "tiene_recibo_servicios")
    private Boolean tieneReciboServicios = false;

    @Column(name = "tiene_deuda_sbs")
    private Boolean tieneDeudaSbs = false;

    @Column(name = "clasificacion_sbs")
    private String clasificacionSbs = "NORMAL";

    @Column(name = "dias_atraso")
    private Integer diasAtraso = 0;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "saldo_capital")
    private BigDecimal saldoCapital;
    @Column(name = "seguro_desgravamen")
    private BigDecimal seguroDesgravamen;

    @Column(name = "cuota_total")
    private BigDecimal cuotaTotal;

    @Column(name = "interes_moratorio")
    private BigDecimal interesMoratorio = BigDecimal.ZERO;

    @Column(name = "tasa_moratoria")
    private BigDecimal tasaMoratoria = new BigDecimal("0.0525");
}