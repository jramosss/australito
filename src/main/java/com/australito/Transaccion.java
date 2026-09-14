package com.australito;

import java.sql.Date;

public class Transaccion {
    private int id;
    private int cuentaId;
    private int categoriaId;
    private double montoArs;
    private double montoUsd;
    private Date fecha;
    private String descripcion;

    public Transaccion(int cuentaId, int categoriaId, double montoArs, double montoUsd, Date fecha, String descripcion) {
        this.cuentaId = cuentaId;
        this.categoriaId = categoriaId;
        this.montoArs = montoArs;
        this.montoUsd = montoUsd;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    // Getters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCuentaId() { return cuentaId; }
    public int getCategoriaId() { return categoriaId; }
    public double getMontoArs() { return montoArs; }
    public double getMontoUsd() { return montoUsd; }
    public Date getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    
    @Override
    public String toString() {
        return "Transaccion [ID=" + id + ", ARS=" + montoArs + ", USD=" + String.format("%.2f", montoUsd) + ", Fecha=" + fecha + ", Desc=" + descripcion + "]";
    }
}
