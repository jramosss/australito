package com.australito.service;

import com.australito.exception.AustralitoException;
import com.australito.model.Gasto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExportadorArchivoService {
    private static final String SEPARADOR = ",";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public File exportarGastosACSV(List<Gasto> gastos, File archivoDestino) throws AustralitoException {
        if (archivoDestino == null) {
            String nombreSugerido = "reporte_gastos_" + LocalDate.now().format(FORMATO_FECHA) + ".csv";
            archivoDestino = new File(nombreSugerido);
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDestino), StandardCharsets.UTF_8))) {

            writer.write("ID,Fecha,Descripcion,Rubro,Cuenta,Monto_ARS,Monto_USD,Tipo_Cambio_Fijado");
            writer.newLine();

            if (gastos != null) {
                for (Gasto g : gastos) {
                    String rubro = (g.getCategoria() != null) ? escapar(g.getCategoria().getNombre()) : "Sin Categoria";
                    String cuenta = (g.getCuenta() != null) ? escapar(g.getCuenta().getNombre()) : "Sin Cuenta";
                    String descripcion = escapar(g.getDescripcion());
                    String fecha = (g.getFecha() != null) ? g.getFecha().format(FORMATO_FECHA) : "";
                    String montoArs = (g.getMonto() != null) ? g.getMonto().toPlainString() : "0.00";
                    String montoUsd = (g.getMontoUSD() != null) ? g.getMontoUSD().toPlainString() : "0.00";
                    String tc = (g.getCotizacionAplicada() != null && g.getCotizacionAplicada().getTasaVentaEfectiva() != null)
                            ? g.getCotizacionAplicada().getTasaVentaEfectiva().toPlainString() : "0.00";

                    String linea = String.join(SEPARADOR,
                            String.valueOf(g.getId()),
                            fecha,
                            descripcion,
                            rubro,
                            cuenta,
                            montoArs,
                            montoUsd,
                            tc
                    );
                    writer.write(linea);
                    writer.newLine();
                }
            }
            writer.flush();
            return archivoDestino;
        } catch (IOException e) {
            throw new AustralitoException("Error de entrada/salida al exportar archivo CSV de gastos: " + e.getMessage(), e);
        }
    }

    public File exportarResumenRubrosCSV(Map<String, java.math.BigDecimal> resumen, File archivoDestino) throws AustralitoException {
        if (archivoDestino == null) {
            archivoDestino = new File("resumen_rubros_" + LocalDate.now().format(FORMATO_FECHA) + ".csv");
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDestino), StandardCharsets.UTF_8))) {

            writer.write("Rubro,Total_ARS");
            writer.newLine();

            if (resumen != null) {
                for (Map.Entry<String, java.math.BigDecimal> entry : resumen.entrySet()) {
                    String rubro = escapar(entry.getKey());
                    String total = (entry.getValue() != null) ? entry.getValue().toPlainString() : "0.00";
                    writer.write(rubro + SEPARADOR + total);
                    writer.newLine();
                }
            }
            writer.flush();
            return archivoDestino;
        } catch (IOException e) {
            throw new AustralitoException("Error al exportar el resumen por rubros: " + e.getMessage(), e);
        }
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
