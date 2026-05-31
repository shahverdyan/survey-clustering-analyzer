package distance;

import domain.classes.distance.GowerDistance;
import domain.classes.vector.Vectorizer;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GowerDistanceTest {

    @Test
    public void testDist() {
        GowerDistance gowerDistance = new GowerDistance();

        Vectorizer.Point mockPointA = mock(Vectorizer.Point.class);
        Vectorizer.Point mockPointB = mock(Vectorizer.Point.class);

        double[] valuesA = {0.1, 0.5, 0.8};
        double[] valuesB = {0.3, 0.5, 0.2};
        boolean[] missingA = {false, false, false};
        boolean[] missingB = {false, false, false};

        Vectorizer.Point pointA = new Vectorizer.Point("A", valuesA, missingA);
        Vectorizer.Point pointB = new Vectorizer.Point("B", valuesB, missingB);

        double result = gowerDistance.dist(pointA, pointB);

        assertTrue(result >= 0.0 && result <= 1.0);

        double expected = (Math.abs(0.1-0.3) + Math.abs(0.5-0.5) + Math.abs(0.8-0.2)) / 3.0;
        assertEquals(expected, result, 1e-9);
    }
}