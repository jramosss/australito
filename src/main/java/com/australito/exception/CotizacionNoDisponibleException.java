package com.australito.exception;

public class CotizacionNoDisponibleException extends AustralitoException {
    public CotizacionNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public CotizacionNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
