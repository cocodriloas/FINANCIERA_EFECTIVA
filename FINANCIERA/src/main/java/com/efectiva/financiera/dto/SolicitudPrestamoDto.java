package com.efectiva.financiera.dto;

public class SolicitudPrestamoDto {
    private String usuarioId;
    private double monto;
    private int plazoMeses;
    private String proposito;

    // Campos del scoring real
    private double ingresoMensual;
    private boolean tieneHistorialEfectiva;
    private int mesesTrabajo;
    private boolean tieneReciboServicios;
    private boolean tieneDeudaSbs;

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String u) { this.usuarioId = u; }
    public double getMonto() { return monto; }
    public void setMonto(double m) { this.monto = m; }
    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int p) { this.plazoMeses = p; }
    public String getProposito() { return proposito; }
    public void setProposito(String p) { this.proposito = p; }
    public double getIngresoMensual() { return ingresoMensual; }
    public void setIngresoMensual(double i) { this.ingresoMensual = i; }
    public boolean isTieneHistorialEfectiva() { return tieneHistorialEfectiva; }
    public void setTieneHistorialEfectiva(boolean t) { this.tieneHistorialEfectiva = t; }
    public int getMesesTrabajo() { return mesesTrabajo; }
    public void setMesesTrabajo(int m) { this.mesesTrabajo = m; }
    public boolean isTieneReciboServicios() { return tieneReciboServicios; }
    public void setTieneReciboServicios(boolean t) { this.tieneReciboServicios = t; }
    public boolean isTieneDeudaSbs() { return tieneDeudaSbs; }
    public void setTieneDeudaSbs(boolean t) { this.tieneDeudaSbs = t; }
}