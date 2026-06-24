package com.efectiva.financiera.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimientos")
@Data
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cuenta_id", nullable = false)
    private UUID cuentaId;

    @Column(nullable = false)
    private String tipo; // "INGRESO" o "EGRESO"

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "saldo_despues", nullable = false)
    private BigDecimal saldoDespues;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}