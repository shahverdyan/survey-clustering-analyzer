package model;

import domain.classes.model.Pregunta;
import domain.classes.model.TipoPregunta;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class PreguntaTest {

    @Test
    public void testGetId() {
        List<String> opciones = Arrays.asList("Sí", "No");
        Pregunta pregunta = new Pregunta("p1", "¿Estas leyendo esto? Tengo hambre, voy a cenar", TipoPregunta.OPCION_UNICA, opciones, 1);

        String result = pregunta.getId();
        assertEquals("p1", result);
    }

    @Test
    public void testGetEnunciado() {
        List<String> opciones = Arrays.asList("Sí", "No");
        Pregunta pregunta = new Pregunta("p1", "¿Sigues leyendo esto? La verdad no se si hacer mas easter eggs de estos por si repercute negativamente en la nota, espero que no xd, por si acaso voy a dejar de hacerlos (de momento :0)", TipoPregunta.OPCION_UNICA, opciones, 1);

        String result = pregunta.getEnunciado();
        assertEquals("¿Sigues leyendo esto? La verdad no se si hacer mas easter eggs de estos por si repercute negativamente en la nota, espero que no xd, por si acaso voy a dejar de hacerlos (de momento :0)", result);
    }

    @Test
    public void testGetTipo() {
        List<String> opciones = Arrays.asList("Sí", "No");
        Pregunta pregunta = new Pregunta("p1", "¿Te gusta Java?", TipoPregunta.OPCION_UNICA, opciones, 1);

        TipoPregunta result = pregunta.getTipo();
        assertEquals(TipoPregunta.OPCION_UNICA, result);
    }

    @Test
    public void testGetOpciones() {
        List<String> opciones = Arrays.asList("Opción A", "Opción B", "Opción C");
        Pregunta pregunta = new Pregunta("p1", "Selecciona una opción", TipoPregunta.OPCION_MULTIPLE, opciones, 2);

        List<String> result = pregunta.getOpciones();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Opción A", result.get(0));
        assertEquals("Opción B", result.get(1));
        assertEquals("Opción C", result.get(2));
    }

    @Test
    public void testGetMaxSeleccion() {
        List<String> opciones = Arrays.asList("A", "B", "C", "D");
        Pregunta pregunta = new Pregunta("p1", "Selecciona máximo 2", TipoPregunta.OPCION_MULTIPLE, opciones, 2);

        int result = pregunta.getMaxSeleccion();
        assertEquals(2, result);
    }
}