package com.australito.view;

import com.australito.controller.GastoController;
import com.australito.exception.AustralitoException;
import com.australito.model.Categoria;
import com.australito.model.Cotizacion;
import com.australito.model.Cuenta;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormularioGastoDialog extends JDialog {
    private final GastoController controller;
    private final Cotizacion cotizacionActual;
    private boolean guardadoExitoso = false;

    private JTextField txtDescripcion;
    private JTextField txtMontoARS;
    private JLabel lblEquivalenteUSD;
    private JComboBox<Categoria> cmbCategoria;
    private JComboBox<Cuenta> cmbCuenta;
    private JTextField txtFecha;

    public FormularioGastoDialog(JFrame parent, GastoController controller, Cotizacion cotizacionActual) {
        super(parent, "Registrar Nuevo Gasto Bimonetario", true);
        this.controller = controller;
        this.cotizacionActual = cotizacionActual;

        setSize(480, 420);
        setLocationRelativeTo(parent);
        setResizable(false);

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        // Encabezado
        JLabel lblTitulo = new JLabel("Nuevo Gasto");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(24, 30, 42));
        mainPanel.add(lblTitulo);

        BigDecimal tasa = (cotizacionActual != null) ? cotizacionActual.getTasaVentaEfectiva() : BigDecimal.ZERO;
        JLabel lblSubtitulo = new JLabel("Tasa de cambio fijada: $ " + tasa + " ARS/USD");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(100, 116, 139));
        mainPanel.add(lblSubtitulo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Formulario en cuadrícula
        JPanel formGrid = new JPanel(new GridLayout(5, 2, 10, 12));
        formGrid.setBackground(Color.WHITE);

        formGrid.add(crearEtiquetaCampo("Descripción:"));
        txtDescripcion = new JTextField();
        formGrid.add(txtDescripcion);

        formGrid.add(crearEtiquetaCampo("Monto (ARS):"));
        txtMontoARS = new JTextField();
        formGrid.add(txtMontoARS);

        formGrid.add(crearEtiquetaCampo("Equivalente (USD):"));
        lblEquivalenteUSD = new JLabel("u$s 0.00");
        lblEquivalenteUSD.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEquivalenteUSD.setForeground(new Color(13, 148, 136));
        formGrid.add(lblEquivalenteUSD);

        formGrid.add(crearEtiquetaCampo("Categoría / Rubro:"));
        cmbCategoria = new JComboBox<>();
        for (Categoria c : controller.listarCategorias()) {
            cmbCategoria.addItem(c);
        }
        cmbCategoria.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Categoria cat) {
                    setText(cat.getNombre());
                }
                return this;
            }
        });
        formGrid.add(cmbCategoria);

        formGrid.add(crearEtiquetaCampo("Cuenta de Débito:"));
        cmbCuenta = new JComboBox<>();
        for (Cuenta c : controller.listarCuentas()) {
            cmbCuenta.addItem(c);
        }
        cmbCuenta.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Cuenta cta) {
                    setText(cta.getNombre() + " (" + cta.getTipoCuenta() + ")");
                }
                return this;
            }
        });
        formGrid.add(cmbCuenta);

        mainPanel.add(formGrid);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel fechaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fechaPanel.setBackground(Color.WHITE);
        fechaPanel.add(crearEtiquetaCampo("Fecha (AAAA-MM-DD):  "));
        txtFecha = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 12);
        fechaPanel.add(txtFecha);
        mainPanel.add(fechaPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        buttonPanel.add(btnCancelar);

        JButton btnGuardar = new JButton("Confirmar Gasto");
        btnGuardar.setBackground(new Color(13, 148, 136));
        btnGuardar.setForeground(Color.BLACK);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.addActionListener(e -> accionarGuardar());
        buttonPanel.add(btnGuardar);

        mainPanel.add(buttonPanel);

        // Cálculo dinámico de conversión monetaria al tipear
        txtMontoARS.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actualizarConversion(); }
            @Override
            public void removeUpdate(DocumentEvent e) { actualizarConversion(); }
            @Override
            public void changedUpdate(DocumentEvent e) { actualizarConversion(); }
        });

        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private JLabel crearEtiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(51, 65, 85));
        return lbl;
    }

    private void actualizarConversion() {
        String texto = txtMontoARS.getText().trim();
        if (texto.isEmpty()) {
            lblEquivalenteUSD.setText("u$s 0.00");
            return;
        }
        try {
            BigDecimal montoArs = new BigDecimal(texto);
            if (montoArs.compareTo(BigDecimal.ZERO) > 0 && cotizacionActual != null) {
                BigDecimal usd = controller.previsualizarConversion(montoArs, cotizacionActual);
                lblEquivalenteUSD.setText("u$s " + usd.toPlainString());
            } else {
                lblEquivalenteUSD.setText("u$s 0.00");
            }
        } catch (Exception ex) {
            lblEquivalenteUSD.setText("u$s --");
        }
    }

    private void accionarGuardar() {
        try {
            String desc = txtDescripcion.getText();
            String montoStr = txtMontoARS.getText().trim();
            if (montoStr.isEmpty()) {
                throw new AustralitoException("Debe ingresar un monto en pesos");
            }
            BigDecimal monto = new BigDecimal(montoStr);

            LocalDate fecha = LocalDate.now();
            try {
                if (!txtFecha.getText().trim().isEmpty()) {
                    fecha = LocalDate.parse(txtFecha.getText().trim());
                }
            } catch (Exception e) {
                throw new AustralitoException("Formato de fecha inválido. Utilice AAAA-MM-DD");
            }

            Categoria cat = (Categoria) cmbCategoria.getSelectedItem();
            Cuenta cta = (Cuenta) cmbCuenta.getSelectedItem();

            controller.registrarGasto(desc, monto, fecha, cat, cta);
            guardadoExitoso = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número decimal válido.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (AustralitoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al Registrar Gasto", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isGuardadoExitoso() {
        return guardadoExitoso;
    }
}
