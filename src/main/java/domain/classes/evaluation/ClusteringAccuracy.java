package domain.classes.evaluation;

import domain.classes.clustering.ClusterResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ClusteringAccuracy {

    /**
       Calcula la accuracy comparando:
        - El cluster asignado a cada respuesta y la etiqueta real de la ÚLTIMA columna del CSV.

       El CSV puede ser de dos tipos:
        - Exportado por nuestro programa: fila 0 = cabecera, fila 1 = tipos (NUMERICA, TEXTUAL...),
          filas 2..N = datos.
        - Externo fila 0 = cabecera, filas 1..N = datos.

       Para ello agregaremos un sistema para detectar si la fila 1 es de tipos o de datos.
     */
    public static double accuracyFromCsv(ClusterResult res, Path csvPath) throws IOException {
        // 1) Cargar etiquetas verdaderas en orden
        List<String> trueLabels = loadLabelsInOrder(csvPath);
        if (trueLabels.isEmpty()) {
            throw new IllegalStateException("No se han encontrado etiquetas en el CSV: " + csvPath);
        }

        // 2) entries = (participanteId, clusterId) en el mismo orden de vectorización
        List<Map.Entry<String,Integer>> entries = new ArrayList<>(res.assignments().entrySet());
        if (entries.isEmpty()) {
            throw new IllegalStateException("El resultado de clustering no contiene asignaciones.");
        }

        // 3) Emparejar por orden hasta el mínimo común
        int n = Math.min(entries.size(), trueLabels.size());
        if (n == 0) {
            throw new IllegalStateException(
                    "No hay interseccion entre respuestas vectorizadas y filas de datos del CSV (n=0).");
        }

        // Conjuntos de clusters y clases realmente presentes en esas n filas
        Set<Integer> clusterSet = new LinkedHashSet<>();
        Set<String> labelSet = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) {
            clusterSet.add(entries.get(i).getValue());
            labelSet.add(trueLabels.get(i));
        }

        int numClusters = clusterSet.size();
        int numLabelClasses = labelSet.size();

        if (numClusters != numLabelClasses) {
            throw new IllegalStateException(
                    "Numero de clusters distintos en el resultado (" + numClusters +
                            ") distinto al numero de clases distintas en la ultima columna del CSV (" +
                            numLabelClasses + "). " +
                            "Elige k igual al numero de clases o revisa el CSV."
            );
        }
        int K = numClusters;

        // clusterId -> índice 0..K-1
        Integer[] clusterIds = clusterSet.toArray(new Integer[0]);
        Map<Integer,Integer> clusterIdToIdx = new HashMap<>();
        for (int i = 0; i < K; i++) clusterIdToIdx.put(clusterIds[i], i);

        // labelName -> índice 0..K-1
        String[] labelNames = labelSet.toArray(new String[0]);
        Map<String,Integer> labelToIdx = new HashMap<>();
        for (int i = 0; i < K; i++) labelToIdx.put(labelNames[i], i);

        // Permutaciones de 0..K-1
        int[] perm = new int[K];
        for (int i = 0; i < K; i++) perm[i] = i;

        double bestAcc = 0.0;
        int permNum = 1;

        do {
            int total = 0;
            int correct = 0;

            for (int i = 0; i < n; i++) {
                Map.Entry<String,Integer> e = entries.get(i);
                Integer cid = e.getValue();
                String trueLabel = trueLabels.get(i);

                Integer ci = clusterIdToIdx.get(cid);
                Integer li = labelToIdx.get(trueLabel);
                if (ci == null || li == null) continue;

                int predictedLabelIdx = perm[ci];
                if (predictedLabelIdx == li) correct++;
                total++;
            }

            double acc = total == 0 ? 0.0 : (correct / (double) total);

            // Print de la permutación
            StringBuilder sb = new StringBuilder();
            sb.append("Permutacion ").append(permNum).append(" -> ");
            for (int i = 0; i < K; i++) {
                int cid = clusterIds[i];
                int lblIdx = perm[i];
                String lblName = (lblIdx >= 0 && lblIdx < labelNames.length)
                        ? labelNames[lblIdx]
                        : ("idx=" + lblIdx);
                sb.append("cluster ").append(cid).append(" -> ").append(lblName);
                if (i + 1 < K) sb.append(", ");
            }
            sb.append(String.format(Locale.US, " | accuracy = %.4f", acc));
            System.out.println(sb);

            if (acc > bestAcc) bestAcc = acc;
            permNum++;
        } while (nextPermutation(perm));

        return bestAcc;
    }

    private static List<String> loadLabelsInOrder(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        // Nuevo formato fijo esperado:
        //  - línea 0: ids de preguntas
        //  - línea 1: tipos
        //  - línea 2: opciones
        //  - líneas 3..N: datos (última columna = etiqueta)
        if (lines.size() < 4) {
            throw new IOException("CSV demasiado corto: " + path);
        }

        int startRow = 3; // datos a partir de la cuarta línea

        List<String> labels = new ArrayList<>();
        for (int i = startRow; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) continue;
            // normalizar posibles CR de Windows
            line = line.replace("\r", "");
            String[] cols = line.split(",", -1);
            if (cols.length == 0) continue;
            String label = cols[cols.length - 1].trim(); // ULTIMA columna
            if (label.isEmpty()) continue;
            labels.add(label);
        }
        return labels;
    }

    // Siguiente permutacion lexicografica sobre perm[]
    private static boolean nextPermutation(int[] a) {
        int n = a.length;
        int i = n - 2;
        while (i >= 0 && a[i] >= a[i + 1]) i--;
        if (i < 0) return false;
        int j = n - 1;
        while (a[j] <= a[i]) j--;
        swap(a, i, j);
        reverse(a, i + 1, n - 1);
        return true;
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
    }

    private static void reverse(int[] a, int i, int j) {
        while (i < j) swap(a, i++, j--);
    }
}
