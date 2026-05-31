package domain.classes.evaluation;

import domain.classes.clustering.ClusterResult;
import domain.classes.distance.Distance;
import domain.classes.vector.Vectorizer;

import java.util.*;

public class Silhouette {
    private final Distance dist;
    public Silhouette(Distance dist){ this.dist = dist; }

    public double compute(ClusterResult res, List<Vectorizer.Point> data){
        Map<String, Vectorizer.Point> byId = new HashMap<>();
        for (var p: data) byId.put(p.id(), p);

        Map<Integer, List<Vectorizer.Point>> groups = new HashMap<>();
        for (var e: res.assignments().entrySet()){
            groups.computeIfAbsent(e.getValue(), k->new ArrayList<>()).add(byId.get(e.getKey()));
        }

        double total=0; int n=0;
        for (var p: data){
            int cid = res.assignments().get(p.id());
            double a = avgDist(p, groups.get(cid));
            double b = Double.POSITIVE_INFINITY;
            for (var entry: groups.entrySet()){
                if (entry.getKey()==cid) continue;
                b = Math.min(b, avgDist(p, entry.getValue()));
            }
            double s = (b - a) / Math.max(a, b);
            total += s; n++;
        }
        return n==0? 0 : total/n;
    }

    private double avgDist(Vectorizer.Point p, List<Vectorizer.Point> list){
        if (list==null || list.size()<=1) return 0;
        double s=0; int c=0;
        for (var q: list){
            if (q==p) continue;
            s += dist.dist(p, q); c++;
        }
        return c==0? 0 : s/c;
    }
}
