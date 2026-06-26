package com.efectiva.financiera.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gestiones_cobranza")
public class GestionCobranza {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID solicitudId;
    private String usuarioGestion;
    private String tipoGestion; // Ej: LLAMADA, CORREO, VISITA
    private String acuerdo;
    private LocalDateTime fechaGestion = LocalDateTime.now();

    // Constructores, Getters y Setters
    public GestionCobranza() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSolicitudId() { return solicitudId; }
    public void setSolicitudId(UUID solicitudId) { this.solicitudId = solicitudId; }

    public String getUsuarioGestion() { return usuarioGestion; }
    public void setUsuarioGestion(String usuarioGestion) { this.usuarioGestion = usuarioGestion; }

    public String getTipoGestion() { return tipoGestion; }
    public void setTipoGestion(String tipoGestion) { this.tipoGestion = tipoGestion; }

    public String getAcuerdo() { return acuerdo; }
    public void setAcuerdo(String acuerdo) { this.acuerdo = acuerdo; }

    public LocalDateTime getFechaGestion() { return fechaGestion; }
    public void setFechaGestion(LocalDateTime fechaGestion) { this.fechaGestion = fechaGestion; }
}