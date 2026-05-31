package domain.classes.clustering;

import domain.classes.distance.Distance;
import domain.classes.vector.Vectorizer;

import java.util.*;

public class KMedoids extends ClusteringAlgorithm{
    public KMedoids() {
        super(23);
    }

    @Override
    public ClusterResult run(List<Vectorizer.Point> data, int k, Distance dist, int maxIter){
        List<Vectorizer.Point> medoids = sample(data, k);
        Map<String,Integer> assign = new LinkedHashMap<>();
        assignToCenters(data, medoids, dist, assign);

        for (int it=0; it<maxIter; it++){
            boolean improved = false;
            for (int i=0;i<k;i++){
                Vectorizer.Point mi = medoids.get(i);
                for (var cand : data){
                    if (medoids.contains(cand)) continue;
                    List<Vectorizer.Point> newMed = new ArrayList<>(medoids);
                    newMed.set(i, cand);

                    double oldCost = cost(data, medoids, dist);
                    double newCost = cost(data, newMed, dist);

                    if (newCost + 1e-9 < oldCost){
                        medoids = newMed;
                        assignToCenters(data, medoids, dist, assign);
                        improved = true;
                    }
                }
            }
            if (!improved) break;
        }
        return new ClusterResult("k-medoids", assign, medoids);
    }

    private double cost(List<Vectorizer.Point> data, List<Vectorizer.Point> medoids, Distance dist){
        double s=0;
        for (var p: data){
            double bd = Double.POSITIVE_INFINITY;
            for (var m: medoids) bd = Math.min(bd, dist.dist(p, m));
            s += bd;
        }
        return s;
    }
}
