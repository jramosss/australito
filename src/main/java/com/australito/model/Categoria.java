package com.australito.model;

import java.math.BigDecimal;

public class Categoria {
    private Long id;
    private String nombre;
    private BigDecimal presupuestoMensual;

    public Categoria() {
        this.presupuestoMensual = BigDecimal.ZERO;
    }

    public Categoria(Long id, String nombre, BigDecimal presupuestoMensual) {
        this.id = id;
        this.nombre = nombre;
        this.presupuestoMensual = (presupuestoMensual != null) ? presupuestoMensual : BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPresupuestoMensual() {
        return presupuestoMensual;
    }

    public void setPresupuestoMensual(BigDecimal presupuestoMensual) {
        this.presupuestoMensual = presupuestoMensual;
    }

    @Override
    public String toString() {
        return nombre + " (Presupuesto: $" + presupuestoMensual + ")";
    }
}
