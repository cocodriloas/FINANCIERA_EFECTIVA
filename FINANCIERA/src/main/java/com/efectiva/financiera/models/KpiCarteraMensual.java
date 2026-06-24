package com.efectiva.financiera.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "kpis_cartera_mensual")
@Data
public class KpiCarteraMensual {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agencia_id")
    private UUID agenciaId;

    private Integer anio;
    private Integer mes;

    @Column(name = "cartera_total")
    private BigDecimal carteraTotal;

    @Column(name = "cartera_vigente")
    private BigDecimal carteraVigente;

    @Column(name = "cartera_vencida")
    private BigDecimal carteraVencida;

    @Column(name = "ratio_mora")
    private BigDecimal ratioMora;

    @Column(name = "nro_clientes")
    private Integer nroClientes;

    @Column(name = "nro_creditos")
    private Integer nroCreditos;

    private BigDecimal desembolsos;

    @Column(name = "tasa_promedio")
    private BigDecimal tasaPromedio;
}