package com.australito.view;

import com.australito.controller.GastoController;
import com.australito.exception.AustralitoException;
import com.australito.model.Cotizacion;
import com.australito.model.Gasto;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VistaPrincipal extends JFrame {
    private final GastoController controller;
    private Cotizacion cotizacionActual;

    private JLabel lblCotizacionBlue;
    private JLabel lblEstadoBD;
    private JLabel lblTotalARS;
    private JLabel lblTotalUSD;
    private JLabel lblBalanceARS;
    private JTable tablaGastos;
    private DefaultTableModel tableModel;

    // Estructuras de datos complementarias: arreglo nativo estático de cabeceras
    private static final String[] CABECERAS_TABLA = {
            "ID", "Fecha", "Rubro", "Cuenta", "Descripción", "Monto (ARS)", "Monto (USD)", "TC Fijado"
    };

    public VistaPrincipal(GastoController controller) {
        super("Australito - Sistema Bimonetario de Gestión de Gastos");
        this.controller = controller;

        configurarVentana();
        inicializarComponentes();
        refrescarTodo();
    }

    private void configurarVentana() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 680);
        setMinimumSize(new Dimension(880, 540));
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 10));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        rootPanel.setBackground(new Color(248, 250, 252));

        // 1. PANEL SUPERIOR (Header & Tarjeta Cambiaria)
        rootPanel.add(crearPanelSuperior(), BorderLayout.NORTH);

        // 2. PANEL CENTRAL (Barra de herramientas y Tabla)
        rootPanel.add(crearPanelCentral(), BorderLayout.CENTER);

        // 3. PANEL INFERIOR (Tarjetas de métricas y resumen)
        rootPanel.add(crearPanelInferior(), BorderLayout.SOUTH);

        getContentPane().add(rootPanel);
    }

    private JPanel crearPanelSuperior() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setOpaque(false);

        JPanel brandingPanel = new JPanel();
        brandingPanel.setLayout(new BoxLayout(brandingPanel, BoxLayout.Y_AXIS));
        brandingPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("AUSTRALITO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitulo = new JLabel("Sistema Bimonetario de Finanzas Personales (Modelo-Vista-Controlador)");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(100, 116, 139));

        brandingPanel.add(lblTitulo);
        brandingPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        brandingPanel.add(lblSubtitulo);

        headerPanel.add(brandingPanel, BorderLayout.WEST);

        // Tarjeta informativa con Dólar Blue y estado MySQL
        JPanel statusCard = new JPanel(new GridLayout(2, 1, 4, 4));
        statusCard.setBackground(Color.WHITE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        lblCotizacionBlue = new JLabel("Cotización Dólar Blue: Cargando...");
        lblCotizacionBlue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCotizacionBlue.setForeground(new Color(13, 148, 136));

        lblEstadoBD = new JLabel("Base de Datos MySQL: Verificando...");
        lblEstadoBD.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEstadoBD.setForeground(new Color(71, 85, 105));

        statusCard.add(lblCotizacionBlue);
        statusCard.add(lblEstadoBD);

        headerPanel.add(statusCard, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel crearPanelCentral() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Barra de botones operacionales
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnNuevoGasto = new JButton("+ Registrar Consumo");
        btnNuevoGasto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNuevoGasto.setBackground(new Color(13, 148, 136));
        btnNuevoGasto.setForeground(Color.BLACK);
        btnNuevoGasto.addActionListener(e -> abrirFormularioGasto());
        toolbar.add(btnNuevoGasto);

        JButton btnExportarCSV = new JButton("Exportar Reporte (CSV)");
        btnExportarCSV.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExportarCSV.addActionListener(e -> accionarExportacionCSV());
        toolbar.add(btnExportarCSV);

        JButton btnVerReporte = new JButton("Resumen por Rubro");
        btnVerReporte.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVerReporte.addActionListener(e -> abrirReporteRubros());
        toolbar.add(btnVerReporte);

        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEliminar.addActionListener(e -> accionarEliminarGasto());
        toolbar.add(btnEliminar);

        JButton btnRefrescar = new JButton("Actualizar");
        btnRefrescar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefrescar.addActionListener(e -> refrescarTodo());
        toolbar.add(btnRefrescar);

        centerPanel.add(toolbar, BorderLayout.NORTH);

        // Tabla de datos bimonetaria
        tableModel = new DefaultTableModel(CABECERAS_TABLA, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaGastos = new JTable(tableModel);
        tablaGastos.setRowHeight(28);
        tablaGastos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaGastos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaGastos.getTableHeader().setBackground(new Color(241, 245, 249));

        // Alineación derecha para columnas numéricas monetarias
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tablaGastos.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tablaGastos.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tablaGastos.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tablaGastos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel crearPanelInferior() {
        JPanel footerPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        footerPanel.setOpaque(false);
        footerPanel.setPreferredSize(new Dimension(0, 75));

        lblTotalARS = new JLabel("$ 0.00");
        footerPanel.add(crearCardMetrica("Total Egresos (ARS)", lblTotalARS, new Color(225, 29, 72)));

        lblTotalUSD = new JLabel("u$s 0.00");
        footerPanel.add(crearCardMetrica("Total Consumos (USD Blue)", lblTotalUSD, new Color(13, 148, 136)));

        lblBalanceARS = new JLabel("$ 0.00");
        footerPanel.add(crearCardMetrica("Balance Disponible (ARS)", lblBalanceARS, new Color(30, 41, 59)));

        return footerPanel;
    }

    private JPanel crearCardMetrica(String titulo, JLabel lblValor, Color colorAcento) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTit.setForeground(new Color(100, 116, 139));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblValor.setForeground(colorAcento);

        card.add(lblTit);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(lblValor);
        return card;
    }

    public void refrescarTodo() {
        actualizarCotizacion();
        actualizarEstadoBD();
        actualizarTabla();
        actualizarMetricas();
    }

    private void actualizarCotizacion() {
        try {
            cotizacionActual = controller.obtenerCotizacionVigente();
            lblCotizacionBlue.setText(String.format("Dólar Blue: Compra $%s | Venta $%s (%s)",
                    cotizacionActual.getValorCompra(), cotizacionActual.getValorVenta(), cotizacionActual.getFuente()));
        } catch (AustralitoException e) {
            lblCotizacionBlue.setText("Dólar Blue: No disponible (" + e.getMessage() + ")");
        }
    }

    private void actualizarEstadoBD() {
        boolean conectada = controller.verificarEstadoBaseDatos();
        if (conectada) {
            lblEstadoBD.setText("Base de Datos MySQL: Conectada (australito_db)");
            lblEstadoBD.setForeground(new Color(22, 101, 52));
        } else {
            lblEstadoBD.setText("Base de Datos MySQL: Sin conexión directa (Modo local activo)");
            lblEstadoBD.setForeground(new Color(194, 65, 12));
        }
    }

    private void actualizarTabla() {
        // Uso complementario de estructuras: ArrayList de origen transformado a matriz de objetos
        List<Gasto> listaGastos = controller.listarGastos();
        Object[][] matrizDatos = new Object[listaGastos.size()][CABECERAS_TABLA.length];
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (int i = 0; i < listaGastos.size(); i++) {
            Gasto g = listaGastos.get(i);
            matrizDatos[i][0] = g.getId();
            matrizDatos[i][1] = (g.getFecha() != null) ? g.getFecha().format(df) : "";
            matrizDatos[i][2] = (g.getCategoria() != null) ? g.getCategoria().getNombre() : "Sin Rubro";
            matrizDatos[i][3] = (g.getCuenta() != null) ? g.getCuenta().getNombre() : "Sin Cuenta";
            matrizDatos[i][4] = g.getDescripcion();
            matrizDatos[i][5] = "$ " + (g.getMonto() != null ? g.getMonto().toPlainString() : "0.00");
            matrizDatos[i][6] = "u$s " + (g.getMontoUSD() != null ? g.getMontoUSD().toPlainString() : "0.00");
            matrizDatos[i][7] = (g.getCotizacionAplicada() != null && g.getCotizacionAplicada().getTasaVentaEfectiva() != null)
                    ? "$ " + g.getCotizacionAplicada().getTasaVentaEfectiva().toPlainString() : "-";
        }

        tableModel.setDataVector(matrizDatos, CABECERAS_TABLA);

        // Reaplicar renderer alineado a derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tablaGastos.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tablaGastos.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tablaGastos.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
    }

    private void actualizarMetricas() {
        BigDecimal totalArs = controller.obtenerTotalGastosARS();
        BigDecimal totalUsd = controller.obtenerTotalGastosUSD();
        BigDecimal balanceArs = controller.obtenerBalanceTotalARS();

        lblTotalARS.setText("$ " + (totalArs != null ? totalArs.toPlainString() : "0.00"));
        lblTotalUSD.setText("u$s " + (totalUsd != null ? totalUsd.toPlainString() : "0.00"));
        lblBalanceARS.setText("$ " + (balanceArs != null ? balanceArs.toPlainString() : "0.00"));
    }

    private void abrirFormularioGasto() {
        FormularioGastoDialog dialog = new FormularioGastoDialog(this, controller, cotizacionActual);
        dialog.setVisible(true);
        if (dialog.isGuardadoExitoso()) {
            refrescarTodo();
        }
    }

    private void abrirReporteRubros() {
        ReporteHistoricoPanel dialog = new ReporteHistoricoPanel(this, controller);
        dialog.setVisible(true);
    }

    private void accionarEliminarGasto() {
        int selectedRow = tablaGastos.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un gasto de la tabla para eliminar.", "Atención", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long idGasto = (Long) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el registro de gasto #" + idGasto + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.eliminarGasto(idGasto);
                refrescarTodo();
                JOptionPane.showMessageDialog(this, "Gasto eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (AustralitoException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void accionarExportacionCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Reporte de Gastos a CSV");
        fileChooser.setSelectedFile(new File("reporte_australito.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".csv")) {
                archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
            }
            try {
                File generado = controller.exportarHistorialCSV(archivo);
                JOptionPane.showMessageDialog(this,
                        "Reporte exportado exitosamente:\n" + generado.getAbsolutePath(),
                        "Exportación Completada", JOptionPane.INFORMATION_MESSAGE);
            } catch (AustralitoException e) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
