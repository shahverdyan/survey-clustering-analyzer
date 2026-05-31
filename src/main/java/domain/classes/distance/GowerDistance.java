package domain.classes.distance;

import domain.classes.vector.Vectorizer;

public class GowerDistance implements Distance {
    @Override
    // Submetodo para calcular la distancia entre dos valores numéricos normalizados
    public double calcularDistanciaNumerica(double valA, double valB) {
        return Math.abs(valA - valB);
    }

    @Override
    public double dist(Vectorizer.Point a, Vectorizer.Point b) {
        double[] x = a.values(), y = b.values();
        boolean[] mx = a.missing(), my = b.missing();
        double sum=0; int cnt=0;
        for (int i=0;i<x.length;i++){
            if (mx[i] || my[i]) continue;
            sum += calcularDistanciaNumerica(x[i], y[i]); // valores normalizados [0,1]
            cnt++;
        }
        if (cnt==0) return 1.0;
        return sum / cnt;
    }
}
