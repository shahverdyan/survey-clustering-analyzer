package drivers;

import persistence.controllers.CtrlPersistencia;
import domain.controllers.CtrlDominio;
import domain.classes.util.ConsoleIO;


public class Driver {
    private static final ConsoleIO io = new ConsoleIO();


    @SuppressWarnings("unused")
    public static void main(String[] args) {
        //Si la primera línea de la entrada es "input", se activa el modo automático
        String m = io.prompt("");
        // Referenciamos `args` explícitamente para evitar advertencias de parámetro no usado
        if (args != null && args.length > 0) {
            // no hacemos nada ruidoso: solo leemos la longitud para marcar uso
            int _unused = args.length;
        }
        ConsoleIO.Modo modo;
        if (m.equals("input")) {
            modo = ConsoleIO.Modo.AUTOMATICO;
        }
        else modo = ConsoleIO.Modo.MANUAL;
        io.setModo(modo);

        //Repositorios
        CtrlPersistencia ctrlPersistencia = new CtrlPersistencia();
        CtrlDominio ctrlDominio = new CtrlDominio(ctrlPersistencia);

        // Bucle externo: permite volver al menú de autenticación desde el menú principal
        while (true) {
            //Autenticación
            boolean autenticado = false;
            while (!autenticado) {
                if (io.getModo() == ConsoleIO.Modo.MANUAL) {
                    System.out.println("\n=== Autenticación ===");
                    System.out.println("1) Iniciar sesión");
                    System.out.println("2) Registrarse");
                    System.out.println("0) Salir");
                }

                String opt = io.prompt("Opcion");

                switch (opt) {
                    case "1" -> {
                        String email = io.prompt("Email");
                        String pass = io.prompt("Contraseña");
                        if (ctrlDominio.login(email, pass)) {
                            System.out.println("Inicio de sesión correcto. Bienvenido, " + ctrlDominio.getUsuarioLogueadoNombre() + "!");
                            autenticado = true;
                        } else {
                            System.out.println("Email o contraseña incorrectos.");
                        }
                    }
                    case "2" -> {
                        String email = io.prompt("Email");
                        String nombre = io.prompt("Nombre");
                        String ape = io.prompt("Apellido");
                        String nac = io.prompt("Fecha nacimiento (DD/MM/AAAA)");
                        String pass = io.prompt("Contraseña");
                        try {
                            ctrlDominio.registrarUsuario(email, pass, nombre, ape, nac);
                            System.out.println("Usuario registrado con éxito. Puedes iniciar sesión ahora.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error registro: " + e.getMessage());
                        }
                    }
                    case "0" -> {
                        return; // salir del programa
                    }
                    default -> System.out.println("Opcion invalida");
                }
            }


            //Menus
            EncuestaMenu encuestaMenu = new EncuestaMenu(io, ctrlDominio, modo);
            RespuestaMenu respuestaMenu = new RespuestaMenu(io, ctrlDominio, modo);
            ClusteringMenu clusteringMenu = new ClusteringMenu(io, ctrlDominio, modo);
            ExportarImportarMenu exportarImportarMenu = new ExportarImportarMenu(io, ctrlDominio, modo);

            // MENU PRINCIPAL (se mantiene hasta que el usuario decida volver a autenticación o salir)
            menuLoop:
            while (true) {
                if(io.getModo() == ConsoleIO.Modo.MANUAL){
                    System.out.println("\nMENU PRINCIPAL");
                    System.out.println("Encuestas cargadas: " + ctrlDominio.listar().size());
                    System.out.println("1) Encuestas (crear con preguntas, listar, editar, eliminar)");
                    System.out.println("2) Respuestas (por encuesta)");
                    System.out.println("3) Clustering (por encuesta)");
                    System.out.println("4) Importar / Exportar");
                    System.out.println("5) Comparar fichero de entrada con fichero de salida");
                    System.out.println("6) Cerrar sesión");
                    System.out.println("0) Salir");
                }

                String opcion = io.prompt("Opcion");

                try { //Se escoge a qué menú acceder
                    switch (opcion) {
                        case "1" -> encuestaMenu.ejecutar();
                        case "2" -> respuestaMenu.ejecutar();
                        case "3" -> clusteringMenu.ejecutar();
                        case "4" -> exportarImportarMenu.ejecutar();
                        case "5" -> {
                            compararFicheros();
                            return;
                        }
                        case "6" -> {
                            // Volver al menú de autenticación: hacemos logout y rompemos el bucle del menú principal
                            ctrlDominio.logout();
                            break menuLoop;
                        }
                        case "0" -> {
                            return;
                        }
                        default -> System.out.println("Opcion invalida");
                    }
                }
                catch (Exception e) {
                    System.out.println("Error inesperado: " + e.getMessage());
                }
            }

            // Si hemos solicitado volver a autenticación, el while externo repetirá el proceso
        }
    }

    private static void compararFicheros() {
        String archivo1 = io.prompt("Ruta del fichero generado por el programa");
        String archivo2 = io.prompt("Ruta del fichero de referencia a comparar");

        java.io.File f1 = new java.io.File(archivo1);
        java.io.File f2 = new java.io.File(archivo2);

        // Si no existen, intentar suponer que están en ./csvFiles/<nombre>
        if (!f1.exists()) {
            java.io.File alt1 = new java.io.File("csvFiles", new java.io.File(archivo1).getName());
            if (alt1.exists()) {
                System.out.println("Usando fichero de proyecto: " + alt1.getPath());
                f1 = alt1;
            } else {
                System.out.println("Fichero no encontrado: " + archivo1);
                return;
            }
        }
        if (!f2.exists()) {
            java.io.File alt2 = new java.io.File("csvFiles", new java.io.File(archivo2).getName());
            if (alt2.exists()) {
                System.out.println("Usando fichero de proyecto: " + alt2.getPath());
                f2 = alt2;
            } else {
                System.out.println("Fichero no encontrado: " + archivo2);
                return;
            }
        }

        try {
            boolean iguales = compararArchivosLineaALinea(f1.getPath(), f2.getPath());
            if (iguales) {
                System.out.println("\nLos ficheros son IGUALES");
            } else {
                System.out.println("\nLos ficheros son DIFERENTES");
            }
        }
        catch (Exception e) {
            System.out.println("Error comparando ficheros: " + e.getMessage());
        }
    }

    private static boolean compararArchivosLineaALinea(String f1, String f2) throws Exception {
        try (java.io.BufferedReader br1 = new java.io.BufferedReader(new java.io.FileReader(f1));
             java.io.BufferedReader br2 = new java.io.BufferedReader(new java.io.FileReader(f2))) {

            String l1, l2;
            int linea = 1;

            while (true) {
                l1 = br1.readLine();
                l2 = br2.readLine();

                if (l1 == null && l2 == null) {
                    return true; // llegaron al final iguales
                }
                if (l1 == null || l2 == null) {
                    System.out.println("\nDiferencia en línea " + linea + ": longitud diferente");
                    return false;
                }
                if (!l1.equals(l2)) {
                    System.out.println("Diferencia en línea " + linea + ":");
                    System.out.println("  Archivo 1: " + l1);
                    System.out.println("  Archivo 2: " + l2);
                    return false;
                }
                linea++;
            }
        }
    }

}
