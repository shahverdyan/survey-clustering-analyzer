package presentation.classes;

import presentation.controllers.CtrlPresentacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PanelLogin extends JPanel {

    private CtrlPresentacion ctrlPresentacion;

    // Contenedor interno para alternar entre Login y Registro
    private JPanel cards;
    private CardLayout cardLayout;

    // --- Componentes Login ---
    private JTextField txtLoginEmail;
    private JPasswordField txtLoginPass;

    // --- Componentes Registro ---
    private JTextField txtRegNombre;
    private JTextField txtRegApellido;
    private JTextField txtRegEmail;
    private JTextField txtRegNacimiento; // Formato DD/MM/AAAA
    private JPasswordField txtRegPass;
    private JPasswordField txtRegPassRepetir;

    public PanelLogin(CtrlPresentacion ctrlPresentacion) {
        this.ctrlPresentacion = ctrlPresentacion;
        inicializarUI();
    }

    private void inicializarUI() {
        // Usamos GridBagLayout en el panel principal para CENTRAR la caja de login en la pantalla
        this.setLayout(new GridBagLayout());
        this.setBackground(new Color(230, 240, 250)); // Fondo azul muy suave

        // Configuración del CardLayout para cambiar entre formularios
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1)); // Borde gris fino

        // Añadimos las dos vistas
        cards.add(crearVistaLogin(), "LOGIN");
        cards.add(crearVistaRegistro(), "REGISTRO");

        // Añadimos el panel de cartas al centro de este panel
        this.add(cards);

        //Tooltip
        GuiUtils.asignarTooltipsGlobales(this);
    }

    private JPanel crearVistaLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40)); // Margen interior

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); // Espaciado entre elementos
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Bienvenido", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Campo Email
        gbc.gridy++; gbc.gridwidth = 1;
        panel.add(new JLabel("Correo Electrónico:"), gbc);

        gbc.gridy++;
        txtLoginEmail = new JTextField(20);
        panel.add(txtLoginEmail, gbc);

        // Campo Password
        gbc.gridy++;
        panel.add(new JLabel("Contraseña:"), gbc);

        gbc.gridy++;
        txtLoginPass = new JPasswordField(20);
        panel.add(txtLoginPass, gbc);

        // Botón Entrar
        gbc.gridy++; gbc.insets = new Insets(20, 5, 10, 5);
        JButton btnEntrar = new JButton("INICIAR SESIÓN");
        btnEntrar.setBackground(new Color(70, 130, 180)); // Azul acero
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntrar.setFocusPainted(false);
        btnEntrar.addActionListener(this::actionLogin);
        panel.add(btnEntrar, gbc);

        // Separador
        gbc.gridy++;
        panel.add(new JSeparator(), gbc);

        // Botón Crear Cuenta
        gbc.gridy++; gbc.insets = new Insets(5, 5, 5, 5);
        JButton btnIrRegistro = new JButton("¿No tienes cuenta? Regístrate aquí");
        btnIrRegistro.setBorderPainted(false);
        btnIrRegistro.setContentAreaFilled(false);
        btnIrRegistro.setForeground(new Color(30, 144, 255));
        btnIrRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIrRegistro.addActionListener(e -> {
            limpiarCamposRegistro();
            cardLayout.show(cards, "REGISTRO");
        });
        panel.add(btnIrRegistro, gbc);

        return panel;
    }

    private JPanel crearVistaRegistro() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Crear Cuenta Nueva", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Campos
        gbc.gridwidth = 1;

        gbc.gridy++; panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; txtRegNombre = new JTextField(15); panel.add(txtRegNombre, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; txtRegApellido = new JTextField(15); panel.add(txtRegApellido, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; txtRegEmail = new JTextField(15); panel.add(txtRegEmail, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Fecha Nacimiento (DD/MM/AAAA):"), gbc);
        gbc.gridx = 1; txtRegNacimiento = new JTextField(15); panel.add(txtRegNacimiento, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; txtRegPass = new JPasswordField(15); panel.add(txtRegPass, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Repetir Contraseña:"), gbc);
        gbc.gridx = 1; txtRegPassRepetir = new JPasswordField(15); panel.add(txtRegPassRepetir, gbc);

        // Botón Registrar
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.insets = new Insets(20, 5, 5, 5);
        JButton btnRegistrar = new JButton("CREAR CUENTA");
        btnRegistrar.setBackground(new Color(60, 179, 113)); // Verde medio
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(this::actionRegistrar);
        panel.add(btnRegistrar, gbc);

        // Botón Volver
        gbc.gridy++; gbc.insets = new Insets(5, 5, 5, 5);
        JButton btnVolver = new JButton("Cancelar y Volver");
        btnVolver.addActionListener(e -> cardLayout.show(cards, "LOGIN"));
        panel.add(btnVolver, gbc);

        return panel;
    }

    //LÓGICA DE EVENTOS

    private void actionLogin(ActionEvent e) {
        String email = txtLoginEmail.getText().trim();
        String pass = new String(txtLoginPass.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            mostrarError("Por favor, introduce email y contraseña.");
            return;
        }

        // Llamada al controlador de presentación (que delegará al dominio)
        boolean exito = ctrlPresentacion.login(email, pass);

        if (exito) {
            // Limpiamos campos por seguridad
            txtLoginPass.setText("");
        } else {
            mostrarError("Email o contraseña incorrectos.");
        }
    }

    private void actionRegistrar(ActionEvent e) {
        String nombre = txtRegNombre.getText().trim();
        String apellido = txtRegApellido.getText().trim();
        String email = txtRegEmail.getText().trim();
        String nacimiento = txtRegNacimiento.getText().trim();
        String p1 = new String(txtRegPass.getPassword());
        String p2 = new String(txtRegPassRepetir.getPassword());

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || p1.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }
        if (!p1.equals(p2)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        try {
            // Llamada al controlador de presentación
            ctrlPresentacion.registrarUsuario(email, p1, nombre, apellido, nacimiento);

            JOptionPane.showMessageDialog(this, "Cuenta creada con éxito. Ahora puedes iniciar sesión.");
            cardLayout.show(cards, "LOGIN"); // Volver al login

        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage()); // E.j. "El usuario ya existe"
        }
    }

    private void limpiarCamposRegistro() {
        txtRegNombre.setText("");
        txtRegApellido.setText("");
        txtRegEmail.setText("");
        txtRegNacimiento.setText("");
        txtRegPass.setText("");
        txtRegPassRepetir.setText("");
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}