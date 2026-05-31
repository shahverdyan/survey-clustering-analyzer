package persistence;

import domain.classes.model.Respuesta;
import persistence.classes.ResponseRepository;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

public class ResponseRepositoryTest {

    private ResponseRepository repository;
    private Respuesta mockRespuesta1;
    private Respuesta mockRespuesta2;

    @Before
    public void setUp() {
        repository = new ResponseRepository();

        mockRespuesta1 = mock(Respuesta.class);
        mockRespuesta2 = mock(Respuesta.class);
    }

    @Test
    public void testAdd() {
        repository.add("encuesta1", mockRespuesta1);

        int size = repository.size("encuesta1");
        assertEquals(1, size);
    }

    @Test
    public void testClear() {
        repository.add("encuesta1", mockRespuesta1);
        repository.clear("encuesta1");

        int size = repository.size("encuesta1");
        assertEquals(0, size);
    }

    @Test
    public void testSize() {
        repository.add("encuesta1", mockRespuesta1);
        repository.add("encuesta1", mockRespuesta2);

        int size = repository.size("encuesta1");
        assertEquals(2, size);
    }

    @Test
    public void testAll() {
        repository.add("encuesta1", mockRespuesta1);
        repository.add("encuesta1", mockRespuesta2);

        List<Respuesta> respuestas = repository.all("encuesta1");
        assertEquals(2, respuestas.size());
    }

    @Test
    public void testRemoveById() {
        when(mockRespuesta1.getIdParticipante()).thenReturn("user1");
        repository.add("encuesta1", mockRespuesta1);

        boolean removed = repository.removeById("encuesta1", "user1");
        assertTrue(removed);
    }

    //Para removeQuestion no es pot fer test, ja que no retorna res
}