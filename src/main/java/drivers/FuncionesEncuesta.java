package drivers;

import domain.classes.model.Cuestionario;
import domain.classes.util.ConsoleIO;

import java.util.Collection;

import domain.controllers.CtrlDominio;

public class FuncionesEncuesta {

    //Muestra encuestas disponibles y devuelve false si no hay ninguna
    public static boolean mostrarEncuestasDisponibles(CtrlDominio ctrlDominio, ConsoleIO io) {
        Collection<Cuestionario> en = ctrlDominio.listar();
        if (en.isEmpty()) {
            System.out.println("No hay encuestas");
            return false;
        }

        System.out.println("Encuestas disponibles:");
        for (Cuestionario c : en) { //Se itera sobre la collection de encuestas
            int numResp = ctrlDominio.getSize(c.getId());
            System.out.println(" - " + c.getId() + " (preguntas=" + c.getPreguntas().size() + ", respuestas=" + numResp + ")");
        }
        return true;
    }

    //Se selecciona una encuesta por id, devolviendo null si no existe o no hay encuestas
    public static Cuestionario seleccionarEncuesta(ConsoleIO io, CtrlDominio ctrlDominio) {
        if (!mostrarEncuestasDisponibles(ctrlDominio, io)) return null; //No hay encuestas

        String id = io.prompt("Id de la encuesta");
        Cuestionario en = ctrlDominio.obtener(id);
        if (en == null) {
            if (io.getModo() == ConsoleIO.Modo.MANUAL) System.out.println("No existe encuesta con id '" + id);
            return null;
        }
        return en;
    }
}
