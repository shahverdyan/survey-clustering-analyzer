package presentation.classes;

import domain.classes.model.*;
import presentation.controllers.CtrlPresentacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogoResponder extends JDialog {

    private final CtrlPresentacion ctrlPresentacion;
    private JComboBox<String> comboEncuestas;
    private JPanel panelPreguntas;

    // Guardamos la referencia a la Pregunta original y a su componente visual
    private Map<Pregunta, InputComponent> inputsMap;

    public DialogoResponder(Frame owner, CtrlPresentacion ctrl) {
        super(owner, "Responder Encuesta", true);
        this.ctrlPresentacion = ctrl;
        this.inputsMap = new HashMap<>();

        this.setSize(600, 500);

        // Aseguramos que no se salga de la pantalla obteniendo el tamaño real
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (this.getHeight() > screenSize.height) {
            this.setSize(this.getWidth(), screenSize.height - 50);
        }

        this.setLocationRelativeTo(owner); // Centrar en pantalla
        this.setLayout(new BorderLayout());

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // --- NORTE: Selector ---
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelTop.setBackground(new Color(245, 245, 245));
        panelTop.add(new JLabel("Selecciona Encuesta a responder:"));

        comboEncuestas = new JComboBox<>();
        cargarTodasLasEncuestas();
        comboEncuestas.addActionListener(e -> cargarPreguntasDeEncuesta());

        panelTop.add(comboEncuestas);
        add(panelTop, BorderLayout.NORTH);

        // --- CENTRO: Preguntas ---
        panelPreguntas = new JPanel();
        panelPreguntas.setLayout(new BoxLayout(panelPreguntas, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(panelPreguntas);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // --- SUR: Botones ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JButton btnEnviar = new JButton("Enviar Respuesta");
        btnEnviar.setBackground(new Color(100, 149, 237));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new Font("Arial", Font.BOLD, 12));
        btnEnviar.addActionListener(e -> enviarRespuesta());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnEnviar);
        add(panelBotones, BorderLayout.SOUTH);

        // Estado inicial
        comboEncuestas.setSelectedIndex(-1);
        panelPreguntas.removeAll();
    }

    private void cargarTodasLasEncuestas() {
        comboEncuestas.removeAllItems();
        List<String> todas = ctrlPresentacion.obtenerIdsTodasEncuestas();
        for (String id : todas) {
            comboEncuestas.addItem(id);
        }
    }

    private void cargarPreguntasDeEncuesta() {
        panelPreguntas.removeAll();
        inputsMap.clear();

        String idEncuesta = (String) comboEncuestas.getSelectedItem();
        if (idEncuesta == null) {
            panelPreguntas.revalidate();
            panelPreguntas.repaint();
            return;
        }

        Cuestionario c = ctrlPresentacion.obtenerEncuesta(idEncuesta);
        if (c != null) {
            for (Pregunta p : c.getPreguntas()) {
                agregarPreguntaVisual(p);
            }
        }

        panelPreguntas.revalidate();
        panelPreguntas.repaint();
    }

    private void agregarPreguntaVisual(Pregunta p) {
        JPanel pRow = new JPanel(new BorderLayout());
        pRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)
        ));

        // 1. Texto de ayuda según el tipo (Requisito del usuario)
        String textoAyuda = obtenerTextoAyuda(p);

        JLabel lbl = new JLabel("<html><b>" + p.getEnunciado() + "</b> <i style='color:gray'>" + textoAyuda + "</i></html>");
        pRow.add(lbl, BorderLayout.NORTH);

        // 2. Componente de entrada
        InputComponent input = crearInputSegunTipo(p);
        pRow.add(input.getComponent(), BorderLayout.CENTER);

        inputsMap.put(p, input); // Guardamos usando la Pregunta como clave
        panelPreguntas.add(pRow);
    }

    private String obtenerTextoAyuda(Pregunta p) {
        switch (p.getTipo()) {
            case NUMERICA: return "(Numérica)";
            case TEXTO: return "(Texto Libre)";
            case OPCION_UNICA: return "(Escoge 1 opción)";
            case OPCION_MULTIPLE:
                int max = p.getMaxSeleccion();
                return "(Escoge varias" + (max > 0 ? ", máx " + max : "") + ")";
            default: return "";
        }
    }

    private InputComponent crearInputSegunTipo(Pregunta p) {
        TipoPregunta tipo = p.getTipo();

        if (tipo == TipoPregunta.OPCION_UNICA) {
            return new RadioInput(p.getOpciones());
        } else if (tipo == TipoPregunta.OPCION_MULTIPLE) {
            return new CheckboxInput(p.getOpciones(), p.getMaxSeleccion());
        } else {
            return new TextInput(tipo == TipoPregunta.NUMERICA);
        }
    }

    private void enviarRespuesta() {
        String idEncuesta = (String) comboEncuestas.getSelectedItem();
        if (idEncuesta == null) return;

        try {
            String usuario = ctrlPresentacion.getUsuarioEmail();
            Respuesta nuevaRespuesta = new Respuesta(usuario);

            // Lista para acumular errores
            List<String> errores = new ArrayList<>();

            // Iteramos sobre las preguntas y sus inputs
            for (Map.Entry<Pregunta, InputComponent> entry : inputsMap.entrySet()) {
                Pregunta p = entry.getKey();
                InputComponent input = entry.getValue();

                try {
                    // Validamos y obtenemos valor
                    ValorRespuesta valor = input.getValorValidado();

                    if (valor != null) {
                        nuevaRespuesta.put(p.getId(), valor);
                    }
                    // Si todo va bien, restauramos color blanco
                    input.marcarComoValido();

                } catch (Exception ex) {
                    // Si falla la validación
                    errores.add("- Pregunta '" + p.getId() + "' (" + p.getEnunciado() + "): " + ex.getMessage());
                    input.marcarComoInvalido(); // Se pone rojo
                }
            }

            if (!errores.isEmpty()) {
                String msg = "No se puede enviar la respuesta. Corrige los siguientes errores:\n\n" + String.join("\n", errores);
                JOptionPane.showMessageDialog(this, msg, "Errores de Validación", JOptionPane.ERROR_MESSAGE);
                return; // IMPORTANTE: No enviamos nada
            }

            // Si llegamos aquí, todo está perfecto
            ctrlPresentacion.agregarRespuesta(idEncuesta, nuevaRespuesta);
            JOptionPane.showMessageDialog(this, "¡Respuesta guardada correctamente!");
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error crítico: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =======================================================
    // Clases internas (UI + Lógica de Validación)
    // =======================================================

    interface InputComponent {
        JComponent getComponent();
        ValorRespuesta getValorValidado() throws Exception;
        void marcarComoValido();
        void marcarComoInvalido();
    }

    // 1. TEXTO Y NÚMEROS
    class TextInput implements InputComponent {
        JTextField txt;
        boolean esNumerica;

        public TextInput(boolean esNumerica) {
            this.txt = new JTextField();
            this.esNumerica = esNumerica;
        }
        @Override
        public JComponent getComponent() { return txt; }

        @Override
        public void marcarComoValido() { txt.setBackground(Color.WHITE); }

        @Override
        public void marcarComoInvalido() { txt.setBackground(new Color(255, 200, 200)); } // Rojo suave

        @Override
        public ValorRespuesta getValorValidado() throws Exception {
            String t = txt.getText().trim();
            if (t.isEmpty()) return null; // Campo vacío se permite (o lanza excepción si fuera obligatorio)

            if (esNumerica) {
                try {
                    double v = Double.parseDouble(t);
                    return new ValorRespuestaNumerica(v);
                } catch (NumberFormatException e) {
                    throw new Exception("Debe ser un valor numérico.");
                }
            } else {
                return new ValorRespuestaTextual(t);
            }
        }
    }

    // 2. OPCIÓN ÚNICA (Radio Buttons)
    class RadioInput implements InputComponent {
        JPanel panel;
        List<JRadioButton> radios = new ArrayList<>();
        ButtonGroup group = new ButtonGroup();

        public RadioInput(List<String> opciones) {
            panel = new JPanel(new GridLayout(0, 1));
            for (String op : opciones) {
                JRadioButton rb = new JRadioButton(op);
                group.add(rb);
                panel.add(rb);
                radios.add(rb);
            }
        }
        @Override
        public JComponent getComponent() { return panel; }

        @Override
        public void marcarComoValido() { panel.setBackground(null); }
        @Override
        public void marcarComoInvalido() { panel.setBackground(new Color(255, 200, 200)); }

        @Override
        public ValorRespuesta getValorValidado() {
            for (int i = 0; i < radios.size(); i++) {
                if (radios.get(i).isSelected()) {
                    return new ValorRespuestaOpcionUnica(i);
                }
            }
            return null;
        }
    }

    // 3. OPCIÓN MÚLTIPLE (CheckBoxes)
    class CheckboxInput implements InputComponent {
        JPanel panel;
        List<JCheckBox> checks = new ArrayList<>();
        int maxSel;

        public CheckboxInput(List<String> opciones, int maxSel) {
            this.maxSel = maxSel;
            panel = new JPanel(new GridLayout(0, 1));
            for (String op : opciones) {
                JCheckBox cb = new JCheckBox(op);
                panel.add(cb);
                checks.add(cb);
            }
        }
        @Override
        public JComponent getComponent() { return panel; }

        @Override
        public void marcarComoValido() { panel.setBackground(null); }
        @Override
        public void marcarComoInvalido() { panel.setBackground(new Color(255, 200, 200)); }

        @Override
        public ValorRespuesta getValorValidado() throws Exception {
            List<Integer> seleccionadas = new ArrayList<>();
            for (int i = 0; i < checks.size(); i++) {
                if (checks.get(i).isSelected()) {
                    seleccionadas.add(i);
                }
            }

            if (maxSel > 0 && seleccionadas.size() > maxSel) {
                throw new Exception("Has seleccionado " + seleccionadas.size() + " opciones. Máximo permitido: " + maxSel);
            }

            if (seleccionadas.isEmpty()) return null;
            return new ValorRespuestaOpcionMultiple(seleccionadas);
        }
    }
}