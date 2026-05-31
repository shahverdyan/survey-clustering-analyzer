package clustering;

import domain.classes.clustering.ClusterResult;
import domain.classes.vector.Vectorizer;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

public class ClusterResultTest {

    private ClusterResult clusterResult;
    private Map<String, Integer> testAssignments;
    private List<Vectorizer.Point> testRepresentatives;

    @Before
    public void setUp() {
        testAssignments = new LinkedHashMap<>();
        testAssignments.put("user1", 0);
        testAssignments.put("user2", 1);
        testAssignments.put("user3", 0);

        Vectorizer.Point mockPoint1 = mock(Vectorizer.Point.class);
        Vectorizer.Point mockPoint2 = mock(Vectorizer.Point.class);
        testRepresentatives = Arrays.asList(mockPoint1, mockPoint2);

        clusterResult = new ClusterResult("k-means", testAssignments, testRepresentatives);
    }

    @Test
    public void testAlgorithm() {
        assertEquals("k-means", clusterResult.algorithm());
    }

    @Test
    public void testAssignments() {
        Map<String, Integer> assignments = clusterResult.assignments();
        assertEquals(3, assignments.size());
        assertEquals(Integer.valueOf(0), assignments.get("user1"));
        assertEquals(Integer.valueOf(1), assignments.get("user2"));
        assertEquals(Integer.valueOf(0), assignments.get("user3"));

        try {
            assignments.put("test", 1);
            fail("Falla el put en el map");
        } catch (UnsupportedOperationException e) {}
    }

    @Test
    public void testRepresentatives() {
        List<Vectorizer.Point> representatives = clusterResult.representatives();
        assertEquals(2, representatives.size());
    }
}
