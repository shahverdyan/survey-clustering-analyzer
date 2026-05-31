package domain.classes.clustering;

import domain.classes.distance.Distance;
import domain.classes.vector.Vectorizer;

import java.util.*;

public class KMeansPlusPlus {
    private final Random rnd = new Random(7);

    public List<Vectorizer.Point> init(List<Vectorizer.Point> data, int k, Distance dist){
        if (k<=0 || k>data.size()) throw new IllegalArgumentException("k inválido");
        List<Vectorizer.Point> centers = new ArrayList<>();
        centers.add(data.get(rnd.nextInt(data.size())));

        double[] minD2 = new double[data.size()];
        Arrays.fill(minD2, Double.POSITIVE_INFINITY);

        while (centers.size() < k){
            var last = centers.get(centers.size()-1);
            for (int i=0;i<data.size();i++){
                double d = dist.dist(data.get(i), last);
                double d2 = d*d;
                if (d2 < minD2[i]) minD2[i] = d2;
            }
            double sum = 0; for (double v: minD2) sum += v;
            double r = rnd.nextDouble() * sum;
            double acc = 0;
            for (int i=0;i<data.size();i++){
                acc += minD2[i];
                if (acc >= r) { centers.add(data.get(i)); break; }
            }
        }
        return centers;
    }
}
