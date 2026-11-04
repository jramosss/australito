package com.australito.controller;

import com.australito.dao.ConexionDB;
import com.australito.exception.AustralitoException;
import com.australito.model.Categoria;
import com.australito.model.Cotizacion;
import com.australito.model.Cuenta;
import com.australito.model.Gasto;
import com.australito.service.ConversionService;
import com.australito.service.ExportadorArchivoService;
import com.australito.service.GastoService;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class GastoController {
    private final GastoService gastoService;
    private final ConversionService conversionService;
    private final ExportadorArchivoService exportadorService;

    public GastoController(GastoService gastoService, ConversionService conversionService) {
        this(gastoService, conversionService, new ExportadorArchivoService());
    }

    public GastoController(GastoService gastoService, ConversionService conversionService,
                           ExportadorArchivoService exportadorService) {
        this.gastoService = gastoService;
        this.conversionService = conversionService;
        this.exportadorService = exportadorService;
    }

    public Gasto registrarGasto(String descripcion, BigDecimal montoARS, LocalDate fecha,
                                Categoria categoria, Cuenta cuenta) throws AustralitoException {
        return gastoService.registrarGasto(descripcion, montoARS, fecha, categoria, cuenta);
    }

    public List<Gasto> listarGastos() {
        return gastoService.listarGastosOrdenadosPorFechaDesc();
    }

    public List<Cuenta> listarCuentas() {
        return gastoService.getCuentas();
    }

    public List<Categoria> listarCategorias() {
        return gastoService.getCategorias();
    }

    public Cotizacion obtenerCotizacionVigente() throws AustralitoException {
        return conversionService.obtenerCotizacionBlue();
    }

    public BigDecimal previsualizarConversion(BigDecimal montoARS, Cotizacion cotizacion) throws AustralitoException {
        return conversionService.convertirARSaUSD(montoARS, cotizacion);
    }

    public boolean eliminarGasto(Long idGasto) throws AustralitoException {
        return gastoService.eliminarGasto(idGasto);
    }

    public boolean actualizarGasto(Gasto gasto) throws AustralitoException {
        return gastoService.actualizarGasto(gasto);
    }

    public File exportarHistorialCSV(File destino) throws AustralitoException {
        List<Gasto> lista = gastoService.listarGastos();
        return exportadorService.exportarGastosACSV(lista, destino);
    }

    public Map<String, BigDecimal> obtenerGastosPorCategoria() {
        return gastoService.calcularGastosPorCategoria();
    }

    public BigDecimal obtenerTotalGastosARS() {
        return gastoService.calcularTotalGastosARS();
    }

    public BigDecimal obtenerTotalGastosUSD() {
        return gastoService.calcularTotalGastosUSD();
    }

    public BigDecimal obtenerBalanceTotalARS() {
        return gastoService.calcularBalanceTotalARS();
    }

    public boolean verificarEstadoBaseDatos() {
        return ConexionDB.probarConexion();
    }
}
