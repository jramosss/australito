package com.australito.exception;

public class AustralitoException extends Exception {
    public AustralitoException(String mensaje) {
        super(mensaje);
    }

    public AustralitoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
