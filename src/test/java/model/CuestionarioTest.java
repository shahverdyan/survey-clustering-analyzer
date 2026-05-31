package model;

import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.TipoPregunta;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;

public class CuestionarioTest {

    private Cuestionario cuestionario;
    private Pregunta pregunta1;
    private Pregunta pregunta2;

    @Before
    public void setUp() {
        cuestionario = new Cuestionario("test-id");

        List<String> opciones = Arrays.asList("op1", "op2", "op3");
        pregunta1 = new Pregunta("p1", "Enunciado 1", TipoPregunta.OPCION_UNICA, opciones, 1);
        pregunta2 = new Pregunta("p2", "Enunciado 2", TipoPregunta.OPCION_MULTIPLE, opciones, 3);
    }

    @Test
    public void testGetId() {
        String result = cuestionario.getId();
        assertEquals("test-id", result);
    }

    @Test
    public void testGetPreguntas() {
        List<Pregunta> result = cuestionario.getPreguntas();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testExistsPregunta() {
        boolean result = cuestionario.existsPregunta("p1");
        assertFalse(result);
    }

    @Test
    public void testAddPregunta() {
        cuestionario.addPregunta(pregunta1);

        assertTrue(cuestionario.existsPregunta("p1"));
        assertEquals(1, cuestionario.getPreguntas().size());
    }

    @Test
    public void testGetPregunta() {
        cuestionario.addPregunta(pregunta1);

        Pregunta result = cuestionario.getPregunta("p1");
        assertSame(pregunta1, result);
    }

    @Test
    public void testEliminarPregunta() {
        cuestionario.addPregunta(pregunta1);

        boolean result = cuestionario.eliminarPregunta("p1");
        assertTrue(result);
        assertFalse(cuestionario.existsPregunta("p1"));
    }

    @Test
    public void testReemplazarPregunta() {
        cuestionario.addPregunta(pregunta1);
        boolean result = cuestionario.reemplazarPregunta("p1", pregunta2);

        assertTrue(result);
        assertSame(pregunta2, cuestionario.getPregunta("p1"));
    }
}