package drivers;

import domain.classes.clustering.ClusterResult;
import domain.controllers.CtrlDominio;
import domain.classes.distance.Distance;
import domain.classes.distance.GowerDistance;
import domain.classes.model.Cuestionario;
import domain.classes.model.Respuesta;
import domain.classes.impexp.CsvClusterExporter;
import domain.classes.util.ConsoleIO;
import domain.classes.vector.Vectorizer;
import domain.classes.evaluation.ClusteringAccuracy;

import java.nio.file.Path;
import java.util.*;

public class ClusteringMenu {

    private final ConsoleIO io;
    private final CtrlDominio ctrlDominio;
    private final ConsoleIO.Modo modo;

    public ClusteringMenu(ConsoleIO io, CtrlDominio ctrlDominio, ConsoleIO.Modo modo) {
        this.io = io;
        this.ctrlDominio = ctrlDominio;
        this.modo = modo;
    }

    public void ejecutar() {
        Cuestionario c = FuncionesEncuesta.seleccionarEncuesta(io, ctrlDominio);
        if (c == null) return;

        while (true) {
            if (modo == ConsoleIO.Modo.MANUAL) {
                System.out.println("\nClustering sobre " + c.getId());
                System.out.println("Respuestas: " + ctrlDominio.getSize(c.getId()));
                System.out.println("1) k-means (random init)");
                System.out.println("2) k-means++ (init)");
                System.out.println("3) k-medoids (PAM)");
                System.out.println("4) Ver ultimo resultado");
                System.out.println("5) Exportar ultimo resultado a CSV");
                System.out.println("6) Calcular accuracy respecto a CSV de etiquetas");
                System.out.println("0) Volver");
            }
            String op = io.prompt("Opcion");
            switch (op) {
                case "1" -> ejecutarKMeans(c, false);
                case "2" -> ejecutarKMeans(c, true);
                case "3" -> ejecutarKMedoids(c);
                case "4" -> verUltimoResultado();
                case "5" -> exportarUltimoResultado();
                case "6" -> calcularAccuracy();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Opcion invalida");
            }
        }

    }

    private void verUltimoResultado() {
        ClusterResult res = ctrlDominio.getUltimoResultado();
        String id = ctrlDominio.getUltimoResultadoEncuestaId();

        if (res == null || id == null) {
            System.out.println("Aun no hay resultado.");
            return;
        }

        Cuestionario cc = ctrlDominio.getEncuesta(id);
        if (cc == null) {
            System.out.println("La encuesta asociada al ultimo resultado ya no existe");
            return;
        } else {
            imprimirResumenDetallado(res, cc);
            long ultimoTiempoMs = ctrlDominio.getUltimoTiempoMs();
            double ultimoSse = ctrlDominio.getUltimoSse();
            double ultimoSilhouette = ctrlDominio.getUltimoSilhouette();

            if (!Double.isNaN(ultimoSse) || !Double.isNaN(ultimoSilhouette) || ultimoTiempoMs >= 0) {
                if(modo == ConsoleIO.Modo.MANUAL) System.out.println("Metricas del ultimo resultado:");
                if (!Double.isNaN(ultimoSse)) {
                    if(modo == ConsoleIO.Modo.MANUAL) System.out.printf(Locale.US, "  SSE: %.4f%n", ultimoSse);
                }
                if (!Double.isNaN(ultimoSilhouette)) {
                    if(modo == ConsoleIO.Modo.MANUAL) System.out.printf(Locale.US, "  Silhouette: %.4f%n", ultimoSilhouette);
                }
                if (ultimoTiempoMs >= 0) {
                    if(modo == ConsoleIO.Modo.MANUAL) System.out.printf(Locale.US, "  Tiempo: %.3f segundos (%d ms)%n",
                            ultimoTiempoMs / 1000.0, ultimoTiempoMs);
                }
            }
        }
    }

    private void ejecutarKMeans(Cuestionario c, boolean kpp) {
        int n = ctrlDominio.numRespuestas(c.getId());
        if (n == 0) {
            System.out.println("No hay respuestas. No se puede hacer clustering.");
            return;
        }
        int k = io.promptInt("k (1.." + n + ")");
        if (k <= 0 || k > n) {
            System.out.println("Valor k invalido. Debe estar entre 1 y " + n);
            return;
        }

        ClusterResult res;
        try {
            res = ctrlDominio.ejecutarKMeans(c, k, kpp);
        } catch (IllegalStateException e) {
            System.out.println("Error al vectorizar respuestas: " + e.getMessage());
            return;
        }

        if (modo == ConsoleIO.Modo.MANUAL) System.out.printf(Locale.US, "Tiempo de clustering (k-means%s): %.3f segundos (%d ms)%n", kpp ? "++" : "", ctrlDominio.getUltimoTiempoMs() / 1000.0, ctrlDominio.getUltimoTiempoMs());


        imprimirResumenDetallado(res, c);
        System.out.printf(Locale.US, "Metricas -> SSE: %.4f | Silhouette: %.4f%n", ctrlDominio.getUltimoSse(), ctrlDominio.getUltimoSilhouette());

    }

