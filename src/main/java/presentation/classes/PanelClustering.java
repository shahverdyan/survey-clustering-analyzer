package presentation.classes;

import domain.classes.clustering.ClusterResult;
import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.Respuesta;
import domain.classes.model.TipoPregunta;
import domain.classes.model.ValorRespuesta;
import domain.classes.model.ValorRespuestaOpcionMultiple;
import domain.classes.model.ValorRespuestaOpcionUnica;
import domain.classes.vector.Vectorizer;
import presentation.controllers.CtrlPresentacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PanelClustering extends JPanel {

    private CtrlPresentacion ctrlPresentacion;
    private JComboBox<String> comboEncuestas;
    private JComboBox<String> comboAlgoritmo;
    private JSpinner spinK;
    private JButton btnEjecutar;
    private JPanel panelMetricas;
    private JLabel lblResumenMetricas;
    private JTable tablaResultados;
    private DefaultTableModel tableModel;
    private JButton btnExportarRes;
    private JButton btnAccuracy;
    private JButton btnGrafica;


    // Colores paleta para clusters (hasta k=5)
    private static final Color[] CLUSTER_SERIES = new Color[] {
            new Color(0, 114, 178),   // azul
            new Color(230, 159, 0),   // naranja
            new Color(0, 158, 115),   // verde
            new Color(204, 121, 167), // morado
            new Color(213, 94, 0)     // rojo/vermillion
    };

    private static final Color[] CLUSTER_ROW_BG = new Color[] {
            pastel(CLUSTER_SERIES[0]),
            pastel(CLUSTER_SERIES[1]),
            pastel(CLUSTER_SERIES[2]),
            pastel(CLUSTER_SERIES[3]),
            pastel(CLUSTER_SERIES[4])
    };

    private static Color pastel(Color c) {
        int r = (int)(c.getRed()   * 0.18 + 255 * 0.82);
        int g = (int)(c.getGreen() * 0.18 + 255 * 0.82);
        int b = (int)(c.getBlue()  * 0.18 + 255 * 0.82);
        return new Color(r, g, b);
    }

    // Estado de coloreado
    private boolean colorearPorCluster = false;
    private int clusterColIndex = -1;

    public PanelClustering(CtrlPresentacion ctrlPresentacion) {
        this.ctrlPresentacion = ctrlPresentacion;
        inicializarComponentes();
    }

    public void alMostrar() {
        recargarComboEncuestas();
        lblResumenMetricas.setText("Ejecuta un algoritmo para ver los resultados.");
        lblResumenMetricas.setForeground(new Color(52, 58, 64));
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        actualizarEstadoBotones();
    }

    private void inicializarComponentes() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Clustering y Análisis", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(255, 140, 0));
        this.add(titulo, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));

        JPanel panelConfig = new JPanel(new GridLayout(3, 2, 10, 10));
        panelConfig.setBorder(new TitledBorder("Configuración del Algoritmo"));

        panelConfig.add(new JLabel("Seleccionar Encuesta:"));
        comboEncuestas = new JComboBox<>();
        comboEncuestas.addActionListener(e -> actualizarEstadoBotones());
        panelConfig.add(comboEncuestas);

        panelConfig.add(new JLabel("Algoritmo:"));
        String[] algos = {"K-Means", "K-Means++", "K-Medoids"};
        comboAlgoritmo = new JComboBox<>(algos);
        panelConfig.add(comboAlgoritmo);

        panelConfig.add(new JLabel("Número de Clusters (k):"));
        JPanel panelK = new JPanel(new BorderLayout(5, 0));
        spinK = new JSpinner(new SpinnerNumberModel(3, 2, 100, 1));
        panelK.add(spinK, BorderLayout.CENTER);

        btnEjecutar = new JButton("EJECUTAR");
        btnEjecutar.setBackground(new Color(255, 228, 181));
        btnEjecutar.setFont(new Font("Arial", Font.BOLD, 12));
        btnEjecutar.addActionListener(e -> ejecutarAlgoritmo());
        panelK.add(btnEjecutar, BorderLayout.EAST);

        panelConfig.add(panelK);
        panelCentral.add(panelConfig, BorderLayout.NORTH);

        JPanel panelResultados = new JPanel(new BorderLayout(5, 5));
        panelResultados.setBorder(new TitledBorder("Resultados"));

        // Panel métricas
        panelMetricas = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelMetricas.setBackground(new Color(248, 249, 251));
        panelMetricas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(225, 229, 234)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        lblResumenMetricas = new JLabel("Selecciona parámetros y ejecuta para ver métricas.");
        lblResumenMetricas.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblResumenMetricas.setForeground(new Color(52, 58, 64)); // gris oscuro (no verde)
        panelMetricas.add(lblResumenMetricas);

        panelResultados.add(panelMetricas, BorderLayout.NORTH);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // TABLA CON TOOLTIP EN CELDAS
        tablaResultados = new JTable(tableModel) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    if (rowIndex >= 0 && colIndex >= 0) {
                        Object val = getValueAt(rowIndex, colIndex);
                        if (val != null) tip = val.toString();
                    }
                } catch (RuntimeException e1) { }
                return tip;
            }
        };

        //  Estilo de tabla
        tablaResultados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaResultados.setRowHeight(26);
        tablaResultados.setShowGrid(false);
        tablaResultados.setIntercellSpacing(new Dimension(0, 0));
        tablaResultados.setSelectionBackground(new Color(220, 229, 255));
        tablaResultados.setSelectionForeground(new Color(20, 20, 20));

        // Cabecera oscura
        JTableHeader header = new JTableHeader(tablaResultados.getColumnModel()) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int col = columnAtPoint(e.getPoint());
                if (col < 0) return null;
                Object hv = getColumnModel().getColumn(col).getHeaderValue();
                return (hv == null) ? null : hv.toString();
            }
        };
        tablaResultados.setTableHeader(header);

        header.setOpaque(true);
        header.setBackground(new Color(33, 37, 41));
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD));


        // Renderer de filas por cluster
        tablaResultados.setDefaultRenderer(Object.class, new ClusterRowRenderer());

        tablaResultados.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tablaResultados.rowAtPoint(evt.getPoint());
                    int col = tablaResultados.columnAtPoint(evt.getPoint());
                    if (row >= 0 && col >= 0) {
                        Object valor = tablaResultados.getValueAt(row, col);
                        if (valor != null) verTextoCompleto(valor.toString());
                    }
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        panelResultados.add(scrollTabla, BorderLayout.CENTER);
        panelCentral.add(panelResultados, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnGrafica = new JButton("Ver Gráfica 2D");
        btnGrafica.addActionListener(e -> mostrarGrafica());
        btnGrafica.setEnabled(false);

        btnExportarRes = new JButton("Exportar CSV");
        btnExportarRes.addActionListener(e -> exportarResultado());
        btnExportarRes.setEnabled(false);

        btnAccuracy = new JButton("Calcular Accuracy");
        btnAccuracy.addActionListener(e -> calcularAccuracy());
        btnAccuracy.setEnabled(false);

        panelBotones.add(btnGrafica);
        panelBotones.add(btnExportarRes);
        panelBotones.add(btnAccuracy);

        panelCentral.add(panelBotones, BorderLayout.SOUTH);
        this.add(panelCentral, BorderLayout.CENTER);

        JButton btnVolver = new JButton("<< Volver al Menú Principal");
        btnVolver.addActionListener(e -> ctrlPresentacion.cambiarVista(VistaPrincipal.VISTA_MENU));
        JPanel panelSur = new JPanel();
        panelSur.add(btnVolver);
        this.add(panelSur, BorderLayout.SOUTH);

        // --- TOOLTIPS GLOBALES PARA BOTONES Y LABELS ---
        GuiUtils.asignarTooltipsGlobales(this);
    }

    // Renderer: colorea TODA la fila según cluster (solo si k <= 5)
    private class ClusterRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) return c;

            c.setBackground(Color.WHITE);
            c.setForeground(new Color(25, 28, 31));

            if (!colorearPorCluster || clusterColIndex < 0) return c;

            Object clusterVal = table.getValueAt(row, clusterColIndex);
            int id = parseClusterId(clusterVal);

            if (id >= 0 && id < CLUSTER_ROW_BG.length) {
                c.setBackground(CLUSTER_ROW_BG[id]);
                c.setForeground(new Color(20, 20, 20));
            }

            return c;
        }

        private int parseClusterId(Object clusterVal) {
            if (clusterVal == null) return -1;
            String s = clusterVal.toString().trim();
            if (s.equals("-")) return -1;

            String lower = s.toLowerCase(Locale.ROOT);
            if (lower.startsWith("cluster")) {
                int lastSpace = s.lastIndexOf(' ');
                if (lastSpace >= 0 && lastSpace < s.length() - 1) {
                    s = s.substring(lastSpace + 1).trim();
                }
            }

            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { return -1; }
        }
    }

    private void recargarComboEncuestas() {
        comboEncuestas.removeAllItems();
        for (String id : ctrlPresentacion.obtenerIdsEncuestas()) {
            comboEncuestas.addItem(id);
        }
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        String idSeleccionado = (String) comboEncuestas.getSelectedItem();
        String idUltimaEjecucion = ctrlPresentacion.getCtrlDominio().getUltimoResultadoEncuestaId();
        boolean resultadosDisponibles = (idSeleccionado != null
                && idUltimaEjecucion != null
                && idSeleccionado.equals(idUltimaEjecucion));

        if (resultadosDisponibles) {
            btnExportarRes.setEnabled(true);
            btnExportarRes.setToolTipText("Guardar los resultados actuales en un archivo CSV.");

            btnAccuracy.setEnabled(true);
            btnAccuracy.setToolTipText("Calcular la precisión comparando con etiquetas reales.");
        } else {
            btnExportarRes.setEnabled(false);
            btnExportarRes.setToolTipText("Deshabilitado: Debes pulsar 'EJECUTAR' primero.");

            btnAccuracy.setEnabled(false);
            btnAccuracy.setToolTipText("Deshabilitado: Debes pulsar 'EJECUTAR' primero.");
        }

        if (idSeleccionado == null) {
            btnGrafica.setEnabled(false);
            btnGrafica.setToolTipText("Selecciona una encuesta.");
            return;
        }

        Cuestionario c = ctrlPresentacion.obtenerEncuesta(idSeleccionado);
        if (c == null) return;

        long numNumericas = c.getPreguntas().stream().filter(p -> p.getTipo() == TipoPregunta.NUMERICA).count();
        long numTexto = c.getPreguntas().stream().filter(p -> p.getTipo() == TipoPregunta.TEXTO).count();
        int total = c.getPreguntas().size();

        boolean estructuraValida = (total == 3 && numNumericas == 2 && numTexto == 1);

        if (!estructuraValida) {
            btnGrafica.setEnabled(false);
            btnGrafica.setToolTipText("Deshabilitado: Requiere 1 Pregunta Texto (ID) y 2 Numéricas.");
        } else if (!resultadosDisponibles) {
            btnGrafica.setEnabled(false);
            btnGrafica.setToolTipText("Deshabilitado: Debes pulsar 'EJECUTAR' primero.");
        } else {
            btnGrafica.setEnabled(true);
            btnGrafica.setToolTipText("Ver gráfica de dispersión 2D");
        }
    }

    private void ejecutarAlgoritmo() {
        String id = (String) comboEncuestas.getSelectedItem();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una encuesta.");
            return;
        }
        String algo = (String) comboAlgoritmo.getSelectedItem();
        int k = (Integer) spinK.getValue();

        lblResumenMetricas.setText("Ejecutando " + algo + "... Por favor espere...");
        lblResumenMetricas.setForeground(new Color(73, 80, 87)); // gris pro

        Timer t = new Timer(100, ev -> {
            try {
                ctrlPresentacion.ejecutarClustering(id, algo, k);
                ClusterResult res = ctrlPresentacion.getCtrlDominio().getUltimoResultado();
                String resumen = String.format(
                        "Algoritmo: %s | Clusters (k): %d | SSE: %.4f | Silhouette: %.4f | Tiempo: %d ms",
                        res.algorithm(), k,
                        ctrlPresentacion.getCtrlDominio().getUltimoSse(),
                        ctrlPresentacion.getCtrlDominio().getUltimoSilhouette(),
                        ctrlPresentacion.getCtrlDominio().getUltimoTiempoMs()
                );

                lblResumenMetricas.setText(resumen);
                lblResumenMetricas.setForeground(CLUSTER_SERIES[0]); // azul elegante (no verde)

                cargarTablaResultados(id, res);
                actualizarEstadoBotones();
                JOptionPane.showMessageDialog(this, "Clustering finalizado con éxito.");
            } catch (Exception ex) {
                lblResumenMetricas.setText("Error: " + ex.getMessage());
                lblResumenMetricas.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                actualizarEstadoBotones();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void cargarTablaResultados(String idEncuesta, ClusterResult res) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        Cuestionario c = ctrlPresentacion.obtenerEncuesta(idEncuesta);
        List<Respuesta> respuestas = ctrlPresentacion.listarRespuestas(idEncuesta);
        List<Pregunta> preguntas = c.getPreguntas();

        tableModel.addColumn("Participante");
        for (Pregunta p : preguntas) tableModel.addColumn(p.getEnunciado());
        tableModel.addColumn("CLUSTER");
        tableModel.addColumn("CENTROIDE");

        tablaResultados.getColumnModel().getColumn(0).setPreferredWidth(100);

        for (Respuesta r : respuestas) {
            Object[] fila = new Object[preguntas.size() + 3];
            fila[0] = r.getIdParticipante();
            for (int i = 0; i < preguntas.size(); i++) {
                fila[i + 1] = formatearValor(r.getValores().get(preguntas.get(i).getId()), preguntas.get(i));
            }
            Integer clusterId = res.assignments().get(r.getIdParticipante());
            if (clusterId != null) {
                fila[preguntas.size() + 1] = "Cluster " + clusterId;
                if (clusterId < res.representatives().size()) {
                    fila[preguntas.size() + 2] = res.representatives().get(clusterId).id();
                } else {
                    fila[preguntas.size() + 2] = "N/A";
                }
            } else {
                fila[preguntas.size() + 1] = "-";
                fila[preguntas.size() + 2] = "-";
            }
            tableModel.addRow(fila);
        }

        // ===== Activar colores SOLO si k <= 5 =====
        clusterColIndex = tableModel.findColumn("CLUSTER");
        int k = (res == null || res.representatives() == null) ? 0 : res.representatives().size();
        colorearPorCluster = (k > 0 && k <= 5);

        // Detalle pro: columna CLUSTER en negrita
        int idxCluster = tableModel.findColumn("CLUSTER");
        if (idxCluster >= 0) {
            TableColumn col = tablaResultados.getColumnModel().getColumn(idxCluster);
            TableCellRenderer base = tablaResultados.getDefaultRenderer(Object.class);
            col.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column) {
                    Component c = ((DefaultTableCellRenderer) base)
                            .getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    return c;
                }
            });
        }

        tablaResultados.repaint();
    }

    private String formatearValor(ValorRespuesta val, Pregunta p) {
        if (val == null) return "";
        try {
            if (p.getTipo() == TipoPregunta.OPCION_UNICA && val instanceof ValorRespuestaOpcionUnica) {
                int idx = ((ValorRespuestaOpcionUnica) val).getIndiceSeleccionado();
                if (idx >= 0 && idx < p.getOpciones().size()) return p.getOpciones().get(idx);
            }
            else if (p.getTipo() == TipoPregunta.OPCION_MULTIPLE && val instanceof ValorRespuestaOpcionMultiple) {
                List<Integer> indices = ((ValorRespuestaOpcionMultiple) val).getIndicesSeleccionados();
                List<String> textos = new ArrayList<>();
                for (int idx : indices) {
                    if (idx >= 0 && idx < p.getOpciones().size()) textos.add(p.getOpciones().get(idx));
                }
                return String.join(", ", textos);
            }
        } catch (Exception e) { return val.toString(); }
        return val.toString();
    }

    private void exportarResultado() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("clusters.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ctrlPresentacion.exportarResultadoClustering(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void calcularAccuracy() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecciona CSV de etiquetas reales");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                double acc = ctrlPresentacion.calcularAccuracy(fc.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, String.format("Accuracy: %.2f%%", acc * 100));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void mostrarGrafica() {
        String idEncuesta = (String) comboEncuestas.getSelectedItem();
        Cuestionario c = ctrlPresentacion.getCtrlDominio().obtener(idEncuesta);

        List<Integer> indicesNumericos = new ArrayList<>();
        List<String> nombresEjes = new ArrayList<>();

        int vectorIndex = 0;
        for(int i=1; i<c.getPreguntas().size(); i++) {
            Pregunta p = c.getPreguntas().get(i);
            if (p.getTipo() == TipoPregunta.NUMERICA) {
                indicesNumericos.add(vectorIndex);
                nombresEjes.add(p.getEnunciado());
                vectorIndex++;
            } else {
                List<String> opts = p.getOpciones();
                int dims = (opts == null || opts.isEmpty()) ? 1 : opts.size();
                vectorIndex += Math.max(1, dims);
            }
        }

        if (indicesNumericos.size() != 2) {
            JOptionPane.showMessageDialog(this, "Error: No se detectaron las 2 dimensiones numéricas correctamente.");
            return;
        }

        List<Vectorizer.Point> puntos = ctrlPresentacion.getPuntosUltimoClustering();
        ClusterResult res = ctrlPresentacion.getCtrlDominio().getUltimoResultado();

        DialogoGrafica dialogo = new DialogoGrafica(SwingUtilities.getWindowAncestor(this),
                nombresEjes.get(0), nombresEjes.get(1),
                indicesNumericos.get(0), indicesNumericos.get(1),
                puntos, res.assignments(), res.representatives());
        dialogo.setVisible(true);
    }

    private void verTextoCompleto(String texto) {
        JTextArea textArea = new JTextArea(texto);
        textArea.setRows(10); textArea.setColumns(50);
        textArea.setLineWrap(true); textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "Detalle Completo", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- GRÁFICA ---
    private static class DialogoGrafica extends JDialog {
        private final List<Vectorizer.Point> puntos;
        private final Map<String, Integer> assignments;
        private final List<Vectorizer.Point> centros;
        private final String titleX, titleY;
        private final int idxX, idxY;

        public DialogoGrafica(Window owner, String titleX, String titleY, int idxX, int idxY,
                              List<Vectorizer.Point> puntos, Map<String, Integer> assignments,
                              List<Vectorizer.Point> centros) {
            super(owner, "Visualización de Clusters", ModalityType.MODELESS);
            this.titleX = titleX; this.titleY = titleY;
            this.idxX = idxX; this.idxY = idxY;
            this.puntos = puntos;
            this.assignments = assignments;
            this.centros = centros;

            setSize(500, 500);
            setLocationRelativeTo(null);

            JPanel panelDibujo = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (puntos == null || puntos.isEmpty()) return;

                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    int padding = 60;

                    // Ejes
                    g2.setColor(Color.BLACK);
                    g2.drawLine(padding, h - padding, w - padding, h - padding);
                    g2.drawLine(padding, padding, padding, h - padding);

                    g2.setFont(new Font("Arial", Font.BOLD, 14));
                    g2.drawString(titleX, w / 2 - 20, h - 20);

                    g2.rotate(-Math.PI / 2);
                    g2.drawString(titleY, -h / 2 - 20, 30);
                    g2.rotate(Math.PI / 2);

                    double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
                    double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

                    List<Vectorizer.Point> todos = new ArrayList<>(puntos);
                    if(centros != null) todos.addAll(centros);

                    for (Vectorizer.Point p : todos) {
                        if (idxX < p.values().length && idxY < p.values().length) {
                            double valX = p.values()[idxX];
                            double valY = p.values()[idxY];
                            if (valX < minX) minX = valX;
                            if (valX > maxX) maxX = valX;
                            if (valY < minY) minY = valY;
                            if (valY > maxY) maxY = valY;
                        }
                    }

                    if (maxX == minX) { maxX += 0.5; minX -= 0.5; }
                    if (maxY == minY) { maxY += 0.5; minY -= 0.5; }

                    double rangeX = maxX - minX;
                    double rangeY = maxY - minY;
                    minX -= rangeX * 0.1; maxX += rangeX * 0.1;
                    minY -= rangeY * 0.1; maxY += rangeY * 0.1;

                    int k = (centros == null) ? 0 : centros.size();

                    // Misma paleta que la tabla SOLO si k <= 5
                    Color[] colores;
                    if (k > 0 && k <= 5) {
                        colores = PanelClustering.CLUSTER_SERIES;
                    } else {
                        colores = new Color[] {
                                Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA,
                                Color.ORANGE, Color.CYAN, Color.PINK, Color.DARK_GRAY
                        };
                    }

                    // Puntos
                    for (Vectorizer.Point p : puntos) {
                        if (idxX >= p.values().length || idxY >= p.values().length) continue;

                        int clusterId = assignments.getOrDefault(p.id(), -1);
                        Color c = (clusterId >= 0 && clusterId < colores.length) ? colores[clusterId] : Color.LIGHT_GRAY;

                        double valX = p.values()[idxX];
                        double valY = p.values()[idxY];

                        int x = padding + (int) ((valX - minX) / (maxX - minX) * (w - 2 * padding));
                        int y = (h - padding) - (int) ((valY - minY) / (maxY - minY) * (h - 2 * padding));

                        int r = 8;
                        g2.setColor(c);
                        g2.fillOval(x - r/2, y - r/2, r, r);
                        g2.setColor(new Color(0, 0, 0, 60));
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawOval(x - r/2, y - r/2, r, r);
                    }

                    // Centros (más grandes)
                    if (centros != null) {
                        for (int i = 0; i < centros.size(); i++) {
                            Vectorizer.Point centro = centros.get(i);
                            if (idxX >= centro.values().length || idxY >= centro.values().length) continue;

                            Color c = (i >= 0 && i < colores.length) ? colores[i] : Color.BLACK;

                            double valX = centro.values()[idxX];
                            double valY = centro.values()[idxY];

                            int x = padding + (int) ((valX - minX) / (maxX - minX) * (w - 2 * padding));
                            int y = (h - padding) - (int) ((valY - minY) / (maxY - minY) * (h - 2 * padding));

                            int r = 14;
                            g2.setColor(c);
                            g2.fillOval(x - r/2, y - r/2, r, r);
                            g2.setColor(Color.BLACK);
                            g2.setStroke(new BasicStroke(1f));
                            g2.drawOval(x - r/2, y - r/2, r, r);
                        }
                    }
                }
            };
            panelDibujo.setBackground(Color.WHITE);
            add(panelDibujo);
        }
    }
}
