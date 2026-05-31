package domain.controllers;

import domain.classes.clustering.ClusterResult;
import domain.classes.distance.GowerDistance;
import domain.classes.clustering.KMedoids;
import domain.classes.clustering.KMeans;
import domain.classes.evaluation.SSE;
import domain.classes.evaluation.Silhouette;
import domain.classes.model.Cuestionario;
import domain.classes.vector.Vectorizer;
import persistence.controllers.CtrlPersistencia;
import domain.classes.distance.Distance;

import java.util.List;


public class CtrlClustering {

    private final CtrlPersistencia ctrlPersistencia;
    private ClusterResult ultimoResultado;
    private String ultimoResultadoEncuestaId;

    private double ultimoSse = Double.NaN;
    private double ultimoSilhouette = Double.NaN;
    private long ultimoTiempoMs = -1;

    public CtrlClustering(CtrlPersistencia ctrlPersistencia) {
        this.ctrlPersistencia = ctrlPersistencia;
    }


    public Cuestionario getEncuesta(String id) {
        return ctrlPersistencia.Eget(id);
    }

    public int numRespuestas(String id) {
        return ctrlPersistencia.Rsize(id);
    }

    public ClusterResult getUltimoResultado() { return ultimoResultado; }
    public String getUltimoResultadoEncuestaId() { return ultimoResultadoEncuestaId; }
    public double getUltimoSse() { return ultimoSse; }
    public double getUltimoSilhouette() { return ultimoSilhouette; }
    public long getUltimoTiempoMs() { return ultimoTiempoMs; }

    public List<Vectorizer.Point> vectorizar(Cuestionario c) {
        var rs = ctrlPersistencia.Rall(c.getId());
        if (rs.isEmpty()) throw new IllegalStateException("No hay respuestas.");
        Vectorizer v = new Vectorizer();
        v.fit(c, rs);
        return v.transformAll(rs, c);
    }

    public double[] evaluar(ClusterResult res, List<Vectorizer.Point> datos) {
        if (res == null) {
            ultimoSse = Double.NaN;
            ultimoSilhouette = Double.NaN;
            return new double[]{Double.NaN, Double.NaN};
        }

        Distance dist = new GowerDistance();
        double sse = new SSE(dist).compute(res, datos);
        double sil = new Silhouette(dist).compute(res, datos);

        ultimoSse = sse;
        ultimoSilhouette = sil;

        return new double[]{sse, sil};
    }

    public ClusterResult ejecutarKMeans(Cuestionario c, int k, boolean kpp) {
        List<Vectorizer.Point> datos = vectorizar(c);
        var dist = new GowerDistance();
        var km = new KMeans();

        long t0 = System.nanoTime();
        ultimoResultado = km.run(datos, k, dist, kpp, 100);
        long t1 = System.nanoTime();

        ultimoTiempoMs = (t1 - t0) / 1_000_000L;
        ultimoResultadoEncuestaId  = c.getId();

        evaluar(ultimoResultado, datos);
        return ultimoResultado;
    }

    public ClusterResult ejecutarKMedoids(Cuestionario c, int k) {
        List<Vectorizer.Point> datos = vectorizar(c);
        var dist = new GowerDistance();
        var pam = new KMedoids();

        long t0 = System.nanoTime();
        ultimoResultado = pam.run(datos, k, dist, 50);
        long t1 = System.nanoTime();

        ultimoTiempoMs = (t1 - t0) / 1_000_000L;
        ultimoResultadoEncuestaId = c.getId();

        evaluar(ultimoResultado, datos);
        return ultimoResultado;
    }

    public List<Vectorizer.Point> calcularDatosUltimaEncuesta() {
        if (ultimoResultadoEncuestaId == null) return null;
        Cuestionario c = ctrlPersistencia.Eget(ultimoResultadoEncuestaId);
        if (c == null) return null;
        return vectorizar(c);
    }
}