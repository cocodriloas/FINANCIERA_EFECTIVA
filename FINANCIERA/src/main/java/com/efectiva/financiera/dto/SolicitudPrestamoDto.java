package com.efectiva.financiera.dto;

public class SolicitudPrestamoDto {
    private String usuarioId;
    private double monto;
    private int plazoMeses;
    private String proposito;
    private double ingresoMensual;
    private int mesesTrabajo;
    private boolean tieneHistorialEfectiva;
    private boolean tieneReciboServicios;
    private boolean tieneDeudaSbs;

    // NUEVOS CAMPOS EMPRESARIALES QUE JAVA NO ENCONTRABA
    private int diaPago;
    private boolean tieneSeguro;

    // Getters y Setters
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }

    public String getProposito() { return proposito; }
    public void setProposito(String proposito) { this.proposito = proposito; }

    public double getIngresoMensual() { return ingresoMensual; }
    public void setIngresoMensual(double ingresoMensual) { this.ingresoMensual = ingresoMensual; }

    public int getMesesTrabajo() { return mesesTrabajo; }
    public void setMesesTrabajo(int mesesTrabajo) { this.mesesTrabajo = mesesTrabajo; }

    public boolean isTieneHistorialEfectiva() { return tieneHistorialEfectiva; }
    public void setTieneHistorialEfectiva(boolean tieneHistorialEfectiva) { this.tieneHistorialEfectiva = tieneHistorialEfectiva; }

    public boolean isTieneReciboServicios() { return tieneReciboServicios; }
    public void setTieneReciboServicios(boolean tieneReciboServicios) { this.tieneReciboServicios = tieneReciboServicios; }

    public boolean isTieneDeudaSbs() { return tieneDeudaSbs; }
    public void setTieneDeudaSbs(boolean tieneDeudaSbs) { this.tieneDeudaSbs = tieneDeudaSbs; }

    public int getDiaPago() { return diaPago; }
    public void setDiaPago(int diaPago) { this.diaPago = diaPago; }

    public boolean isTieneSeguro() { return tieneSeguro; }
    public void setTieneSeguro(boolean tieneSeguro) { this.tieneSeguro = tieneSeguro; }
}