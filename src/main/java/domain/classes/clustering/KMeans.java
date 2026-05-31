package domain.classes.clustering;

import domain.classes.distance.Distance;
import domain.classes.vector.Vectorizer;

import java.util.*;

public class KMeans extends ClusteringAlgorithm{
    public KMeans() {
        super(13);
    }

    public ClusterResult run(List<Vectorizer.Point> data, int k, Distance dist, boolean useKPP, int maxIter){
        List<Vectorizer.Point> centers;
        if (useKPP) centers = new KMeansPlusPlus().init(data, k, dist);
        else centers = sample(data, k);

        Map<String,Integer> assign = new LinkedHashMap<>();

        for (int it=0; it<maxIter; it++){
            boolean changed = assignToCenters(data, centers, dist, assign);
            List<Vectorizer.Point> newCenters = recomputeCenters(data, assign, k);
            if (!changed || same(centers, newCenters)) { centers = newCenters; break; }
            centers = newCenters;
        }
        return new ClusterResult("k-means" + (useKPP?"++":""), assign, centers);
    }

    @Override
    public ClusterResult run(List<Vectorizer.Point> data, int k, Distance dist, int maxIter) {
        return run(data, k, dist, false, maxIter);
    }

    private List<Vectorizer.Point> recomputeCenters(List<Vectorizer.Point> data, Map<String,Integer> assign, int k){
        int dim = data.get(0).values().length;
        double[][] sum = new double[k][dim];
        boolean[][] miss = new boolean[k][dim];
        int[] cnt = new int[k];

        Map<String, Vectorizer.Point> byId = new HashMap<>();
        for (var p : data) byId.put(p.id(), p);

        for (int c=0;c<k;c++) Arrays.fill(miss[c], true);

        for (var e: assign.entrySet()){
            int c = e.getValue();
            var p = byId.get(e.getKey());
            double[] v = p.values();
            boolean[] m = p.missing();
            for (int i=0;i<dim;i++){
                if (!m[i]) { sum[c][i] += v[i]; miss[c][i] = false; }
            }
            cnt[c]++;
        }

        List<Vectorizer.Point> out = new ArrayList<>();
        for (int c=0;c<k;c++){
            double[] v = new double[dim];
            boolean[] m = new boolean[dim];
            for (int i=0;i<dim;i++){
                if (miss[c][i] || cnt[c]==0) { v[i]=0; m[i]=true; }
                else { v[i] = sum[c][i] / cnt[c]; m[i]=false; }
            }
            out.add(new Vectorizer.Point("centro_"+c, v, m));
        }
        return out;
    }


    private boolean same(List<Vectorizer.Point> a, List<Vectorizer.Point> b){
        if (a.size()!=b.size()) return false;
        for (int i=0;i<a.size();i++){
            double[] va = a.get(i).values(), vb = b.get(i).values();
            if (va.length!=vb.length) return false;
            for (int j=0;j<va.length;j++) if (Math.abs(va[j]-vb[j])>1e-9) return false;
        }
        return true;
    }
}
