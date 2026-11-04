package com.australito;

import com.australito.controller.GastoController;
import com.australito.service.ConversionService;
import com.australito.service.DolarApiService;
import com.australito.service.ExportadorArchivoService;
import com.australito.service.GastoService;
import com.australito.view.MenuConsola;
import com.australito.view.VistaPrincipal;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Australito - Sistema Bimonetario de Gastos Personales (Módulo 4)");

        // 1. Inicialización de componentes de persistencia, servicios y controlador
        ConversionService conversionService = new DolarApiService();
        GastoService gastoService = new GastoService(conversionService);
        ExportadorArchivoService exportadorService = new ExportadorArchivoService();
        GastoController controller = new GastoController(gastoService, conversionService, exportadorService);

        boolean forzarConsola = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                forzarConsola = true;
                break;
            }
        }

        // 2. Despacho según el entorno de ejecución
        if (forzarConsola || GraphicsEnvironment.isHeadless()) {
            System.out.println("[INFO] Entorno sin pantalla gráfica o flag --console detectado. Iniciando interfaz de consola...");
            MenuConsola menu = new MenuConsola(gastoService, conversionService);
            menu.iniciar();
            System.exit(0);
        } else {
            System.out.println("[INFO] Lanzando interfaz gráfica de escritorio (Java Swing)...");
            SwingUtilities.invokeLater(() -> {
                VistaPrincipal vista = new VistaPrincipal(controller);
                vista.setVisible(true);
            });
        }
    }
}
