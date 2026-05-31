package clustering;

import domain.classes.clustering.KMeansPlusPlus;
import domain.classes.distance.Distance;
import domain.classes.vector.Vectorizer;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

public class KMeansPlusPlusTest {

    private KMeansPlusPlus initializer;
    private List<Vectorizer.Point> mockData;
    private Distance mockDistance;

    @Before
    public void setUp() {
        initializer = new KMeansPlusPlus();

        mockData = new ArrayList<>();
        mockData.add(new Vectorizer.Point("p1", new double[]{1.0, 2.0}, new boolean[]{false, false}));
        mockData.add(new Vectorizer.Point("p2", new double[]{3.0, 4.0}, new boolean[]{false, false}));
        mockData.add(new Vectorizer.Point("p3", new double[]{5.0, 6.0}, new boolean[]{false, false}));
        mockData.add(new Vectorizer.Point("p4", new double[]{7.0, 8.0}, new boolean[]{false, false}));

        mockDistance = mock(Distance.class);
    }

    @Test
    public void testInit() {
        List<Vectorizer.Point> centers = initializer.init(mockData, 3, mockDistance);

        assertNotNull(centers);
        assertEquals(3, centers.size());
        assertTrue(mockData.containsAll(centers));
    }
}