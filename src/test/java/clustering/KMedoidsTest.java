package clustering;

import domain.classes.clustering.ClusterResult;
import domain.classes.clustering.KMedoids;
import domain.classes.distance.Distance;
import domain.classes.vector.Vectorizer;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

public class KMedoidsTest {

    private KMedoids kMedoids;
    private List<Vectorizer.Point> mockData;
    private Distance mockDistance;

    @Before
    public void setUp() {
        kMedoids = new KMedoids();

        mockData = new ArrayList<>();
        mockData.add(new Vectorizer.Point("p1", new double[]{1.0, 2.0}, new boolean[]{false, false}));
        mockData.add(new Vectorizer.Point("p2", new double[]{3.0, 4.0}, new boolean[]{false, false}));
        mockData.add(new Vectorizer.Point("p3", new double[]{5.0, 6.0}, new boolean[]{false, false}));

        mockDistance = mock(Distance.class);
    }

    @Test
    public void testRun() {
        ClusterResult result = kMedoids.run(mockData, 2, mockDistance, 10);

        assertNotNull(result);
        assertEquals("k-medoids", result.algorithm());
        assertTrue(result.assignments().size() > 0);
        assertEquals(2, result.representatives().size());
    }
}