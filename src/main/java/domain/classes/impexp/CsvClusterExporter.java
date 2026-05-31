package domain.classes.impexp;

import domain.classes.clustering.ClusterResult;
import domain.classes.distance.GowerDistance;
import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.Respuesta;
import domain.classes.vector.Vectorizer;
import domain.classes.model.TipoPregunta;
import domain.classes.model.ValorRespuesta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvClusterExporter {

    /**
     Exporta el resultado de clustering.

     Formato:
     # algorithm=...
     # SSE=...
     # Silhouette=...
     # time_ms=...
     # representatives:
     # cluster 0 -> centro_0 (nearest=resp_3, dist=0.123456)
     # cluster 1 -> resp_5 (medoid)
     P1,P2,P3,...,clusterId
     v1,v2,"a|b|c",...,0
     ...
     */
    public void exportClusterResult(Path path,
                                    ClusterResult result,
                                    Double sse,
                                    Double silhouette,
                                    Long elapsedMillis,
                                    Cuestionario cuestionario,
                                    List<Respuesta> respuestas) throws IOException {

        Map<String, Respuesta> respById = new HashMap<>();
        for (Respuesta r : respuestas) {
            respById.put(r.getIdParticipante(), r);
        }

        List<Pregunta> preguntas = cuestionario.getPreguntas();

        // Preparamos los puntos vectorizados para poder medir distancias
        List<Vectorizer.Point> puntos = new ArrayList<>();
        Map<String, Vectorizer.Point> puntoPorId = new HashMap<>();
        if (!respuestas.isEmpty()) {
            Vectorizer v = new Vectorizer();
            v.fit(cuestionario, respuestas);
            puntos = v.transformAll(respuestas, cuestionario);
            for (Vectorizer.Point p : puntos) {
                puntoPorId.put(p.id(), p);
            }
        }
        GowerDistance dist = new GowerDistance();

        try (BufferedWriter bw = Files.newBufferedWriter(path)) {

            // Metadatos
            bw.write("# algorithm=" + result.algorithm());
            bw.newLine();
            if (sse != null && !Double.isNaN(sse)) {
                bw.write(String.format(Locale.US, "# SSE=%.6f", sse));
                bw.newLine();
            }
            if (silhouette != null && !Double.isNaN(silhouette)) {
                bw.write(String.format(Locale.US, "# Silhouette=%.6f", silhouette));
                bw.newLine();
            }
            if (elapsedMillis != null && elapsedMillis >= 0) {
                bw.write("# time_ms=" + elapsedMillis);
                bw.newLine();
            }

            // Información de representantes
            bw.write("# representatives:");
            bw.newLine();

            // Agrupar puntos por cluster para buscar el más cercano al centroide
            Map<Integer, List<Vectorizer.Point>> puntosPorCluster = new HashMap<>();
            for (Map.Entry<String,Integer> e : result.assignments().entrySet()) {
                String pid = e.getKey();
                Integer cid = e.getValue();
                Vectorizer.Point p = puntoPorId.get(pid);
                if (p == null) continue;
                puntosPorCluster.computeIfAbsent(cid, k -> new ArrayList<>()).add(p);
            }

            for (int cid = 0; cid < result.representatives().size(); cid++) {
                Vectorizer.Point rep = result.representatives().get(cid);

                // Caso K-Means / K-Means++: centroide sintético
                if (rep.id().startsWith("centro_")) {
                    List<Vectorizer.Point> miembros = puntosPorCluster.getOrDefault(cid, List.of());
                    String nearestId = "(ninguno)";
                    double best = Double.POSITIVE_INFINITY;

                    for (Vectorizer.Point p : miembros) {
                        double d = dist.dist(p, rep);
                        if (d < best) {
                            best = d;
                            nearestId = p.id();
                        }
                    }

                    if (Double.isInfinite(best)) best = 0.0; // por si el cluster está vacío

                    bw.write(String.format(Locale.US,
                            "# cluster %d -> %s (nearest=%s, dist=%.6f)",
                            cid, rep.id(), nearestId, best));
                    bw.newLine();
                }
                // Caso K-Medoids: el representante ya es un dato real
                else {
                    bw.write("# cluster " + cid + " -> " + rep.id() + " (medoid)");
                    bw.newLine();
                }
            }

            // Cabecera de datos: una columna por pregunta + clusterId
            for (int i = 0; i < preguntas.size(); i++) {
                bw.write(preguntas.get(i).getId());
                if (i + 1 < preguntas.size()) bw.write(",");
            }
            bw.write(",clusterId");
            bw.newLine();

            // Filas de datos
            for (Map.Entry<String, Integer> e : result.assignments().entrySet()) {
                String pid = e.getKey();
                Integer cid = e.getValue();

                Respuesta r = respById.get(pid);

                StringBuilder row = new StringBuilder();
                if (r != null) {
                    for (int i = 0; i < preguntas.size(); i++) {
                        Pregunta p = preguntas.get(i);
                        ValorRespuesta valWrapper = r.getValores().get(p.getId());
                        String out = "";

                        if (valWrapper != null) {
                            out = valWrapper.getValorNormalizado();

                            if (p.getTipo() == TipoPregunta.OPCION_MULTIPLE) {
                                out = out.replace(",", "|");
                            }
                        }

                        row.append(out);
                        if (i + 1 < preguntas.size()) row.append(",");
                    }
                } else {
                    // Si no encontramos la respuesta, dejamos columnas vacías
                    for (int i = 0; i < preguntas.size(); i++) {
                        if (i > 0) row.append(",");
                    }
                }

                // Añadir el clusterId al final
                row.append(",");
                row.append(cid);

                bw.write(row.toString());
                bw.newLine();
            }
        }
    }
}
