package domain.classes.clustering;

import domain.classes.vector.Vectorizer;

import java.util.*;

public class ClusterResult {
    private final String algorithm;
    private final Map<String,Integer> assignment = new LinkedHashMap<>();
    private final List<Vectorizer.Point> representatives;

    public ClusterResult(String algorithm, Map<String,Integer> assignment, List<Vectorizer.Point> representatives) {
        this.algorithm = algorithm;
        this.assignment.putAll(assignment);
        this.representatives = representatives;
    }

    public String algorithm(){ return algorithm; }
    public Map<String,Integer> assignments(){ return Collections.unmodifiableMap(assignment); }
    public List<Vectorizer.Point> representatives(){ return representatives; }
}
