package com.australito.service;

import com.australito.exception.AustralitoException;
import com.australito.exception.SaldoInsuficienteException;
import com.australito.exception.ValidacionException;
import com.australito.model.Categoria;
import com.australito.model.Cotizacion;
import com.australito.model.Cuenta;
import com.australito.model.Gasto;
import com.australito.model.Ingreso;
import com.australito.model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GastoService {
    private final List<Transaccion> transacciones;
    private final List<Cuenta> cuentas;
    private final List<Categoria> categorias;
    private final ConversionService conversionService;
    private long secuenciadorId;

    public GastoService(ConversionService conversionService) {
        this.conversionService = conversionService;
        this.transacciones = new ArrayList<>();
        this.cuentas = new ArrayList<>();
        this.categorias = new ArrayList<>();
        this.secuenciadorId = 1L;
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        // Cuentas predeterminadas
        cuentas.add(new Cuenta(1L, "Caja de Ahorro Santander", "Bancaria", new BigDecimal("350000.00")));
        cuentas.add(new Cuenta(2L, "Billetera Mercado Pago", "Virtual", new BigDecimal("120000.00")));
        cuentas.add(new Cuenta(3L, "Efectivo Billetera", "Efectivo", new BigDecimal("45000.00")));

        // Categorías predeterminadas
        categorias.add(new Categoria(1L, "Supermercado y Alimentos", new BigDecimal("180000.00")));
        categorias.add(new Categoria(2L, "Transporte y Movilidad", new BigDecimal("40000.00")));
        categorias.add(new Categoria(3L, "Servicios e Internet", new BigDecimal("65000.00")));
        categorias.add(new Categoria(4L, "Salidas y Gastos Hormiga", new BigDecimal("50000.00")));
        categorias.add(new Categoria(5L, "Salud y Farmacia", new BigDecimal("30000.00")));
    }

    public Gasto registrarGasto(String descripcion, BigDecimal montoARS, LocalDate fecha,
                                Categoria categoria, Cuenta cuenta) throws AustralitoException {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ValidacionException("La descripción del gasto no puede estar vacía");
        }
        if (montoARS == null || montoARS.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El monto en ARS debe ser estrictamente positivo");
        }
        if (categoria == null) {
            throw new ValidacionException("Debe seleccionar una categoría válida");
        }
        if (cuenta == null) {
            throw new ValidacionException("Debe seleccionar una cuenta de débito");
        }
        if (cuenta.getSaldoActual().compareTo(montoARS) < 0) {
            throw new SaldoInsuficienteException(String.format(
                    "Saldo insuficiente en la cuenta '%s'. Saldo disponible: $%s, Monto requerido: $%s",
                    cuenta.getNombre(), cuenta.getSaldoActual(), montoARS));
        }

        Cotizacion cotizacion = conversionService.obtenerCotizacionBlue();

        Gasto gasto = new Gasto(secuenciadorId++, fecha, descripcion.trim(), montoARS, categoria, cuenta, cotizacion);

        cuenta.debitar(montoARS);
        transacciones.add(gasto);
        return gasto;
    }

    public Ingreso registrarIngreso(String descripcion, BigDecimal montoARS, LocalDate fecha,
                                    Cuenta cuenta, String origen) throws AustralitoException {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new ValidacionException("La descripción del ingreso no puede estar vacía");
        }
        if (montoARS == null || montoARS.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El monto del ingreso debe ser positivo");
        }
        if (cuenta == null) {
            throw new ValidacionException("Debe seleccionar la cuenta de acreditación");
        }

        Ingreso ingreso = new Ingreso(secuenciadorId++, fecha, descripcion.trim(), montoARS, cuenta, origen);
        cuenta.acreditar(montoARS);
        transacciones.add(ingreso);
        return ingreso;
    }

    public List<Gasto> listarGastos() {
        List<Gasto> gastos = new ArrayList<>();
        for (Transaccion t : transacciones) {
            if (t instanceof Gasto) {
                gastos.add((Gasto) t);
            }
        }
        return gastos;
    }

    public List<Gasto> listarGastosOrdenadosPorFechaDesc() {
        List<Gasto> lista = listarGastos();
        lista.sort(Comparator.comparing(Gasto::getFecha).reversed());
        return lista;
    }

    public List<Gasto> listarGastosOrdenadosPorMontoDesc() {
        List<Gasto> lista = listarGastos();
        lista.sort(Comparator.comparing(Gasto::getMonto).reversed());
        return lista;
    }

    public List<Gasto> listarGastosOrdenadosPorMontoUSDDesc() {
        List<Gasto> lista = listarGastos();
        lista.sort(Comparator.comparing(Gasto::getMontoUSD).reversed());
        return lista;
    }

    public List<Gasto> buscarGastosPorCategoria(String nombreCategoria) {
        List<Gasto> resultado = new ArrayList<>();
        if (nombreCategoria == null) return resultado;
        String query = nombreCategoria.trim().toLowerCase();
        for (Gasto g : listarGastos()) {
            if (g.getCategoria() != null && g.getCategoria().getNombre().toLowerCase().contains(query)) {
                resultado.add(g);
            }
        }
        return resultado;
    }

    public List<Gasto> buscarGastosPorDescripcion(String texto) {
        List<Gasto> resultado = new ArrayList<>();
        if (texto == null) return resultado;
        String query = texto.trim().toLowerCase();
        for (Gasto g : listarGastos()) {
            if (g.getDescripcion().toLowerCase().contains(query)) {
                resultado.add(g);
            }
        }
        return resultado;
    }

    public List<Gasto> buscarGastosPorRangoFecha(LocalDate inicio, LocalDate fin) {
        List<Gasto> resultado = new ArrayList<>();
        for (Gasto g : listarGastos()) {
            boolean despuesOIgual = (inicio == null) || !g.getFecha().isBefore(inicio);
            boolean antesOIgual = (fin == null) || !g.getFecha().isAfter(fin);
            if (despuesOIgual && antesOIgual) {
                resultado.add(g);
            }
        }
        return resultado;
    }

    public BigDecimal calcularBalanceTotalARS() {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaccion t : transacciones) {
            balance = balance.add(t.calcularImpactoEnBalance());
        }
        return balance;
    }

    public BigDecimal calcularTotalGastosARS() {
        BigDecimal total = BigDecimal.ZERO;
        for (Gasto g : listarGastos()) {
            total = total.add(g.getMonto());
        }
        return total;
    }

    public BigDecimal calcularTotalGastosUSD() {
        BigDecimal total = BigDecimal.ZERO;
        for (Gasto g : listarGastos()) {
            total = total.add(g.getMontoUSD());
        }
        return total;
    }

    public Map<String, BigDecimal> calcularGastosPorCategoria() {
        Map<String, BigDecimal> resumen = new HashMap<>();
        for (Gasto g : listarGastos()) {
            String cat = (g.getCategoria() != null) ? g.getCategoria().getNombre() : "Sin Categoria";
            BigDecimal actual = resumen.getOrDefault(cat, BigDecimal.ZERO);
            resumen.put(cat, actual.add(g.getMonto()));
        }
        return resumen;
    }

    public List<Transaccion> getTransacciones() {
        return Collections.unmodifiableList(transacciones);
    }

    public List<Cuenta> getCuentas() {
        return cuentas;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public Cuenta buscarCuentaPorId(Long id) {
        for (Cuenta c : cuentas) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public Categoria buscarCategoriaPorId(Long id) {
        for (Categoria c : categorias) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }
}
