package com.efectiva.financiera.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferenciaRequestDto {
    private UUID cuentaOrigenId;
    private String numeroCuentaDestino;
    private BigDecimal monto;
    private String concepto;
}