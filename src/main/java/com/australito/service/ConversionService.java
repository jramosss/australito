package com.australito.service;

import com.australito.exception.CotizacionNoDisponibleException;
import com.australito.exception.ValidacionException;
import com.australito.model.Cotizacion;

import java.math.BigDecimal;

public interface ConversionService {
    Cotizacion obtenerCotizacionBlue() throws CotizacionNoDisponibleException;

    BigDecimal convertirARSaUSD(BigDecimal montoARS, Cotizacion cotizacion) throws ValidacionException;
}
