package com.australito.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Gasto extends Transaccion {
    private Categoria categoria;
    private Cuenta cuenta;
    private Cotizacion cotizacionAplicada;
    private BigDecimal montoUSD;

    public Gasto() {
        super();
        this.montoUSD = BigDecimal.ZERO;
    }

    public Gasto(Long id, LocalDate fecha, String descripcion, BigDecimal montoARS,
                 Categoria categoria, Cuenta cuenta, Cotizacion cotizacionAplicada) {
        super(id, fecha, descripcion, montoARS, "ARS");
        this.categoria = categoria;
        this.cuenta = cuenta;
        this.cotizacionAplicada = cotizacionAplicada;
        calcularMontoUSD();
    }

    public void calcularMontoUSD() {
        if (getMonto() != null && cotizacionAplicada != null && cotizacionAplicada.getTasaVentaEfectiva() != null
                && cotizacionAplicada.getTasaVentaEfectiva().compareTo(BigDecimal.ZERO) > 0) {
            this.montoUSD = getMonto().divide(cotizacionAplicada.getTasaVentaEfectiva(), 2, RoundingMode.HALF_EVEN);
        } else {
            this.montoUSD = BigDecimal.ZERO;
        }
    }

    @Override
    public String getTipoTransaccion() {
        return "GASTO";
    }

    @Override
    public BigDecimal calcularImpactoEnBalance() {
        return (getMonto() != null) ? getMonto().negate() : BigDecimal.ZERO;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Cotizacion getCotizacionAplicada() {
        return cotizacionAplicada;
    }

    public void setCotizacionAplicada(Cotizacion cotizacionAplicada) {
        this.cotizacionAplicada = cotizacionAplicada;
        calcularMontoUSD();
    }

    public BigDecimal getMontoUSD() {
        return montoUSD;
    }

    public void setMontoUSD(BigDecimal montoUSD) {
        this.montoUSD = montoUSD;
    }

    @Override
    public String toString() {
        String catNombre = (categoria != null) ? categoria.getNombre() : "Sin Categoria";
        String ctaNombre = (cuenta != null) ? cuenta.getNombre() : "Sin Cuenta";
        return String.format("[%s] %s | ARS: $%s | USD: $%.2f | Cat: %s | Cta: %s | %s",
                getFecha(), getDescripcion(), getMonto(), montoUSD, catNombre, ctaNombre, getTipoTransaccion());
    }
}
