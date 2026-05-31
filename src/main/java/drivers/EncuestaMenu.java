package drivers;

import domain.controllers.CtrlDominio;
import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.TipoPregunta;
import domain.classes.util.ConsoleIO;

import java.util.*;

public class EncuestaMenu {
    private final ConsoleIO io;
    private final CtrlDominio ctrlDominio;
    private final ConsoleIO.Modo modo;

    public EncuestaMenu(ConsoleIO io, CtrlDominio ctrlDominio, ConsoleIO.Modo modo) {
        this.io = io;
        this.modo = modo;
        this.ctrlDominio = ctrlDominio;
    }

    public void ejecutar() {
        while (true) {
            if(modo == ConsoleIO.Modo.MANUAL) {
                System.out.println("\nEncuestas");
                System.out.println("1) Crear encuesta");
                System.out.println("2) Listar encuestas");
                System.out.println("3) Editar encuesta");
                System.out.println("4) Eliminar encuesta");
                System.out.println("0) Volver");
            }
            String opcion = io.prompt("Opcion");
            switch (opcion) { //Se escoge que hacer con las encuestas
                case "1" -> crearEncuestaConPreguntas();
                case "2" -> listarEncuestas();
                case "3" -> editarEncuesta();
                case "4" -> eliminarEncuesta();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    //Encuestas

    private void crearEncuestaConPreguntas() {
        if (!ctrlDominio.estaLogueado()) {
            System.out.println("Debes iniciar sesión para crear encuestas.");
            return;
        }
        String id = io.prompt("Id de la nueva encuesta");
        if (id.isBlank()) {
            if(modo == ConsoleIO.Modo.MANUAL) System.out.println("El id no puede estar vacio");
            return;
        }
        try {
            ctrlDominio.crear(id); //Se crea encuesta nueva
        }
        catch (Exception e) {
            System.out.println("Error al crear encuesta: " + e.getMessage());
            return;
        }
        Cuestionario en = ctrlDominio.obtener(id); //Se obtiene la encuesta creada
        System.out.println("Encuesta creada: " + id);

        while (true) {
            if(modo == ConsoleIO.Modo.MANUAL) {
                System.out.println("1) Agregar pregunta");
                System.out.println("2) Listar preguntas");
                System.out.println("0) Terminar");
            }
            String opcion = io.prompt("Opcion");
            switch (opcion){ //Se escoge que hacer con las preguntas
                case "1" -> agregarPregunta(en);
                case "2" -> listarPreguntas(en);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    private void listarEncuestas() {
        FuncionesEncuesta.mostrarEncuestasDisponibles(ctrlDominio, io);
    }

    private void editarEncuesta() {
        if (!ctrlDominio.estaLogueado()) {
            System.out.println("Debes iniciar sesión para editar encuestas.");
            return;
        }
        Cuestionario en = FuncionesEncuesta.seleccionarEncuesta(io, ctrlDominio);
        if (en == null) return;

        while (true) {
            if(modo == ConsoleIO.Modo.MANUAL) {
                System.out.println("Editando '" + en.getId());
                System.out.println("1) Listar preguntas");
                System.out.println("2) Agregar pregunta");
                System.out.println("3) Eliminar pregunta");
                System.out.println("0) Volver");
            }
            String op = io.prompt("Opcion");
            switch (op) {
                case "1" -> listarPreguntas(en);
                case "2" -> agregarPregunta(en);
                case "3" -> eliminarPregunta(en);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    private void eliminarEncuesta() {
        if (!ctrlDominio.estaLogueado()) {
            System.out.println("Debes iniciar sesión para eliminar encuestas.");
            return;
        }
        if (!FuncionesEncuesta.mostrarEncuestasDisponibles(ctrlDominio, io)) return; //No hay encuestas

        String id = io.prompt("Id a eliminar");
        if (id.isBlank()) {
            System.out.println("Id vacio, no se elimina nada");
            return;
        }
        if (ctrlDominio.eliminar(id)) {
            ctrlDominio.eliminarTodas(id);
            System.out.println("Encuesta '" + id + "' eliminada y respuestas borradas");
        }
        else {
            System.out.println("No existe encuesta con id '" + id);
        }
    }

    //Preguntas

    private void listarPreguntas(Cuestionario en) {
        if (en.getPreguntas().isEmpty()) {
            System.out.println("No hay preguntas");
            return;
        }

        System.out.println("Listando preguntas de la encuesta '" + en.getId() + "':");
        List<Pregunta> preguntas = en.getPreguntas();
        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            System.out.println((i+1) + ") " + p.getEnunciado() + " " + p.getTipo());
            if (p.getOpciones() != null && !p.getOpciones().isEmpty()) {
                System.out.print("Opciones: " + p.getOpciones());
                if (p.getTipo() == TipoPregunta.OPCION_MULTIPLE) { //Tienen una seleccion maxima
                    System.out.print(" (max=" + p.getMaxSeleccion() + ")");
                }
                System.out.println();
            }
        }
    }

    private TipoPregunta leerTipoPregunta() {
        String prompt = "Tipo (OPCION_UNICA, OPCION_MULTIPLE, NUMERICA, TEXTO)";
        while (true) {
            String entrada = io.prompt(prompt).trim(); //Se lee el tipo de pregunta
            if (entrada.isEmpty()) {
                System.out.println("Debes indicar un tipo de pregunta");
                continue;
            }
            try {
                String tp = entrada.trim().toUpperCase();
                return TipoPregunta.valueOf(tp); //Tipo existe en enum TipoPregunta
            }
            catch (IllegalArgumentException e) {
                System.out.println("Tipo invalido. Tipos validos: OPCION_UNICA, OPCION_MULTIPLE, NUMERICA, TEXTO");
            }
        }
    }

    private void agregarPregunta(Cuestionario en) {
        String enun = io.prompt("Texto de la pregunta (identificador en el CSV)");
        if (enun.isBlank()) {
            System.out.println("La pregunta no puede estar vacia");
            return;
        }
        if (en.getPregunta(enun) != null) {
            System.out.println("Ya existe una pregunta con ese texto. Usa otro enunciado");
            return;
        }

        TipoPregunta tipo = leerTipoPregunta();
        List<String> opciones = new ArrayList<>();
        int maxSel = 0;

        if (tipo == TipoPregunta.OPCION_UNICA || tipo == TipoPregunta.OPCION_MULTIPLE) {
            String entrada = io.prompt("Opciones separadas por | (ej. Opcion1|Opcion2|Opcion3)");
            if (!entrada.isBlank()) {
                opciones = Arrays.asList(entrada.split("\\|"));
            }
            if (tipo == TipoPregunta.OPCION_MULTIPLE) {
                String ms = io.prompt("Maximo a seleccionar (0 = sin limite)");
                if (!ms.isBlank()) {
                    try {
                        maxSel = Integer.parseInt(ms.trim()); //Se pasa la entrada a entero
                        if (maxSel < 0) {
                            System.out.println("No puede ser negativo, se deja a 0 (sin limite)");
                            maxSel = 0;
                        }
                    }
                    catch (NumberFormatException e) {
                        System.out.println("Valor no valido, se deja a 0 (sin limite)");
                        maxSel = 0;
                    }
                }
            }
        }

        try {
            ctrlDominio.agregarPregunta(en.getId(), new Pregunta(enun, enun, tipo, opciones, maxSel)); //Se agrega la pregunta a la encuesta
            System.out.println("Pregunta agregada");
        }
        catch (IllegalArgumentException e) {
            System.out.println("Error al agregar pregunta: " + e.getMessage());
        }
    }

    private void eliminarPregunta(Cuestionario en) {
        String id = io.prompt("Id de pregunta a eliminar (texto exacto de la pregunta):");
        if (id.isBlank()) {
            System.out.println("Id vacio, no se elimina nada");
            return;
        }
        if (ctrlDominio.eliminarPregunta(en.getId(), id)) {
            ctrlDominio.eliminarPregunta(en.getId(), id);
            System.out.println("Pregunta eliminada.");
        }
        else {
            System.out.println("No existe pregunta con ese id");
        }
    }
}
