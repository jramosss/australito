package com.australito;

import com.australito.controller.GastoController;
import com.australito.exception.AustralitoException;
import com.australito.exception.SaldoInsuficienteException;
import com.australito.exception.ValidacionException;
import com.australito.model.Categoria;
import com.australito.model.Cotizacion;
import com.australito.model.Cuenta;
import com.australito.model.Gasto;
import com.australito.model.Ingreso;
import com.australito.model.Transaccion;
import com.australito.service.ConversionService;
import com.australito.service.ExportadorArchivoService;
import com.australito.service.GastoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GastoServiceTest {

    private GastoService gastoService;
    private ConversionService mockConversionService;
    private ExportadorArchivoService exportadorService;
    private GastoController controller;

    @BeforeEach
    void setUp() {
        mockConversionService = new ConversionService() {
            @Override
            public Cotizacion obtenerCotizacionBlue() {
                return new Cotizacion(1L, new BigDecimal("1300.00"), new BigDecimal("1350.00"), "Mock Test");
            }

            @Override
            public BigDecimal convertirARSaUSD(BigDecimal montoARS, Cotizacion cotizacion) {
                return montoARS.divide(cotizacion.getTasaVentaEfectiva(), 2, java.math.RoundingMode.HALF_EVEN);
            }
        };

        gastoService = new GastoService(mockConversionService, null, null, null, null);
        exportadorService = new ExportadorArchivoService();
        controller = new GastoController(gastoService, mockConversionService, exportadorService);
    }

    @Test
    @DisplayName("Debe registrar un gasto bimonetario correctamente y debitar la cuenta")
    void testRegistrarGastoExitoso() throws AustralitoException {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria categoria = gastoService.getCategorias().get(0);
        BigDecimal saldoInicial = cuenta.getSaldoActual();
        BigDecimal montoGasto = new BigDecimal("27000.00");

        Gasto gasto = gastoService.registrarGasto("Supermercado Coto", montoGasto, LocalDate.now(), categoria, cuenta);

        assertNotNull(gasto.getId());
        assertEquals("Supermercado Coto", gasto.getDescripcion());
        assertEquals(montoGasto, gasto.getMonto());
        assertEquals(saldoInicial.subtract(montoGasto), cuenta.getSaldoActual());

        // 27000 / 1350 = 20.00 USD
        assertEquals(new BigDecimal("20.00"), gasto.getMontoUSD());
        assertEquals("GASTO", gasto.getTipoTransaccion());
    }

    @Test
    @DisplayName("Debe lanzar SaldoInsuficienteException si el gasto supera el saldo de la cuenta")
    void testGastoSuperaSaldo() {
        Cuenta cuenta = gastoService.getCuentas().get(2);
        Categoria categoria = gastoService.getCategorias().get(0);
        BigDecimal montoExcesivo = new BigDecimal("90000.00");

        assertThrows(SaldoInsuficienteException.class, () ->
                gastoService.registrarGasto("Compra inviable", montoExcesivo, LocalDate.now(), categoria, cuenta)
        );
    }

    @Test
    @DisplayName("Debe lanzar ValidacionException si el monto es negativo o cero")
    void testMontoInvalido() {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria categoria = gastoService.getCategorias().get(0);

        assertThrows(ValidacionException.class, () ->
                gastoService.registrarGasto("Invalido", new BigDecimal("-500.00"), LocalDate.now(), categoria, cuenta)
        );
    }

    @Test
    @DisplayName("Debe ordenar los gastos por monto descendente correctamente")
    void testOrdenamientoPorMonto() throws AustralitoException {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria cat = gastoService.getCategorias().get(0);

        gastoService.registrarGasto("Gasto Menor", new BigDecimal("5000.00"), LocalDate.now(), cat, cuenta);
        gastoService.registrarGasto("Gasto Mayor", new BigDecimal("45000.00"), LocalDate.now(), cat, cuenta);
        gastoService.registrarGasto("Gasto Medio", new BigDecimal("15000.00"), LocalDate.now(), cat, cuenta);

        List<Gasto> ordenados = gastoService.listarGastosOrdenadosPorMontoDesc();

        assertEquals(new BigDecimal("45000.00"), ordenados.get(0).getMonto());
        assertEquals(new BigDecimal("15000.00"), ordenados.get(1).getMonto());
        assertEquals(new BigDecimal("5000.00"), ordenados.get(2).getMonto());
    }

    @Test
    @DisplayName("Debe buscar gastos por coincidencia parcial en descripción")
    void testBusquedaPorDescripcion() throws AustralitoException {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria cat = gastoService.getCategorias().get(0);

        gastoService.registrarGasto("Pago de Internet Fibertel", new BigDecimal("12000.00"), LocalDate.now(), cat, cuenta);
        gastoService.registrarGasto("Compra en Supermercado", new BigDecimal("25000.00"), LocalDate.now(), cat, cuenta);

        List<Gasto> encontrados = gastoService.buscarGastosPorDescripcion("internet");

        assertEquals(1, encontrados.size());
        assertEquals("Pago de Internet Fibertel", encontrados.get(0).getDescripcion());
    }

    @Test
    @DisplayName("Debe calcular el balance polimorfico entre ingresos y gastos")
    void testCalculoPolimorficoBalance() throws AustralitoException {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria cat = gastoService.getCategorias().get(0);

        Ingreso ingreso = gastoService.registrarIngreso("Honorarios freelance", new BigDecimal("100000.00"),
                LocalDate.now(), cuenta, "Cliente exterior");

        Gasto gasto = gastoService.registrarGasto("Farmacia", new BigDecimal("30000.00"),
                LocalDate.now(), cat, cuenta);

        Transaccion t1 = ingreso;
        Transaccion t2 = gasto;

        assertEquals(new BigDecimal("100000.00"), t1.calcularImpactoEnBalance());
        assertEquals(new BigDecimal("-30000.00"), t2.calcularImpactoEnBalance());

        assertEquals(new BigDecimal("70000.00"), gastoService.calcularBalanceTotalARS());
    }

    @Test
    @DisplayName("Debe exportar correctamente el historial a un archivo plano CSV (CU-07 / EFIP I)")
    void testExportacionACSV(@TempDir Path tempDir) throws Exception {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria cat = gastoService.getCategorias().get(0);

        gastoService.registrarGasto("Supermercado Semanal", new BigDecimal("35000.00"), LocalDate.now(), cat, cuenta);

        File archivoSalida = tempDir.resolve("test_export.csv").toFile();
        File generado = exportadorService.exportarGastosACSV(gastoService.listarGastos(), archivoSalida);

        assertTrue(generado.exists());
        List<String> lineas = Files.readAllLines(generado.toPath());
        assertTrue(lineas.size() >= 2);
        assertTrue(lineas.get(0).startsWith("ID,Fecha,Descripcion,Rubro,Cuenta"));
        assertTrue(lineas.get(1).contains("Supermercado Semanal"));
    }

    @Test
    @DisplayName("Debe eliminar un gasto del historial correctamente")
    void testEliminarGasto() throws AustralitoException {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria cat = gastoService.getCategorias().get(0);

        Gasto g = gastoService.registrarGasto("Gasto Temporal", new BigDecimal("8000.00"), LocalDate.now(), cat, cuenta);
        assertEquals(1, gastoService.listarGastos().size());

        boolean eliminado = gastoService.eliminarGasto(g.getId());
        assertTrue(eliminado);
        assertEquals(0, gastoService.listarGastos().size());
    }

    @Test
    @DisplayName("El controlador debe coordinar el registro y las métricas para la Vista (MVC)")
    void testControladorMVC() throws AustralitoException {
        Cuenta cuenta = controller.listarCuentas().get(0);
        Categoria cat = controller.listarCategorias().get(0);

        Gasto g = controller.registrarGasto("Compra Libreria", new BigDecimal("13500.00"), LocalDate.now(), cat, cuenta);

        assertNotNull(g);
        assertEquals(1, controller.listarGastos().size());
        assertEquals(new BigDecimal("13500.00"), controller.obtenerTotalGastosARS());
        assertEquals(new BigDecimal("10.00"), controller.obtenerTotalGastosUSD());
    }

    @Test
    @DisplayName("Debe actualizar los datos y recalcular el equivalente USD de un gasto existente")
    void testActualizarGasto() throws AustralitoException {
        Cuenta cuenta = gastoService.getCuentas().get(0);
        Categoria cat = gastoService.getCategorias().get(0);

        Gasto g = gastoService.registrarGasto("Gasto Original", new BigDecimal("13500.00"), LocalDate.now(), cat, cuenta);
        assertEquals(new BigDecimal("10.00"), g.getMontoUSD());

        g.setDescripcion("Gasto Modificado");
        g.setMonto(new BigDecimal("27000.00"));
        boolean actualizado = gastoService.actualizarGasto(g);

        assertTrue(actualizado);
        assertEquals("Gasto Modificado", g.getDescripcion());
        assertEquals(new BigDecimal("20.00"), g.getMontoUSD());
    }
}
