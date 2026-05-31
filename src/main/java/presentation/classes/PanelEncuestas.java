package presentation.classes;

import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.TipoPregunta;
import presentation.controllers.CtrlPresentacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelEncuestas extends JPanel {

    private CtrlPresentacion ctrlPresentacion;

    private JList<String> listaEncuestas;
    private DefaultListModel<String> listModel;
    private JTable tablaPreguntas;
    private DefaultTableModel tableModel;
    private JLabel lblTituloDetalle;

    public PanelEncuestas(CtrlPresentacion ctrlPresentacion) {
        this.ctrlPresentacion = ctrlPresentacion;
        inicializarComponentes();
        recargarLista();
    }

    private void inicializarComponentes() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titulo = new JLabel("Gestor de Encuestas", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(0, 102, 204));
        this.add(titulo, BorderLayout.NORTH);

        // Panel dividido
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);

        // Zona izquierda: Lista
        JPanel panelIzquierdo = new JPanel(new BorderLayout(5, 5));
        panelIzquierdo.setBorder(BorderFactory.createTitledBorder("Encuestas Disponibles"));

        listModel = new DefaultListModel<>();

        // --- CAMBIO PASO 3: JList con Tooltip dinámico ---
        listaEncuestas = new JList<>(listModel) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index > -1) {
                    Object item = getModel().getElementAt(index);
                    return item.toString(); // Muestra el nombre de la encuesta
                }
                return null;
            }
        };
        // -------------------------------------------------

        listaEncuestas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaEncuestas.addListSelectionListener(e -> mostrarDetallesSeleccion());

        panelIzquierdo.add(new JScrollPane(listaEncuestas), BorderLayout.CENTER);

        // Botones izquierda
        JPanel panelBotonesEnc = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton btnCrear = new JButton("Crear Nueva");
        btnCrear.addActionListener(e -> crearEncuesta());
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setForeground(Color.RED);
        btnEliminar.addActionListener(e -> eliminarEncuesta());

        panelBotonesEnc.add(btnCrear);
        panelBotonesEnc.add(btnEliminar);
        panelIzquierdo.add(panelBotonesEnc, BorderLayout.SOUTH);

        // Zona derecha: Tabla preguntas
        JPanel panelDerecho = new JPanel(new BorderLayout(5, 5));
        lblTituloDetalle = new JLabel("Selecciona una encuesta para ver detalles");
        lblTituloDetalle.setFont(new Font("Arial", Font.ITALIC, 14));
        panelDerecho.add(lblTituloDetalle, BorderLayout.NORTH);

        String[] columnas = {"ID Pregunta", "Tipo", "Opciones / Config"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // --- CAMBIO PASO 3: JTable con Tooltip dinámico por celda ---
        tablaPreguntas = new JTable(tableModel) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                try {
                    if (rowIndex >= 0 && colIndex >= 0) {
                        Object val = getValueAt(rowIndex, colIndex);
                        if (val != null) {
                            tip = val.toString(); // Muestra el contenido de la celda
                        }
                    }
                } catch (RuntimeException e1) { }
                return tip;
            }
        };
        // -----------------------------------------------------------

        // FUNCIONALIDAD: Doble clic para ver detalle de la pregunta/opciones
        tablaPreguntas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tablaPreguntas.rowAtPoint(evt.getPoint());
                    int col = tablaPreguntas.columnAtPoint(evt.getPoint());

                    if (row >= 0 && col >= 0) {
                        Object valor = tablaPreguntas.getValueAt(row, col);
                        if (valor != null) {
                            verTextoCompleto(valor.toString());
                        }
                    }
                }
            }
        });

        panelDerecho.add(new JScrollPane(tablaPreguntas), BorderLayout.CENTER);

        // Botones derecha
        JPanel panelBotonesPreg = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddPregunta = new JButton("Añadir Pregunta...");
        btnAddPregunta.addActionListener(e -> agregarPreguntaCompleta());
        JButton btnDelPregunta = new JButton("Eliminar Pregunta");
        btnDelPregunta.addActionListener(e -> eliminarPregunta());

        panelBotonesPreg.add(btnAddPregunta);
        panelBotonesPreg.add(btnDelPregunta);
        panelDerecho.add(panelBotonesPreg, BorderLayout.SOUTH);

        splitPane.setLeftComponent(panelIzquierdo);
        splitPane.setRightComponent(panelDerecho);
        this.add(splitPane, BorderLayout.CENTER);

        // BOTÓN VOLVER (Asegurado)
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnVolver = new JButton("<< Volver al Menú Principal");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        // Llama a cambiarVista usando la constante del menú
        btnVolver.addActionListener(e -> ctrlPresentacion.cambiarVista(VistaPrincipal.VISTA_MENU));

        panelSur.add(btnVolver);
        this.add(panelSur, BorderLayout.SOUTH);

        // Esto ya lo tenías, y se encarga de los botones estáticos y labels
        GuiUtils.asignarTooltipsGlobales(this);
    }

    // Cargar lista desde controlador
    public void recargarLista() {
        listModel.clear();
        for (String id : ctrlPresentacion.obtenerIdsEncuestas()) {
            listModel.addElement(id);
        }
        tableModel.setRowCount(0);
        lblTituloDetalle.setText("Selecciona una encuesta...");
    }

    private void mostrarDetallesSeleccion() {
        String idSeleccionado = listaEncuestas.getSelectedValue();
        if (idSeleccionado == null) return;

        lblTituloDetalle.setText("Detalles de: " + idSeleccionado);
        tableModel.setRowCount(0);

        Cuestionario c = ctrlPresentacion.getCtrlDominio().obtener(idSeleccionado);
        if (c != null) {
            for (Pregunta p : c.getPreguntas()) {
                String config = "";
                if (p.getTipo() == TipoPregunta.OPCION_UNICA || p.getTipo() == TipoPregunta.OPCION_MULTIPLE) {
                    config = String.join("|", p.getOpciones());
                    if (p.getTipo() == TipoPregunta.OPCION_MULTIPLE) config += " (Max: " + p.getMaxSeleccion() + ")";
                }
                tableModel.addRow(new Object[]{p.getId(), p.getTipo(), config});
            }
        }
    }

    // Operaciones
    private void crearEncuesta() {
        String id = JOptionPane.showInputDialog(this, "Introduce el ID de la nueva encuesta:", "Crear Encuesta", JOptionPane.QUESTION_MESSAGE);
        if (id != null && !id.trim().isEmpty()) {
            try {
                ctrlPresentacion.getCtrlDominio().crear(id.trim());
                recargarLista();
                JOptionPane.showMessageDialog(this, "Encuesta creada con éxito.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarEncuesta() {
        String id = listaEncuestas.getSelectedValue();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una encuesta primero.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres borrar '" + id + "' y todas sus respuestas?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ctrlPresentacion.getCtrlDominio().eliminar(id);
            ctrlPresentacion.getCtrlDominio().eliminarTodas(id);
            recargarLista();
        }
    }

    private void agregarPreguntaCompleta() {
        String idEncuesta = listaEncuestas.getSelectedValue();
        if (idEncuesta == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una encuesta primero.");
            return;
        }

        // Formulario
        JTextField txtEnunciado = new JTextField();
        JComboBox<TipoPregunta> comboTipo = new JComboBox<>(TipoPregunta.values());

        JTextArea txtOpciones = new JTextArea(3, 20);
        txtOpciones.setLineWrap(true);
        txtOpciones.setToolTipText("Ej: Rojo|Verde|Azul");
        JScrollPane scrollOpciones = new JScrollPane(txtOpciones);

        txtOpciones.setEnabled(false);
        txtOpciones.setBackground(SystemColor.control);

        JSpinner spinMax = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        spinMax.setEnabled(false);

        JPanel panelForm = new JPanel(new GridLayout(0, 1, 5, 5));
        panelForm.add(new JLabel("Enunciado (ID):"));
        panelForm.add(txtEnunciado);

        panelForm.add(new JLabel("Tipo de Pregunta:"));
        panelForm.add(comboTipo);

        panelForm.add(new JLabel("Opciones (separadas por '|'):"));
        panelForm.add(scrollOpciones);

        panelForm.add(new JLabel("Máx. Selecciones (0 = sin límite, solo Mult.):"));
        panelForm.add(spinMax);

        // Listener para activar campos
        comboTipo.addActionListener(e -> {
            TipoPregunta tipo = (TipoPregunta) comboTipo.getSelectedItem();
            boolean usaOpciones = (tipo == TipoPregunta.OPCION_UNICA || tipo == TipoPregunta.OPCION_MULTIPLE);
            boolean esMultiple = (tipo == TipoPregunta.OPCION_MULTIPLE);

            txtOpciones.setEnabled(usaOpciones);
            txtOpciones.setBackground(usaOpciones ? Color.WHITE : SystemColor.control);

            spinMax.setEnabled(esMultiple);
        });

        comboTipo.setSelectedItem(TipoPregunta.TEXTO);

        int result = JOptionPane.showConfirmDialog(this, panelForm,
                "Nueva Pregunta para " + idEncuesta, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String enun = txtEnunciado.getText().trim();
            if (enun.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El enunciado no puede estar vacío.");
                return;
            }

            TipoPregunta tipo = (TipoPregunta) comboTipo.getSelectedItem();
            List<String> listaOpciones = new ArrayList<>();
            int maxSel = 0;

            if (tipo == TipoPregunta.OPCION_UNICA || tipo == TipoPregunta.OPCION_MULTIPLE) {
                String rawOpciones = txtOpciones.getText().trim();
                if (rawOpciones.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Para este tipo de pregunta debes escribir opciones (separadas por '|').");
                    return;
                }

                String[] partes = rawOpciones.split("\\|");
                for (String p : partes) {
                    if (!p.trim().isEmpty()) {
                        listaOpciones.add(p.trim());
                    }
                }

                if (tipo == TipoPregunta.OPCION_MULTIPLE) {
                    maxSel = (Integer) spinMax.getValue();
                }
            }

            try {
                Pregunta p = new Pregunta(enun, enun, tipo, listaOpciones, maxSel);
                ctrlPresentacion.getCtrlDominio().agregarPregunta(idEncuesta, p);

                mostrarDetallesSeleccion();
                JOptionPane.showMessageDialog(this, "Pregunta añadida correctamente.");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al crear pregunta: " + e.getMessage());
            }
        }
    }

    private void eliminarPregunta() {
        String idEncuesta = listaEncuestas.getSelectedValue();
        int filaTabla = tablaPreguntas.getSelectedRow();

        if (idEncuesta == null || filaTabla == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una encuesta y una pregunta.");
            return;
        }

        String idPregunta = (String) tableModel.getValueAt(filaTabla, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Borrar pregunta '" + idPregunta + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ctrlPresentacion.getCtrlDominio().eliminarPregunta(idEncuesta, idPregunta);
            ctrlPresentacion.getCtrlDominio().eliminarPregunta(idEncuesta, idPregunta);
            mostrarDetallesSeleccion();
        }
    }

    private void verTextoCompleto(String texto) {
        JTextArea textArea = new JTextArea(texto);
        textArea.setRows(8);
        textArea.setColumns(40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "Detalle Completo", JOptionPane.INFORMATION_MESSAGE);
    }
}