package com.australito.model;

import java.math.BigDecimal;

public class Cuenta {
    private Long id;
    private String nombre;
    private String tipoCuenta;
    private BigDecimal saldoActual;

    public Cuenta() {
        this.saldoActual = BigDecimal.ZERO;
    }

    public Cuenta(Long id, String nombre, String tipoCuenta, BigDecimal saldoActual) {
        this.id = id;
        this.nombre = nombre;
        this.tipoCuenta = tipoCuenta;
        this.saldoActual = (saldoActual != null) ? saldoActual : BigDecimal.ZERO;
    }

    public void debitar(BigDecimal monto) {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) > 0) {
            this.saldoActual = this.saldoActual.subtract(monto);
        }
    }

    public void acreditar(BigDecimal monto) {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) > 0) {
            this.saldoActual = this.saldoActual.add(monto);
        }
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

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
    }

    @Override
    public String toString() {
        return nombre + " (" + tipoCuenta + ") - Saldo: $" + saldoActual;
    }
}
