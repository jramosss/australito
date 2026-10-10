package com.australito;

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
import com.australito.service.GastoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GastoServiceTest {

    private GastoService gastoService;
    private ConversionService mockConversionService;

    @BeforeEach
    void setUp() {
        // Mock simple de ConversionService para pruebas determinísticas
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

        gastoService = new GastoService(mockConversionService);
    }

    @Test
    @DisplayName("Debe registrar un gasto bimonetario correctamente y debitar la cuenta")
    void testRegistrarGastoExitoso() throws AustralitoException {
        Cuenta cuenta = gastoService.getCuentas().get(0); // Saldo inicial: $350.000
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
        Cuenta cuenta = gastoService.getCuentas().get(2); // Efectivo: $45.000
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

        // Registro de ingreso (+100.000)
        Ingreso ingreso = gastoService.registrarIngreso("Honorarios freelance", new BigDecimal("100000.00"),
                LocalDate.now(), cuenta, "Cliente exterior");

        // Registro de gasto (-30.000)
        Gasto gasto = gastoService.registrarGasto("Farmacia", new BigDecimal("30000.00"),
                LocalDate.now(), cat, cuenta);

        // Comprobación de polimorfismo a través de la interfaz abstracta Transaccion
        Transaccion t1 = ingreso;
        Transaccion t2 = gasto;

        assertEquals(new BigDecimal("100000.00"), t1.calcularImpactoEnBalance());
        assertEquals(new BigDecimal("-30000.00"), t2.calcularImpactoEnBalance());

        // Balance total esperado: 100000 - 30000 = +70000
        assertEquals(new BigDecimal("70000.00"), gastoService.calcularBalanceTotalARS());
    }
}
