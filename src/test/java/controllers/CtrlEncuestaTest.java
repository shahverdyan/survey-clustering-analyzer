package controllers;

import persistence.controllers.CtrlPersistencia;
import domain.controllers.CtrlDominio;
import domain.classes.model.Pregunta;
import domain.classes.model.Cuestionario;
import domain.classes.model.Usuario;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;

public class CtrlEncuestaTest {

    private CtrlDominio ctrlDominio;
    private CtrlPersistencia ctrlPersistencia;
    private Cuestionario mockCuestionario;
    private Pregunta mockPregunta;
    private Usuario mockUsuario;

    @Before
    public void setUp() {
        ctrlPersistencia = mock(CtrlPersistencia.class);
        ctrlDominio = new CtrlDominio(ctrlPersistencia);
        mockCuestionario = mock(Cuestionario.class);
        mockPregunta = mock(Pregunta.class);
        mockUsuario = mock(Usuario.class);

        when(ctrlPersistencia.Uexists("user@test.com")).thenReturn(true);
        when(ctrlPersistencia.Uget("user@test.com")).thenReturn(mockUsuario);
        when(mockUsuario.checkPassword("1234")).thenReturn(true);
        when(mockUsuario.getEmail()).thenReturn("user@test.com");

        boolean logged = ctrlDominio.login("user@test.com", "1234");
        assertTrue("El login de prueba deberia funcionar", logged);
    }

    @Test
    public void testCrear() {
        when(ctrlPersistencia.Eget("encuesta1")).thenReturn(mockCuestionario);

        ctrlDominio.crear("encuesta1");

        verify(ctrlPersistencia).Ecreate("encuesta1");
        verify(mockCuestionario).setOwner("user@test.com");
        verify(ctrlPersistencia).Eput(mockCuestionario);
    }

    @Test
    public void testObtener() {
        when(ctrlPersistencia.Eget("encuesta1")).thenReturn(mockCuestionario);

        Cuestionario resultado = ctrlDominio.obtener("encuesta1");

        assertSame(mockCuestionario, resultado);
    }

    @Test
    public void testListar() {
        Collection<Cuestionario> encuestasMock = Arrays.asList(mockCuestionario);
        when(ctrlPersistencia.Elist()).thenReturn(encuestasMock);

        Collection<Cuestionario> resultado = ctrlDominio.listar();

        assertEquals(1, resultado.size());
        assertTrue(resultado.contains(mockCuestionario));
    }

    @Test
    public void testGuardar() {
        when(mockCuestionario.getOwner()).thenReturn(null);

        ctrlDominio.guardar(mockCuestionario);

        verify(ctrlPersistencia).Eput(mockCuestionario);
    }

    @Test
    public void testEliminar() {
        when(ctrlPersistencia.Eget("encuesta1")).thenReturn(mockCuestionario);
        when(mockCuestionario.getOwner()).thenReturn("user@test.com");
        when(ctrlPersistencia.Edelete("encuesta1")).thenReturn(true);

        boolean eliminado = ctrlDominio.eliminar("encuesta1");

        assertTrue(eliminado);
    }

    @Test
    public void testExiste() {
        when(ctrlPersistencia.Eexists("encuesta1")).thenReturn(true);

        boolean existe = ctrlDominio.existe("encuesta1");

        assertTrue(existe);
    }

    @Test
    public void testAgregarPregunta() {
        when(ctrlPersistencia.Eget("encuesta1")).thenReturn(mockCuestionario);
        when(mockCuestionario.getOwner()).thenReturn("user@test.com");

        ctrlDominio.agregarPregunta("encuesta1", mockPregunta);

        verify(mockCuestionario).addPregunta(mockPregunta);
        verify(ctrlPersistencia).Eput(mockCuestionario);
    }

    @Test
    public void testEliminarPregunta() {
        when(ctrlPersistencia.Eget("encuesta1")).thenReturn(mockCuestionario);
        when(mockCuestionario.getOwner()).thenReturn("user@test.com");
        when(mockCuestionario.eliminarPregunta("pregunta1")).thenReturn(true);

        boolean eliminado = ctrlDominio.eliminarPregunta("encuesta1", "pregunta1");

        assertTrue(eliminado);
        verify(ctrlPersistencia).Eput(mockCuestionario);
    }
}