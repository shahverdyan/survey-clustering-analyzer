package domain.classes.clustering;

import domain.classes.distance.Distance;
import domain.classes.vector.Vectorizer;

import java.util.*;

public abstract class ClusteringAlgorithm {
    protected final Random rnd;

    public ClusteringAlgorithm(long seed) {
        this.rnd = new Random(seed);
    }

    public abstract ClusterResult run(List<Vectorizer.Point> data, int k, Distance dist, int maxIter);

    protected boolean assignToCenters(List<Vectorizer.Point> data, List<Vectorizer.Point> centers, Distance dist,
                                      Map<String,Integer> assign){
        boolean changed = false;
        for (var p: data){
            int best = -1; double bd = Double.POSITIVE_INFINITY;
            for (int c=0; c<centers.size(); c++){
                double d = dist.dist(p, centers.get(c));
                if (d < bd){ bd = d; best = c; }
            }
            Integer prev = assign.put(p.id(), best);
            // La condición de cambio es importante para el bucle de K-Means.
            if (prev == null || prev != best) changed = true;
        }
        return changed; // Retorna si hubo cambios, útil para K-Means
    }

    protected List<Vectorizer.Point> sample(List<Vectorizer.Point> data, int k){
        List<Vectorizer.Point> copy = new ArrayList<>(data);
        Collections.shuffle(copy, rnd);
        return new ArrayList<>(copy.subList(0, k));
    }
}