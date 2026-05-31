package presentation.classes;

import presentation.controllers.CtrlPresentacion;

import javax.swing.*;
import java.awt.*;

public class VistaPrincipal {

    private CtrlPresentacion ctrlPresentacion;
    private JFrame frame;
    private JPanel panelContenedor;
    private CardLayout cardLayout;

    // Referencias a los paneles
    private PanelLogin panelLogin; // <--- NUEVO
    private PanelEncuestas panelEncuestas;
    private PanelRespuestas panelRespuestas;
    private PanelClustering panelClustering;

    private JTextArea logArea;

    // Constantes de navegación
    public static final String VISTA_LOGIN = "LOGIN"; // <--- NUEVO
    public static final String VISTA_MENU = "MENU";
    public static final String VISTA_ENCUESTAS = "ENCUESTAS";
    public static final String VISTA_RESPUESTAS = "RESPUESTAS";
    public static final String VISTA_CLUSTERING = "CLUSTERING";
    public static final String VISTA_DATOS = "DATOS";

    public VistaPrincipal(CtrlPresentacion ctrlPresentacion) {
        this.ctrlPresentacion = ctrlPresentacion;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        frame = new JFrame("Survey Clustering Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750); // Un poco más alto para que quepa todo bien
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);

        // LOGIN (Lo primero que añadimos)
        this.panelLogin = new PanelLogin(ctrlPresentacion);
        panelContenedor.add(this.panelLogin, VISTA_LOGIN);

        // RESTO DE PANELES
        panelContenedor.add(crearPanelMenu(), VISTA_MENU);

        this.panelRespuestas = new PanelRespuestas(ctrlPresentacion);
        panelContenedor.add(this.panelRespuestas, VISTA_RESPUESTAS);

        this.panelClustering = new PanelClustering(ctrlPresentacion);
        panelContenedor.add(this.panelClustering, VISTA_CLUSTERING);

        // Gestor de Datos
        panelContenedor.add(new PanelDatos(ctrlPresentacion), VISTA_DATOS);

        // Gestor de Encuestas
        this.panelEncuestas = new PanelEncuestas(ctrlPresentacion);
        panelContenedor.add(this.panelEncuestas, VISTA_ENCUESTAS);

        frame.add(panelContenedor, BorderLayout.CENTER);

        crearPanelLog();

        //Arrancar mostrando el LOGIN
        cardLayout.show(panelContenedor, VISTA_LOGIN);
    }

    public void entrarAlSistema() {
        log("Acceso concedido. Bienvenido/a " + ctrlPresentacion.getUsuarioNombre());
        mostrarPanel(VISTA_MENU);
        frame.revalidate();
    }

    public void salirAlLogin() {
        log("Sesión cerrada.");
        mostrarPanel(VISTA_LOGIN);
    }


    private void crearPanelLog() {
        logArea = new JTextArea(5, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.append(">> Sistema iniciado. Identifíquese por favor...\n");
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Consola de Sistema"));

        frame.add(scrollLog, BorderLayout.SOUTH);
    }

    private JPanel crearPanelMenu() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        panel.add(crearBotonMenu("Gestor Encuestas", VISTA_ENCUESTAS, new Color(173, 216, 230)));
        panel.add(crearBotonMenu("Gestor Respuestas", VISTA_RESPUESTAS, new Color(144, 238, 144)));
        panel.add(crearBotonMenu("Clustering", VISTA_CLUSTERING, new Color(255, 228, 181)));
        panel.add(crearBotonMenu("Gestor de Datos", VISTA_DATOS, new Color(221, 160, 221)));

        JPanel panelTotal = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Bienvenido al Sistema de Clustering", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));

        // Panel inferior con botones de Salir y Logout
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setBackground(new Color(255, 100, 100)); // Rojo claro
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> ctrlPresentacion.logout());

        JButton btnSalir = new JButton("Salir del Escritorio");
        btnSalir.addActionListener(e -> System.exit(0));

        panelBotones.add(btnLogout);
        panelBotones.add(btnSalir);

        panelTotal.add(titulo, BorderLayout.NORTH);
        panelTotal.add(panel, BorderLayout.CENTER);
        panelTotal.add(panelBotones, BorderLayout.SOUTH);

        //Tooltip
        GuiUtils.asignarTooltipsGlobales(panelTotal);

        return panelTotal;
    }

    private JButton crearBotonMenu(String texto, String vistaDestino, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setBackground(colorFondo);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> mostrarPanel(vistaDestino));
        return btn;
    }

    public void mostrarPanel(String nombreVista) {
        cardLayout.show(panelContenedor, nombreVista);

        // Actualizar vistas al entrar
        if (nombreVista.equals(VISTA_RESPUESTAS) && panelRespuestas != null) {
            panelRespuestas.alMostrar();
        }
        // Ahora PanelEncuestas cargará solo encuestas usuario
        if (nombreVista.equals(VISTA_ENCUESTAS) && panelEncuestas != null) {
            panelEncuestas.recargarLista();
        }
        if (nombreVista.equals(VISTA_CLUSTERING) && panelClustering != null) {
            panelClustering.alMostrar();
        }
    }

    public void hacerVisible() {
        frame.setVisible(true);
    }

    public void log(String mensaje) {
        if (logArea != null) {
            logArea.append(">> " + mensaje + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    public void mostrarError(String titulo, String msg) {
        JOptionPane.showMessageDialog(frame, msg, titulo, JOptionPane.ERROR_MESSAGE);
    }
    public void mostrarInfo(String titulo, String msg) {
        JOptionPane.showMessageDialog(frame, msg, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}