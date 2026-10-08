package com.australito.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Ingreso extends Transaccion {
    private Cuenta cuenta;
    private String origen;

    public Ingreso() {
        super();
    }

    public Ingreso(Long id, LocalDate fecha, String descripcion, BigDecimal monto, Cuenta cuenta, String origen) {
        super(id, fecha, descripcion, monto, "ARS");
        this.cuenta = cuenta;
        this.origen = origen;
    }

    @Override
    public String getTipoTransaccion() {
        return "INGRESO";
    }

    @Override
    public BigDecimal calcularImpactoEnBalance() {
        return (getMonto() != null) ? getMonto() : BigDecimal.ZERO;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    @Override
    public String toString() {
        String ctaNombre = (cuenta != null) ? cuenta.getNombre() : "Sin Cuenta";
        return String.format("[%s] %s | ARS: +$%s | Origen: %s | Cta: %s | %s",
                getFecha(), getDescripcion(), getMonto(), origen, ctaNombre, getTipoTransaccion());
    }
}
