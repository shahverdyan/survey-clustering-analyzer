package controllers;

import domain.controllers.CtrlDominio;
import domain.classes.model.Respuesta;
import persistence.controllers.CtrlPersistencia;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

public class CtrlRespuestaTest {

    private CtrlDominio ctrlDominio;
    private CtrlPersistencia ctrlPersistencia;
    private Respuesta mockRespuesta1;
    private Respuesta mockRespuesta2;

    @Before
    public void setUp() {
        ctrlPersistencia = mock(CtrlPersistencia.class);
        ctrlDominio = new CtrlDominio(ctrlPersistencia);
        mockRespuesta1 = mock(Respuesta.class);
        mockRespuesta2 = mock(Respuesta.class);
    }

    @Test
    public void testAgregar() {
        ctrlDominio.agregar("encuesta1", mockRespuesta1);

        verify(ctrlPersistencia).Radd("encuesta1", mockRespuesta1);
    }

    @Test
    public void testListar() {
        List<Respuesta> respuestasMock = Arrays.asList(mockRespuesta1, mockRespuesta2);
        when(ctrlPersistencia.Rall("encuesta1")).thenReturn(respuestasMock);

        List<Respuesta> resultado = ctrlDominio.listarRespuestas("encuesta1");

        assertEquals(2, resultado.size());
        assertSame(mockRespuesta1, resultado.get(0));
        assertSame(mockRespuesta2, resultado.get(1));
    }

    @Test
    public void testEliminarTodas() {
        ctrlDominio.eliminarTodas("encuesta1");
        verify(ctrlPersistencia).Rclear("encuesta1");
    }

    @Test
    public void testGetSize() {
        when(ctrlPersistencia.Rsize("encuesta1")).thenReturn(5);

        int size = ctrlDominio.getSize("encuesta1");

        assertEquals(5, size);
    }

    @Test
    public void testEliminarPorParticipante() {
        when(ctrlPersistencia.RremoveById("encuesta1", "user1")).thenReturn(true);

        boolean eliminado = ctrlDominio.eliminarPorParticipante("encuesta1", "user1");

        assertTrue(eliminado);
    }

    @Test
    public void testEliminarPregunta() {
        ctrlDominio.eliminarPreguntaR("encuesta1", "pregunta1");

        verify(ctrlPersistencia).RremoveQuestion("encuesta1", "pregunta1");
    }
}