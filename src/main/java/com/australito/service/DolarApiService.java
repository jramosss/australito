package com.australito.service;

import com.australito.dao.CotizacionDAO;
import com.australito.exception.CotizacionNoDisponibleException;
import com.australito.exception.ValidacionException;
import com.australito.model.Cotizacion;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DolarApiService implements ConversionService {
    private static final String API_URL = "https://dolarapi.com/v1/dolares/blue";
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private final HttpClient httpClient;
    private final CotizacionDAO cotizacionDAO;
    private Cotizacion ultimaCotizacionEnCache;

    public DolarApiService() {
        this(new CotizacionDAO());
    }

    public DolarApiService(CotizacionDAO cotizacionDAO) {
        this.cotizacionDAO = cotizacionDAO;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.ultimaCotizacionEnCache = new Cotizacion(1L, new BigDecimal("1280.00"), new BigDecimal("1310.00"), "Cache Local Contingencia");
    }

    @Override
    public Cotizacion obtenerCotizacionBlue() throws CotizacionNoDisponibleException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                BigDecimal compra = json.get("compra").getAsBigDecimal();
                BigDecimal venta = json.get("venta").getAsBigDecimal();

                Cotizacion cotizacion = new Cotizacion(null, compra, venta, "DolarAPI Oficial");
                if (cotizacionDAO != null) {
                    try {
                        Long idCot = cotizacionDAO.guardar(cotizacion);
                        if (idCot != null) {
                            cotizacion.setId(idCot);
                        }
                    } catch (Exception ignored) {
                    }
                }
                this.ultimaCotizacionEnCache = cotizacion;
                return cotizacion;
            } else {
                throw new CotizacionNoDisponibleException("Respuesta HTTP no satisfactoria: " + response.statusCode());
            }
        } catch (Exception e) {
            // Modo degradado: si falla la API externa, se busca la última cotización persistida en MySQL
            try {
                if (cotizacionDAO != null) {
                    Cotizacion persistida = cotizacionDAO.obtenerUltima();
                    if (persistida != null) {
                        this.ultimaCotizacionEnCache = persistida;
                        return persistida;
                    }
                }
            } catch (Exception ignored) {
            }

            if (ultimaCotizacionEnCache != null) {
                return ultimaCotizacionEnCache;
            }
            throw new CotizacionNoDisponibleException("No fue posible obtener la cotización cambiaria", e);
        }
    }

    @Override
    public BigDecimal convertirARSaUSD(BigDecimal montoARS, Cotizacion cotizacion) throws ValidacionException {
        if (montoARS == null || montoARS.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El monto en ARS debe ser un valor positivo");
        }
        if (cotizacion == null || cotizacion.getTasaVentaEfectiva() == null
                || cotizacion.getTasaVentaEfectiva().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("La cotizacion cambiaria no es valida para la conversion");
        }
        return montoARS.divide(cotizacion.getTasaVentaEfectiva(), 2, RoundingMode.HALF_EVEN);
    }
}
