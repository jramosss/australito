package com.australito.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cotizacion {
    private Long id;
    private String monedaOrigen;
    private String monedaDestino;
    private BigDecimal valorCompra;
    private BigDecimal valorVenta;
    private LocalDateTime fechaActualizacion;
    private String fuente;

    public Cotizacion() {
        this.monedaOrigen = "USD";
        this.monedaDestino = "ARS";
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Cotizacion(Long id, BigDecimal valorCompra, BigDecimal valorVenta, String fuente) {
        this.id = id;
        this.monedaOrigen = "USD";
        this.monedaDestino = "ARS";
        this.valorCompra = valorCompra;
        this.valorVenta = valorVenta;
        this.fechaActualizacion = LocalDateTime.now();
        this.fuente = fuente;
    }

    public BigDecimal getTasaVentaEfectiva() {
        return (valorVenta != null && valorVenta.compareTo(BigDecimal.ZERO) > 0) ? valorVenta : valorCompra;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonedaOrigen() {
        return monedaOrigen;
    }

    public void setMonedaOrigen(String monedaOrigen) {
        this.monedaOrigen = monedaOrigen;
    }

    public String getMonedaDestino() {
        return monedaDestino;
    }

    public void setMonedaDestino(String monedaDestino) {
        this.monedaDestino = monedaDestino;
    }

    public BigDecimal getValorCompra() {
        return valorCompra;
    }

    public void setValorCompra(BigDecimal valorCompra) {
        this.valorCompra = valorCompra;
    }

    public BigDecimal getValorVenta() {
        return valorVenta;
    }

    public void setValorVenta(BigDecimal valorVenta) {
        this.valorVenta = valorVenta;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    @Override
    public String toString() {
        return "USD Blue [" + fuente + "] - Compra: $" + valorCompra + " | Venta: $" + valorVenta;
    }
}
