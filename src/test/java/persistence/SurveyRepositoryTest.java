package persistence;

import domain.classes.model.Cuestionario;
import persistence.classes.SurveyRepository;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;

public class SurveyRepositoryTest {

    private SurveyRepository repository;
    private Cuestionario mockCuestionario1;
    private Cuestionario mockCuestionario2;

    @Before
    public void setUp() {
        repository = new SurveyRepository();

        mockCuestionario1 = mock(Cuestionario.class);
        mockCuestionario2 = mock(Cuestionario.class);

        when(mockCuestionario1.getId()).thenReturn("encuesta1");
        when(mockCuestionario2.getId()).thenReturn("encuesta2");
    }

    @Test
    public void testList() {
        repository.put(mockCuestionario1);
        repository.put(mockCuestionario2);

        Collection<Cuestionario> result = repository.list();
        assertEquals(2, result.size());
    }

    @Test
    public void testCreate() {
        repository.create("nueva_encuesta");

        boolean exists = repository.exists("nueva_encuesta");
        assertTrue(exists);
    }

    @Test
    public void testPut() {
        repository.put(mockCuestionario1);

        Cuestionario result = repository.get("encuesta1");
        assertSame(mockCuestionario1, result);
    }

    @Test
    public void testDelete() {
        repository.put(mockCuestionario1);

        boolean deleted = repository.delete("encuesta1");
        assertTrue(deleted);

        boolean exists = repository.exists("encuesta1");
        assertFalse(exists);
    }

    @Test
    public void testExists() {
        repository.put(mockCuestionario1);

        boolean exists = repository.exists("encuesta1");
        assertTrue(exists);

        boolean notExists = repository.exists("encuesta2");
        assertFalse(notExists);
    }

    @Test
    public void testGet() {
        repository.put(mockCuestionario1);

        Cuestionario result = repository.get("encuesta1");
        assertSame(mockCuestionario1, result);

        Cuestionario nullResult = repository.get("encuesta2");
        assertNull(nullResult);
    }
}