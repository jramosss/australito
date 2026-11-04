package com.australito.view;

import com.australito.controller.GastoController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

public class ReporteHistoricoPanel extends JDialog {
    private final GastoController controller;
    private JTable tablaReporte;
    private DefaultTableModel tableModel;

    public ReporteHistoricoPanel(JFrame parent, GastoController controller) {
        super(parent, "Reporte Consolidado por Rubro", true);
        this.controller = controller;

        setSize(560, 420);
        setLocationRelativeTo(parent);

        inicializarComponentes();
        cargarDatos();
    }

    private void inicializarComponentes() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Distribución de Gastos por Rubro");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(24, 30, 42));
        contentPane.add(lblTitulo, BorderLayout.NORTH);

        // Estructuras complementarias: arreglo de cabeceras estático
        String[] columnas = {"Rubro / Categoría", "Total Gastado (ARS)", "Participación (%)"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaReporte = new JTable(tableModel);
        tablaReporte.setRowHeight(26);
        tablaReporte.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaReporte.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tablaReporte);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        bottomPanel.add(btnCerrar);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(contentPane);
    }

    private void cargarDatos() {
        Map<String, BigDecimal> resumen = controller.obtenerGastosPorCategoria();
        BigDecimal totalGeneral = controller.obtenerTotalGastosARS();

        tableModel.setRowCount(0);
        for (Map.Entry<String, BigDecimal> entry : resumen.entrySet()) {
            BigDecimal monto = entry.getValue();
            String porcentaje = "0.00 %";
            if (totalGeneral != null && totalGeneral.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = monto.multiply(new BigDecimal("100")).divide(totalGeneral, 2, java.math.RoundingMode.HALF_EVEN);
                porcentaje = pct.toPlainString() + " %";
            }
            // Arreglo de fila unidimensional
            Object[] fila = {entry.getKey(), "$ " + monto.toPlainString(), porcentaje};
            tableModel.addRow(fila);
        }
    }
}
