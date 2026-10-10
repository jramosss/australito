package com.australito.view;

import com.australito.exception.AustralitoException;
import com.australito.model.Categoria;
import com.australito.model.Cotizacion;
import com.australito.model.Cuenta;
import com.australito.model.Gasto;
import com.australito.model.Ingreso;
import com.australito.service.ConversionService;
import com.australito.service.GastoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MenuConsola {
    private final GastoService gastoService;
    private final ConversionService conversionService;
    private final Scanner scanner;

    public MenuConsola(GastoService gastoService, ConversionService conversionService) {
        this.gastoService = gastoService;
        this.conversionService = conversionService;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        int opcion = -1;
        do {
            mostrarEncabezado();
            System.out.println("1. Registrar nuevo gasto bimonetario");
            System.out.println("2. Registrar ingreso de fondos");
            System.out.println("3. Listar gastos (con opciones de ordenamiento)");
            System.out.println("4. Buscar gastos (por categoría o descripción)");
            System.out.println("5. Consultar cotización Dólar Blue en tiempo real");
            System.out.println("6. Ver balances consolidados y saldos por cuenta");
            System.out.println("0. Salir del sistema");
            System.out.print("\nSeleccione una opción: ");

            try {
                String linea = scanner.nextLine();
                opcion = Integer.parseInt(linea.trim());

                switch (opcion) {
                    case 1 -> registrarGastoInteractivo();
                    case 2 -> registrarIngresoInteractivo();
                    case 3 -> listarGastosInteractivo();
                    case 4 -> buscarGastosInteractivo();
                    case 5 -> consultarCotizacionInteractivo();
                    case 6 -> verBalancesInteractivo();
                    case 0 -> System.out.println("\nCerrando sesión en Australito. ¡Hasta luego!");
                    default -> System.out.println("\n[!] Opción inválida. Ingrese un valor del 0 al 6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\n[!] Entrada incorrecta. Debe ingresar un número entero.");
            } catch (Exception e) {
                System.out.println("\n[ERROR] Ocurrió un error inesperado: " + e.getMessage());
            }

            if (opcion != 0) {
                System.out.println("\nPresione ENTER para continuar...");
                scanner.nextLine();
            }
        } while (opcion != 0);
    }

    private void mostrarEncabezado() {
        System.out.println("\n========================================================");
        System.out.println("         AUSTRALITO - GESTOR DE GASTOS BIMONETARIO      ");
        System.out.println("========================================================");
    }

    private void registrarGastoInteractivo() {
        System.out.println("\n--- REGISTRO DE NUEVO GASTO ---");
        try {
            System.out.print("Ingrese descripción del gasto (ej: Compra en Coto): ");
            String descripcion = scanner.nextLine();

            System.out.print("Ingrese el monto en ARS: $");
            BigDecimal monto = new BigDecimal(scanner.nextLine().trim());

            System.out.println("\nSeleccione la Cuenta de débito:");
            List<Cuenta> cuentas = gastoService.getCuentas();
            for (int i = 0; i < cuentas.size(); i++) {
                Cuenta c = cuentas.get(i);
                System.out.printf("  %d. %s (Saldo: $%s)%n", i + 1, c.getNombre(), c.getSaldoActual());
            }
            System.out.print("Opción de cuenta: ");
            int idxCuenta = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idxCuenta < 0 || idxCuenta >= cuentas.size()) {
                System.out.println("[!] Índice de cuenta inválido.");
                return;
            }
            Cuenta cuentaSeleccionada = cuentas.get(idxCuenta);

            System.out.println("\nSeleccione la Categoría:");
            List<Categoria> categorias = gastoService.getCategorias();
            for (int i = 0; i < categorias.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, categorias.get(i).getNombre());
            }
            System.out.print("Opción de categoría: ");
            int idxCat = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idxCat < 0 || idxCat >= categorias.size()) {
                System.out.println("[!] Índice de categoría inválido.");
                return;
            }
            Categoria categoriaSeleccionada = categorias.get(idxCat);

            Gasto gastoRegistrado = gastoService.registrarGasto(
                    descripcion, monto, LocalDate.now(), categoriaSeleccionada, cuentaSeleccionada);

            System.out.println("\n[OK] Gasto registrado con éxito:");
            System.out.printf("  ID: %d | Fecha: %s%n", gastoRegistrado.getId(), gastoRegistrado.getFecha());
            System.out.printf("  Descripción: %s%n", gastoRegistrado.getDescripcion());
            System.out.printf("  Monto ARS: $%s%n", gastoRegistrado.getMonto());
            System.out.printf("  Cotización aplicada: $%.2f (USD Blue)%n",
                    gastoRegistrado.getCotizacionAplicada().getTasaVentaEfectiva());
            System.out.printf("  Equivalencia bimonetaria: USD $%.2f%n", gastoRegistrado.getMontoUSD());
            System.out.printf("  Nuevo saldo en cuenta '%s': $%s%n",
                    cuentaSeleccionada.getNombre(), cuentaSeleccionada.getSaldoActual());

        } catch (AustralitoException e) {
            System.out.println("\n[ERROR DE NEGOCIO] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("\n[!] Formato numérico incorrecto.");
        }
    }

    private void registrarIngresoInteractivo() {
        System.out.println("\n--- REGISTRO DE INGRESO ---");
        try {
            System.out.print("Ingrese descripción del ingreso (ej: Sueldo mensual): ");
            String descripcion = scanner.nextLine();

            System.out.print("Ingrese el monto en ARS: $");
            BigDecimal monto = new BigDecimal(scanner.nextLine().trim());

            System.out.println("\nSeleccione la Cuenta de destino:");
            List<Cuenta> cuentas = gastoService.getCuentas();
            for (int i = 0; i < cuentas.size(); i++) {
                Cuenta c = cuentas.get(i);
                System.out.printf("  %d. %s (Saldo: $%s)%n", i + 1, c.getNombre(), c.getSaldoActual());
            }
            System.out.print("Opción de cuenta: ");
            int idxCuenta = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idxCuenta < 0 || idxCuenta >= cuentas.size()) {
                System.out.println("[!] Índice de cuenta inválido.");
                return;
            }
            Cuenta cuentaSeleccionada = cuentas.get(idxCuenta);

            System.out.print("Ingrese el origen de los fondos (ej: Empleador / Honorarios): ");
            String origen = scanner.nextLine();

            Ingreso ingreso = gastoService.registrarIngreso(
                    descripcion, monto, LocalDate.now(), cuentaSeleccionada, origen);

            System.out.println("\n[OK] Ingreso acreditado correctamente:");
            System.out.printf("  Monto: +$%s en %s (Nuevo Saldo: $%s)%n",
                    ingreso.getMonto(), cuentaSeleccionada.getNombre(), cuentaSeleccionada.getSaldoActual());

        } catch (AustralitoException e) {
            System.out.println("\n[ERROR DE NEGOCIO] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("\n[!] Formato numérico incorrecto.");
        }
    }

    private void listarGastosInteractivo() {
        System.out.println("\n--- LISTADO DE GASTOS REGISTRADOS ---");
        System.out.println("Criterio de ordenamiento:");
        System.out.println("  1. Por fecha más reciente");
        System.out.println("  2. Por monto en pesos (ARS) descendente");
        System.out.println("  3. Por monto en dólares (USD) descendente");
        System.out.print("Seleccione criterio: ");

        String opc = scanner.nextLine().trim();
        List<Gasto> lista;
        switch (opc) {
            case "2" -> {
                System.out.println("\n[Ordenado por Monto ARS (Descendente)]");
                lista = gastoService.listarGastosOrdenadosPorMontoDesc();
            }
            case "3" -> {
                System.out.println("\n[Ordenado por Monto USD (Descendente)]");
                lista = gastoService.listarGastosOrdenadosPorMontoUSDDesc();
            }
            default -> {
                System.out.println("\n[Ordenado por Fecha (Más reciente primero)]");
                lista = gastoService.listarGastosOrdenadosPorFechaDesc();
            }
        }

        if (lista.isEmpty()) {
            System.out.println("No hay gastos registrados aún en el sistema.");
            return;
        }

        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-10s | %-25s | %-12s | %-10s | %-18s%n",
                "ID", "Fecha", "Descripción", "Monto ARS", "Monto USD", "Categoría");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Gasto g : lista) {
            System.out.printf("%-4d | %-10s | %-25s | $%-11s | USD $%-7.2f | %-18s%n",
                    g.getId(), g.getFecha(), truncar(g.getDescripcion(), 25),
                    g.getMonto(), g.getMontoUSD(),
                    (g.getCategoria() != null ? truncar(g.getCategoria().getNombre(), 18) : "-"));
        }
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("Total acumulado: ARS $%s | USD $%.2f%n",
                gastoService.calcularTotalGastosARS(), gastoService.calcularTotalGastosUSD());
    }

    private void buscarGastosInteractivo() {
        System.out.println("\n--- BÚSQUEDA Y FILTRADO DE GASTOS ---");
        System.out.println("  1. Buscar por Categoría");
        System.out.println("  2. Buscar por texto en la Descripción");
        System.out.print("Seleccione tipo de búsqueda: ");

        String opc = scanner.nextLine().trim();
        List<Gasto> encontrados;
        if ("1".equals(opc)) {
            System.out.print("Ingrese nombre o parte de la categoría: ");
            String cat = scanner.nextLine();
            encontrados = gastoService.buscarGastosPorCategoria(cat);
        } else {
            System.out.print("Ingrese palabra clave en descripción: ");
            String desc = scanner.nextLine();
            encontrados = gastoService.buscarGastosPorDescripcion(desc);
        }

        System.out.printf("\nSe encontraron %d coincidencias:%n", encontrados.size());
        for (Gasto g : encontrados) {
            System.out.println("  " + g);
        }
    }

    private void consultarCotizacionInteractivo() {
        System.out.println("\n--- CONSULTA DE DIVISAS (DOLAR BLUE) ---");
        try {
            Cotizacion cot = conversionService.obtenerCotizacionBlue();
            System.out.println("Fuente: " + cot.getFuente());
            System.out.println("Fecha/Hora actualización: " + cot.getFechaActualizacion());
            System.out.printf("Valor Compra: $%s%n", cot.getValorCompra());
            System.out.printf("Valor Venta:  $%s%n", cot.getValorVenta());
            System.out.printf("Tasa efectiva aplicada a consumos: $%s ARS por USD%n", cot.getTasaVentaEfectiva());
        } catch (AustralitoException e) {
            System.out.println("[ERROR] No se pudo obtener la cotización: " + e.getMessage());
        }
    }

    private void verBalancesInteractivo() {
        System.out.println("\n--- BALANCE CONSOLIDADO Y SALDOS ---");
        System.out.println("Saldos por Cuenta:");
        for (Cuenta c : gastoService.getCuentas()) {
            System.out.printf("  * %-25s : $%s%n", c.getNombre(), c.getSaldoActual());
        }

        System.out.println("\nGastos agrupados por Categoría:");
        Map<String, BigDecimal> porCat = gastoService.calcularGastosPorCategoria();
        for (Map.Entry<String, BigDecimal> entry : porCat.entrySet()) {
            System.out.printf("  * %-25s : $%s%n", entry.getKey(), entry.getValue());
        }

        System.out.printf("%nBalance Neto Operacional (Ingresos - Gastos): $%s ARS%n",
                gastoService.calcularBalanceTotalARS());
        System.out.printf("Gasto Total Acumulado en Dólares: USD $%.2f%n",
                gastoService.calcularTotalGastosUSD());
    }

    private String truncar(String str, int max) {
        if (str == null) return "";
        return (str.length() <= max) ? str : str.substring(0, max - 3) + "...";
    }
}
