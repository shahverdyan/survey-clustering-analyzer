package presentation.classes;

import presentation.controllers.CtrlPresentacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class PanelDatos extends JPanel {

    private CtrlPresentacion ctrlPresentacion;

    public PanelDatos(CtrlPresentacion ctrlPresentacion) {
        this.ctrlPresentacion = ctrlPresentacion;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título superior
        JLabel titulo = new JLabel("Gestor de Datos", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        this.add(titulo, BorderLayout.NORTH);

        // Panel central con botones
        JPanel panelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Botón importar
        JButton btnImportar = new JButton("Importar Encuesta (CSV)");
        btnImportar.setFont(new Font("Arial", Font.PLAIN, 16));
        btnImportar.setBackground(new Color(221, 160, 221)); // Violeta
        btnImportar.setPreferredSize(new Dimension(300, 50));
        btnImportar.addActionListener(e -> abrirSelectorImportar());

        // Botón exportar
        JButton btnExportar = new JButton("Exportar Encuesta (CSV)");
        btnExportar.setFont(new Font("Arial", Font.PLAIN, 16));
        btnExportar.setPreferredSize(new Dimension(300, 50));
        btnExportar.addActionListener(e -> abrirSelectorExportar());

        panelCentral.add(btnImportar, gbc);
        gbc.gridy++;
        panelCentral.add(btnExportar, gbc);

        this.add(panelCentral, BorderLayout.CENTER);

        // Botón volver
        JButton btnVolver = new JButton("<< Volver al Menú Principal");
        btnVolver.addActionListener(e -> ctrlPresentacion.cambiarVista(VistaPrincipal.VISTA_MENU));

        JPanel panelSur = new JPanel();
        panelSur.add(btnVolver);
        this.add(panelSur, BorderLayout.SOUTH);

        //tooltip
        GuiUtils.asignarTooltipsGlobales(this);
    }

    private void abrirSelectorImportar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle("Importar CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            ctrlPresentacion.importarEncuesta(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    // Exportar con selección
    private void abrirSelectorExportar() {
        // Lista de encuestas
        List<String> idsEncuestas = ctrlPresentacion.obtenerIdsEncuestas();

        if (idsEncuestas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay encuestas cargadas en el sistema para exportar.",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Elegir encuesta
        String idSeleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona la encuesta que quieres exportar:",
                "Elegir Encuesta",
                JOptionPane.QUESTION_MESSAGE,
                null,
                idsEncuestas.toArray(), // Opciones
                idsEncuestas.get(0));   // Default

        if (idSeleccionado == null) return;

        // Selector de archivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle("Guardar CSV de '" + idSeleccionado + "'");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));

        // Nombre por defecto
        fileChooser.setSelectedFile(new File(idSeleccionado + "_export.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) {
                path += ".csv";
            }
            // Exportar
            ctrlPresentacion.exportarDatos(idSeleccionado, path);
        }
    }
}