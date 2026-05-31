package model;

import domain.classes.model.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Arrays;
import java.util.List;

public class RespuestaTest {

    @Test
    public void testGetIdParticipante() {
        Respuesta respuesta = new Respuesta("user123");
        String result = respuesta.getIdParticipante();
        assertEquals("user123", result);
    }

    @Test
    public void testGetValores() {
        Respuesta respuesta = new Respuesta("user123");
        Map<String, ValorRespuesta> result = respuesta.getValores();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testPut() {
        Respuesta respuesta = new Respuesta("user123");

        // 1. Respuesta Textual
        ValorRespuestaTextual valTextual = new ValorRespuestaTextual("Texto de prueba");
        respuesta.put("pregunta1_texto", valTextual);

        // 2. Respuesta Numérica
        ValorRespuestaNumerica valNumerico = new ValorRespuestaNumerica(42.5);
        respuesta.put("pregunta2_num", valNumerico);

        // 3. Respuesta Opción Única (asumiendo que guarda el índice de la opción: 1)
        ValorRespuestaOpcionUnica valUnica = new ValorRespuestaOpcionUnica(1);
        respuesta.put("pregunta3_unica", valUnica);

        // 4. Respuesta Opción Múltiple (asumiendo que guarda una lista de índices: [0, 2])
        List<Integer> indices = Arrays.asList(0, 2);
        ValorRespuestaOpcionMultiple valMultiple = new ValorRespuestaOpcionMultiple(indices);
        respuesta.put("pregunta4_multiple", valMultiple);

        Map<String, ValorRespuesta> valores = respuesta.getValores();
        assertEquals(4, valores.size());

        // Aserciones para verificar que los tipos se guardaron correctamente

        // Pregunta 1: Texto
        assertTrue(valores.get("pregunta1_texto") instanceof ValorRespuestaTextual);
        // Opcional: Verificar el valor real casteando el objeto
        assertEquals("Texto de prueba", ((ValorRespuestaTextual) valores.get("pregunta1_texto")).getValor());

        // Pregunta 2: Numérica
        assertTrue(valores.get("pregunta2_num") instanceof ValorRespuestaNumerica);
        assertEquals(42.5, ((ValorRespuestaNumerica) valores.get("pregunta2_num")).getValor(), 0.001);

        // Pregunta 3: Opción Única
        assertTrue(valores.get("pregunta3_unica") instanceof ValorRespuestaOpcionUnica);
        assertEquals(1, ((ValorRespuestaOpcionUnica) valores.get("pregunta3_unica")).getIndiceSeleccionado());

        // Pregunta 4: Opción Múltiple
        assertTrue(valores.get("pregunta4_multiple") instanceof ValorRespuestaOpcionMultiple);
        assertEquals(indices, ((ValorRespuestaOpcionMultiple) valores.get("pregunta4_multiple")).getIndicesSeleccionados());
    }

    @Test
    public void testRemovePregunta() {
        Respuesta respuesta = new Respuesta("user123");
        ValorRespuestaTextual val1 = new ValorRespuestaTextual("valor1");
        ValorRespuestaNumerica val2 = new ValorRespuestaNumerica(99);

        respuesta.put("pregunta1", val1);
        respuesta.put("pregunta2", val2);

        respuesta.removePregunta("pregunta1");

        Map<String, ValorRespuesta> valores = respuesta.getValores();
        assertEquals(1, valores.size());
        assertFalse(valores.containsKey("pregunta1"));
        assertTrue(valores.containsKey("pregunta2"));
    }
}