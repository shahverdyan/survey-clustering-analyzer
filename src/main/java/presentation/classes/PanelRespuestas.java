package presentation.classes;

import domain.classes.model.*;
import presentation.controllers.CtrlPresentacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class PanelRespuestas extends JPanel {

    private CtrlPresentacion ctrlPresentacion;

    private JComboBox<String> selectorMisEncuestas;
    private JTable tablaRespuestas;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;

    public PanelRespuestas(CtrlPresentacion ctrlPresentacion) {
        this.ctrlPresentacion = ctrlPresentacion;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // --- 1. Panel Superior ---
        JPanel panelNorte = new JPanel(new BorderLayout());
        JPanel panelIzq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelIzq.add(new JLabel("Ver respuestas de mi encuesta:"));

        selectorMisEncuestas = new JComboBox<>();
        selectorMisEncuestas.setPreferredSize(new Dimension(250, 25));
        selectorMisEncuestas.addActionListener(e -> cargarDatosEnTabla());
        panelIzq.add(selectorMisEncuestas);

        panelNorte.add(panelIzq, BorderLayout.WEST);

        JButton btnResponder = new JButton("Responder Encuestas");
        btnResponder.setBackground(new Color(100, 149, 237));
        btnResponder.setForeground(Color.WHITE);
        btnResponder.setFont(new Font("Arial", Font.BOLD, 12));
        btnResponder.setFocusPainted(false);
        btnResponder.addActionListener(e -> abrirDialogoResponder());

        panelNorte.add(btnResponder, BorderLayout.EAST);
        add(panelNorte, BorderLayout.NORTH);

        // --- 2. Panel Central (Tabla Mejorada) ---
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacemos que no sea editable
            }
        };

        // Creamos la tabla sobrescribiendo prepareRenderer para los TOOLTIPS
        tablaRespuestas = new JTable(tableModel) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    // Si hay texto en la celda, lo mostramos en el tooltip
                    if (rowIndex >= 0 && colIndex >= 0) {
                        Object val = getValueAt(rowIndex, colIndex);
                        if (val != null && !val.toString().isEmpty()) {
                            tip = "<html><p width=\"300\">" + val.toString() + "</p></html>";
                        }
                    }
                } catch (RuntimeException e1) {
                    // Ignorar errores puntuales de renderizado
                }
                return tip;
            }
        };

        // Configuración visual
        tablaRespuestas.setRowHeight(25); // Filas un poco más altas
        tablaRespuestas.getTableHeader().setReorderingAllowed(false);

        // FUNCIONALIDAD: Doble clic para ver texto completo
        tablaRespuestas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Solo si es doble clic (para no molestar al seleccionar)
                if (evt.getClickCount() == 2) {
                    int row = tablaRespuestas.rowAtPoint(evt.getPoint());
                    int col = tablaRespuestas.columnAtPoint(evt.getPoint());

                    if (row >= 0 && col >= 0) {
                        Object valor = tablaRespuestas.getValueAt(row, col);
                        if (valor != null) {
                            verTextoCompleto(valor.toString());
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablaRespuestas);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado de Respuestas Recibidas"));

        // Scroll horizontal suave
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll, BorderLayout.CENTER);

        // --- 3. Panel Inferior ---
        JPanel panelSur = new JPanel(new BorderLayout());
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotal = new JLabel("Total respuestas: 0");
        panelInfo.add(lblTotal);

        JPanel panelVolver = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnVolver = new JButton("<< Volver al Menú");
        btnVolver.addActionListener(e -> ctrlPresentacion.cambiarVista(VistaPrincipal.VISTA_MENU));
        panelVolver.add(btnVolver);

        panelSur.add(panelInfo, BorderLayout.WEST);
        panelSur.add(panelVolver, BorderLayout.EAST);
        add(panelSur, BorderLayout.SOUTH);

        //Tooltip
        GuiUtils.asignarTooltipsGlobales(this);
    }

    public void alMostrar() {
        recargarSelector();
    }

    private void abrirDialogoResponder() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogoResponder dialogo = new DialogoResponder(parent, ctrlPresentacion);
        dialogo.setVisible(true);
        cargarDatosEnTabla();
    }

    private void recargarSelector() {
        selectorMisEncuestas.removeAllItems();
        List<String> misIds = ctrlPresentacion.obtenerIdsMisEncuestas();

        if (misIds.isEmpty()) {
            selectorMisEncuestas.addItem("No tienes encuestas creadas");
            selectorMisEncuestas.setEnabled(false);
        } else {
            selectorMisEncuestas.setEnabled(true);
            for (String id : misIds) {
                selectorMisEncuestas.addItem(id);
            }
            selectorMisEncuestas.setSelectedIndex(0);
        }
    }

    private void cargarDatosEnTabla() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        String idSeleccionado = (String) selectorMisEncuestas.getSelectedItem();
        if (idSeleccionado == null || idSeleccionado.equals("No tienes encuestas creadas")) {
            lblTotal.setText("Total respuestas: 0");
            return;
        }

        Cuestionario c = ctrlPresentacion.obtenerEncuesta(idSeleccionado);
        if (c == null) return;
        List<Pregunta> preguntas = c.getPreguntas();

        // --- Definir Columnas ---
        tableModel.addColumn("Participante");
        for (Pregunta p : preguntas) {
            tableModel.addColumn(p.getId());
        }

        // --- Lógica de Auto-ajuste de columnas ---
        // Si hay pocas columnas (ej: < 6), dejamos que la tabla ocupe todo el ancho (AUTO_RESIZE_ALL_COLUMNS)
        // Si hay muchas, activamos el scroll horizontal (AUTO_RESIZE_OFF) para que no se apiñen
        if (preguntas.size() < 6) {
            tablaRespuestas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
            tablaRespuestas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            // Ajustamos anchos mínimos para que se lea bien
            tablaRespuestas.getColumnModel().getColumn(0).setPreferredWidth(150); // Participante más ancho
            for (int i = 1; i < tablaRespuestas.getColumnCount(); i++) {
                tablaRespuestas.getColumnModel().getColumn(i).setPreferredWidth(120);
            }
        }

        // --- Obtener Datos ---
        List<Respuesta> respuestas = ctrlPresentacion.listarRespuestas(idSeleccionado);
        lblTotal.setText("Total respuestas: " + respuestas.size());

        if (respuestas.isEmpty()) return;

        for (Respuesta r : respuestas) {
            Object[] fila = new Object[preguntas.size() + 1];
            fila[0] = r.getIdParticipante();

            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta p = preguntas.get(i);
                ValorRespuesta val = r.getValores().get(p.getId());
                String textoAMostrar = "";

                if (val != null) {
                    if (val instanceof ValorRespuestaOpcionUnica && (p.getTipo() == TipoPregunta.OPCION_UNICA)) {
                        try {
                            int indice = ((ValorRespuestaOpcionUnica) val).getIndiceSeleccionado();
                            if (indice >= 0 && indice < p.getOpciones().size()) {
                                textoAMostrar = p.getOpciones().get(indice);
                            } else {
                                textoAMostrar = String.valueOf(indice);
                            }
                        } catch (Exception ex) { textoAMostrar = val.toString(); }
                    } else if (val instanceof ValorRespuestaOpcionMultiple && (p.getTipo() == TipoPregunta.OPCION_MULTIPLE)) {
                        try {
                            java.util.Collection<Integer> indices = ((ValorRespuestaOpcionMultiple) val).getIndicesSeleccionados();
                            List<String> textos = new ArrayList<>();
                            for (Integer idx : indices) {
                                if (idx >= 0 && idx < p.getOpciones().size()) {
                                    textos.add(p.getOpciones().get(idx));
                                }
                            }
                            textoAMostrar = String.join(", ", textos);
                        } catch (Exception ex) { textoAMostrar = val.toString(); }
                    } else {
                        textoAMostrar = val.toString();
                    }
                }
                fila[i + 1] = textoAMostrar;
            }
            tableModel.addRow(fila);
        }
    }

    // Método auxiliar para mostrar la ventana emergente
    private void verTextoCompleto(String texto) {
        JTextArea textArea = new JTextArea(texto);
        textArea.setRows(10);
        textArea.setColumns(50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(this, scrollPane, "Contenido Completo", JOptionPane.INFORMATION_MESSAGE);
    }
}