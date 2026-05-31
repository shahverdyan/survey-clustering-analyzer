package domain.classes.distance;

import domain.classes.vector.Vectorizer;

public interface Distance {
    double calcularDistanciaNumerica(double a, double b);
    double dist(Vectorizer.Point a, Vectorizer.Point b);
}
