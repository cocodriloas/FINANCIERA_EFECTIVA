package com.efectiva.financiera.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cronograma_pagos")
@Data
public class CronogramaPago {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "solicitud_id", nullable = false)
    private UUID solicitudId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "saldo_inicial")
    private BigDecimal saldoInicial;

    @Column(nullable = false)
    private BigDecimal interes;

    @Column(nullable = false)
    private BigDecimal amortizacion;

    @Column(nullable = false)
    private BigDecimal cuota; // Cuota total incluyendo seguro

    @Column(name = "saldo_final")
    private BigDecimal saldoFinal;

    @Column(nullable = false)
    private String estado = "PENDIENTE"; // PENDIENTE, PAGADO, ATRASADO

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "dias_atraso")
    private Integer diasAtraso = 0;

    @Column(name = "interes_moratorio")
    private BigDecimal interesMoratorio = BigDecimal.ZERO;
}