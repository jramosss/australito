package com.australito.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Transaccion {
    private Long id;
    private LocalDate fecha;
    private String descripcion;
    private BigDecimal monto;
    private String moneda;

    public Transaccion() {
        this.fecha = LocalDate.now();
        this.moneda = "ARS";
    }

    public Transaccion(Long id, LocalDate fecha, String descripcion, BigDecimal monto, String moneda) {
        this.id = id;
        this.fecha = (fecha != null) ? fecha : LocalDate.now();
        this.descripcion = descripcion;
        this.monto = monto;
        this.moneda = (moneda != null) ? moneda : "ARS";
    }

    public abstract String getTipoTransaccion();

    public abstract BigDecimal calcularImpactoEnBalance();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}