    private void ejecutarKMedoids(Cuestionario c) {
        int n = ctrlDominio.numRespuestas(c.getId());
        if (n == 0) {
            System.out.println("No hay respuestas. No se puede hacer clustering.");
            return;
        }
        int k = io.promptInt("k (1.." + n + ")");
        if (k <= 0 || k > n) {
            System.out.println("Valor k invalido. Debe estar entre 1 y " + n);
            return;
        }

        ClusterResult res;
        try {
            res = ctrlDominio.ejecutarKMedoids(c, k);
        } catch (IllegalStateException e) {
            System.out.println("Error al vectorizar respuestas: " + e.getMessage());
            return;
        }


        if (modo == ConsoleIO.Modo.MANUAL) System.out.printf(Locale.US, "Tiempo de clustering (k-medoids): %.3f segundos (%d ms)%n", ctrlDominio.getUltimoTiempoMs() / 1000.0, ctrlDominio.getUltimoTiempoMs());

        imprimirResumenDetallado(res, c);
        System.out.printf(Locale.US, "Metricas -> SSE: %.4f | Silhouette: %.4f%n", ctrlDominio.getUltimoSse(), ctrlDominio.getUltimoSilhouette());
    }

    private void exportarUltimoResultado() {
        ClusterResult res = ctrlDominio.getUltimoResultado();
        String id = ctrlDominio.getUltimoResultadoEncuestaId();

        if (res == null || id == null) {
            System.out.println("Aun no hay resultado");
            return;
        }

        String ruta = io.prompt("Ruta CSV de salida (ej. clusters.csv)");
        if (ruta.isBlank()) {
            System.out.println("Ruta vacia, no se exporta nada.");
            return;
        }

        Path out = Path.of(ruta);
        CsvClusterExporter exporter = new CsvClusterExporter();

        Cuestionario c = ctrlDominio.getEncuesta(id);
        if (c == null) {
            System.out.println("La encuesta asociada al ultimo resultado ya no existe");
            return;
        }
        List<Respuesta> listaRespuestas = ctrlDominio.listarRespuestas(id);

        try {
            exporter.exportClusterResult(
                    out,
                    res,
                    ctrlDominio.getUltimoSse(),
                    ctrlDominio.getUltimoSilhouette(),
                    ctrlDominio.getUltimoTiempoMs(),
                    c,
                    listaRespuestas
            );
            System.out.println("Resultado exportado a " + out);
        } catch (Exception e) {
            System.out.println("No se pudieron exportar los resultados: " + e.getMessage());
        }
    }

    private void calcularAccuracy() {
        ClusterResult res = ctrlDominio.getUltimoResultado();
        if (res == null) {
            System.out.println("Aun no hay resultado de clustering. Ejecuta primero k-means / k-means++ / k-medoids");
            return;
        }

        String ruta = io.prompt("Ruta del CSV de etiquetas (ej. loan-train.csv)");
        if (ruta.isBlank()) {
            System.out.println("Ruta vacia, no se calcula accuracy.");
            return;
        }

        try {
            double acc = ClusteringAccuracy.accuracyFromCsv(res, Path.of(ruta));
            if(modo == ConsoleIO.Modo.MANUAL) System.out.printf(Locale.US,
                    "Mejor accuracy respecto a %s: %.2f%%%n",
                    Path.of(ruta),
                    acc * 100.0
            );
        } catch (Exception e) {
            System.out.println("Error calculando accuracy: " + e.getMessage());
        }
    }

    private void imprimirResumenDetallado(ClusterResult res, Cuestionario c) {
        if (res == null) {
            System.out.println("Aun no hay resultado.");
            return;
        }
        if (ctrlDominio.numRespuestas(c.getId()) == 0) {
            System.out.println("No hay respuestas para mostrar resumen.");
            return;
        }

        System.out.println("Algoritmo: " + res.algorithm());
        if (modo == ConsoleIO.Modo.MANUAL) {
            System.out.println("Asignaciones:");
            res.assignments().forEach((pid, cid) -> System.out.println("  " + pid + " -> " + cid));
        }

        Distance dist = new GowerDistance();
        List<Vectorizer.Point> datos;
        try {
            datos = ctrlDominio.vectorizar(c);
        } catch (IllegalStateException e) {
            System.out.println("Error al vectorizar para el resumen: " + e.getMessage());
            return;
        }

        Map<String, Vectorizer.Point> byId = new HashMap<>();
        for (var p : datos) byId.put(p.id(), p);

        Map<Integer, List<Vectorizer.Point>> grupos = new HashMap<>();
        for (var e : res.assignments().entrySet()) {
            grupos.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(byId.get(e.getKey()));
        }

        System.out.println("Representantes por cluster:");
        for (int cid = 0; cid < res.representatives().size(); cid++) {
            var rep = res.representatives().get(cid);
            var miembros = grupos.getOrDefault(cid, List.of());

            if (rep.id().startsWith("centro_")) {
                double best = Double.POSITIVE_INFINITY;
                String bestId = "(ninguno)";
                for (var p : miembros) {
                    double d = dist.dist(p, rep);
                    if (d < best) {
                        best = d;
                        bestId = p.id();
                    }
                }
                double representanteDist = (miembros.isEmpty() ? 0.0 : best);
                System.out.printf("  Cluster %d -> punto mas cercano al centroide: %s (dist=%.4f)%n",
                        cid, bestId, representanteDist);
            } else {
                System.out.printf("  Cluster %d -> medoid (dato real): %s%n", cid, rep.id());
            }
        }
    }
}
