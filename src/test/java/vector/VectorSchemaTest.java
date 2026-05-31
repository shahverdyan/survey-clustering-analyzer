package vector;

import domain.classes.vector.VectorSchema;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class VectorSchemaTest {

    private VectorSchema vectorSchema;

    @Before
    public void setUp() {
        vectorSchema = new VectorSchema();
    }

    @Test
    public void testSetOneHot() {
        List<String> opciones = Arrays.asList("rojo", "azul", "verde");

        vectorSchema.setOneHot("color", opciones);

        List<String> resultado = vectorSchema.getOptions("color");
        assertEquals(3, resultado.size());
        assertEquals("rojo", resultado.get(0));
        assertEquals("azul", resultado.get(1));
        assertEquals("verde", resultado.get(2));
    }

    @Test
    public void testSetNumericRange() {
        vectorSchema.setNumericRange("edad", 18.0, 65.0);

        double[] rango = vectorSchema.getRange("edad");
        assertNotNull(rango);
        assertEquals(18.0, rango[0], 0.001);
        assertEquals(65.0, rango[1], 0.001);
    }

    @Test
    public void testGetOptions() {
        List<String> opciones = Arrays.asList("si", "no");
        vectorSchema.setOneHot("pregunta1", opciones);

        List<String> resultado = vectorSchema.getOptions("pregunta1");
        assertEquals(2, resultado.size());
        assertEquals("si", resultado.get(0));
        assertEquals("no", resultado.get(1));

        resultado = vectorSchema.getOptions("pregunta345");
        assertNotNull(resultado); //getOrdefault siempre devuelve una lista, aunque esté vacía
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetRange() {
        vectorSchema.setNumericRange("altura", 150.0, 200.0);

        double[] rango = vectorSchema.getRange("altura");
        assertNotNull(rango);
        assertEquals(150.0, rango[0], 0.001);
        assertEquals(200.0, rango[1], 0.001);

        rango = vectorSchema.getRange("pregunta23");
        assertNull(rango);
    }

    @Test
    public void testSetDimensions() {
        vectorSchema.setDimensions(10);

        int dimensiones = vectorSchema.getDimensions();
        assertEquals(10, dimensiones);
    }
}