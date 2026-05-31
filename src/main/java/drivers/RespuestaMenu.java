package drivers;

import domain.classes.model.ValorRespuesta;
import domain.controllers.CtrlDominio;
import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.Respuesta;
import domain.classes.util.ConsoleIO;
import domain.classes.model.ValorRespuestaNumerica;
import domain.classes.model.ValorRespuestaTextual;
import domain.classes.model.ValorRespuestaOpcionUnica;
import domain.classes.model.ValorRespuestaOpcionMultiple;

import java.util.*;

public class RespuestaMenu {

    private final ConsoleIO io;
    private final CtrlDominio ctrlDominio;
    private final ConsoleIO.Modo modo;

    public RespuestaMenu(ConsoleIO io, CtrlDominio ctrlDominio, ConsoleIO.Modo modo) {
        this.io = io;
        this.ctrlDominio = ctrlDominio;
        this.modo = modo;
    }

    public void ejecutar() {
        Cuestionario en = FuncionesEncuesta.seleccionarEncuesta(io, ctrlDominio);
        if (en == null) return; //La respuesta tiene que pertenecer a una encuesta existente

        while (true) {
            if(modo == ConsoleIO.Modo.MANUAL) {
                System.out.println("\nRespuestas de " + en.getId());
                System.out.println("1) Agregar respuesta");
                System.out.println("2) Listar respuestas");
                System.out.println("3) Eliminar respuesta de un participante");
                System.out.println("4) Eliminar todas las respuestas");
                System.out.println("0) Volver");
            }
            String opcion = io.prompt("Opcion");
            switch (opcion) {
                case "1" -> agregarRespuesta(en);
                case "2" -> listarRespuestas(en);
                case "3" -> {
                    String p = io.prompt("Participante a eliminar");
                    if (p.isBlank()) {
                        System.out.println("Id de participante vacio");
                    }
                    else {
                        if (ctrlDominio.eliminarPorParticipante(en.getId(), p)) {
                            System.out.println("Eliminada respuesta del participante " + p);
                        }
                        else {
                            System.out.println("No encontrada");
                        }

                    }
                }
                case "4" -> {
                    ctrlDominio.eliminarTodas(en.getId());
                    System.out.println("Respuestas borradas de la encuesta: " + en.getId());
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    private void agregarRespuesta(Cuestionario en) {
        if (en.getPreguntas().isEmpty()) {
            System.out.println("La encuesta no tiene preguntas");
            return; //No se pueden agregar respuestas a encuestas sin preguntas
        }

        if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Creando nueva respuesta para la encuesta " + en.getId());

        // Obtener el identificador del participante: preferimos usar el email del usuario logueado
        String par = null;
        try {
            par = ctrlDominio.getUsuarioLogueadoEmail();
        } catch (Exception ignored) {
            // Si por alguna razón no está disponible, seguiremos con el fallback
        }

        if (par == null || par.isBlank()) {
            // Fallback al comportamiento anterior: generar id por defecto y preguntar al usuario
            String s = "resp" + (ctrlDominio.getSize(en.getId()) + 1);
            String respuestaPrompt = io.promptDefault("Id de participante", s);
            if (respuestaPrompt == null || respuestaPrompt.isBlank()) par = s;
            else par = respuestaPrompt;
        } else {
            if (modo == ConsoleIO.Modo.MANUAL) System.out.println("Usando participante logueado: " + par);
        }

        Respuesta r = new Respuesta(par);

        for (Pregunta p : en.getPreguntas()) {
            if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Pregunta (" + p.getTipo() + ")");
            if(modo == ConsoleIO.Modo.MANUAL) System.out.println(p.getEnunciado());

            switch (p.getTipo()) {
                case NUMERICA -> {
                    String v = io.prompt("Valor numerico (enter = sin respuesta)");
                    if (!v.isBlank()) {
                        try {
                            double d = Double.parseDouble(v);
                            r.put(p.getId(), new ValorRespuestaNumerica(d));
                        } catch (NumberFormatException e) {
                            if(modo == ConsoleIO.Modo.MANUAL) System.out.println("No es un numero valido, se omite la respuesta");
                        }
                    }
                }
                case TEXTO -> {
                    String v = io.prompt("Texto (enter = sin respuesta)");
                    if (!v.isBlank()) r.put(p.getId(), new ValorRespuestaTextual(v));
                }
                case OPCION_UNICA -> {
                    if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Opciones disponibles: " + p.getOpciones());
                    String v = io.prompt("Escribe una opción tal cual (enter = sin respuesta)");

                    if (!v.isBlank()) {
                        int indice = p.getOpciones().indexOf(v);
                        if (indice != -1 && p.getOpciones().contains(v)) {
                            r.put(p.getId(), new ValorRespuestaOpcionUnica(indice));
                        }
                        else {
                            if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Opción no válida. No se añade respuesta para esta pregunta.");
                        }
                    }
                }
                case OPCION_MULTIPLE -> {
                    if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Opciones disponibles: " + p.getOpciones());
                    if (p.getMaxSeleccion() > 0 && modo == ConsoleIO.Modo.MANUAL) System.out.println("Maximo a seleccionar: " + p.getMaxSeleccion());

                    String v = io.prompt("Selecciona separando con '|' (enter = sin respuesta)");
                    if (!v.isBlank()) {
                        List<String> sel = new ArrayList<>(Arrays.asList(v.split("\\|"))); //opciones seleccionadas
                        sel.replaceAll(String::trim); //eliminar espacios

                        List<Integer> indicesValidos = new ArrayList<>();
                        for (String opcion : sel) {
                            int indice = p.getOpciones().indexOf(opcion);
                            if (indice != -1) {
                                indicesValidos.add(indice);
                            } else {
                                if(modo == ConsoleIO.Modo.MANUAL) System.out.println("La opción '" + opcion + "' no es válida y se descarta.");
                            }
                        }

                        // Verificar que queda al menos una válida
                        if (indicesValidos.isEmpty()) {
                            if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Ninguna opción ingresada es válida. No se añade respuesta a esta pregunta.");
                            // No usamos break porque queremos continuar al siguiente ciclo del for.
                        } else {
                            //Se seleccionan mas opciones del maximo
                            if (p.getMaxSeleccion() > 0 && indicesValidos.size() > p.getMaxSeleccion()) {
                                if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Has seleccionado mas de " + p.getMaxSeleccion() + "opciones, se guardan las primeras");
                                indicesValidos = indicesValidos.subList(0, p.getMaxSeleccion());
                            }
                            r.put(p.getId(), new ValorRespuestaOpcionMultiple(indicesValidos));
                        }
                    }
                }
            }
        }

        ctrlDominio.agregar(en.getId(), r);
        System.out.println("Respuesta agregada a la encuesta '" + en.getId() + "' con id de participante: " + par);
    }

    private void listarRespuestas(Cuestionario en) {
        List<Respuesta> resp = ctrlDominio.listarRespuestas(en.getId());
        if (resp.isEmpty()) {
            System.out.println("No hay respuestas para esta encuesta");
            return;
        }

        System.out.println("Respuestas de la encuesta " + en.getId() + ": " + resp.size());

        StringBuilder header = new StringBuilder("idParticipante");
        for (Pregunta p : en.getPreguntas()) {
            header.append(",").append(p.getId());
        }
        System.out.println(header);

        for (Respuesta r : resp) {
            StringBuilder fila = new StringBuilder();
            fila.append(r.getIdParticipante());
            for (Pregunta p : en.getPreguntas()) {
                fila.append(",");
                ValorRespuesta vr = r.getValores().get(p.getId());
                if (vr != null) {
                    fila.append(vr.getValorNormalizado());
                } // si es null, dejamos la celda vacía (ya se añadió la coma)
            }
            System.out.println(fila);
        }

    }


}
