package domain.classes.vector;

import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.Respuesta;
import domain.classes.model.TipoPregunta;
import domain.classes.model.ValorRespuesta;
import domain.classes.model.ValorRespuestaNumerica;
import domain.classes.model.ValorRespuestaTextual;
import domain.classes.model.ValorRespuestaOpcionUnica;
import domain.classes.model.ValorRespuestaOpcionMultiple;

import java.util.*;

public class Vectorizer {

    // Punto = vectorizado de un participante
    public static class Point {
        private final String id;
        private final double[] values;
        private final boolean[] missing;
        public Point(String id, double[] values, boolean[] missing){
            this.id = id;
            this.values = values;
            this.missing = missing;
        }
        public String id(){ return id; }
        public double[] values(){ return values; }
        public boolean[] missing(){ return missing; }
    }

    private final VectorSchema schema = new VectorSchema();

    /**
     Ajusta el esquema de vectorizacion a partir de las respuestas:
     - Preguntas NUMERICAS: calcula el rango [min, max] observado para normalizar luego a [0,1].
     - Preguntas de TEXTO: detecta las categorias observadas y configura una codificacion one-hot.
     - OPCION_UNICA / OPCION_MULTIPLE: configura tambien sus categorias en el esquema (one-hot),
     Se hace un esquema que se utilizará despues en transform(...) para convertir cada Respuesta en un vector numerico.
     */
    public void fit(Cuestionario c, List<Respuesta> rs){
        List<Pregunta> preguntas = c.getPreguntas();
        if (preguntas.isEmpty()) {
            schema.setDimensions(0);
            return;
        }

        // empezamos en 1 para saltar la PRIMERA PREGUNTA (IDs)
        for (int idx = 1; idx < preguntas.size(); idx++) {
            Pregunta p = preguntas.get(idx);
            String qid = p.getId();
            if (p.getTipo() == TipoPregunta.NUMERICA) {
                List <Double> valoresNumericos = new ArrayList<>();

                for (Respuesta r : rs){
                    ValorRespuesta v = r.getValores().get(p.getId());
                    if (v instanceof ValorRespuestaNumerica n){
                        valoresNumericos.add(n.getValor());
                    }
                }


                double minValid = Double.POSITIVE_INFINITY;
                double maxValid = Double.NEGATIVE_INFINITY;

                if (!valoresNumericos.isEmpty()) {
                    Collections.sort(valoresNumericos);
                    int n = valoresNumericos.size();
                    int idxQ1 = (int) (valoresNumericos.size() * 0.25);
                    int idxQ3 = (int) (valoresNumericos.size() * 0.75);

                    double q1 = valoresNumericos.get(idxQ1);
                    double q3 = valoresNumericos.get(idxQ3);

                    double iqr = q3 - q1;
                    minValid = q1 - 1.5 * iqr;
                    maxValid = q3 + 1.5 * iqr;

                    schema.setOutlierLimits(qid, minValid, maxValid);
                }

                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                double suma = 0;
                int count = 0;

                for (Double v : valoresNumericos){
                    if (v >= minValid && v <= maxValid){
                        if (v < min) min = v;
                        if (v > max) max = v;
                        suma += v;
                        count++;
                    }
                }

                if (min == Double.POSITIVE_INFINITY){
                    min = 0;
                    max = 1;
                }
                schema.setNumericRange(p.getId(), min, max);

                if (count > 0) {
                    double media = suma / count;
                    schema.setDefaultValue(p.getId(), new ValorRespuestaNumerica(media));
                }

            } else if (p.getTipo() == TipoPregunta.TEXTO
                    || p.getTipo() == TipoPregunta.OPCION_UNICA
                    || p.getTipo() == TipoPregunta.OPCION_MULTIPLE) {
                List<String> opts = (p.getOpciones() == null || p.getOpciones().isEmpty())
                        ? observedOptions(p.getId(), rs)
                        : p.getOpciones();
                schema.setOneHot(p.getId(), opts);
                if (p.getTipo() == TipoPregunta.OPCION_UNICA) {
                    Map <Integer,Integer> conteo = new HashMap<>();
                    for (Respuesta r : rs) {
                        ValorRespuesta v = r.getValores().get(p.getId());
                        if (v instanceof ValorRespuestaOpcionUnica vu) {
                            Integer indice = vu.getIndiceSeleccionado();
                            conteo.put(indice, conteo.getOrDefault(indice, 0) + 1);
                        }
                    }
                    int mejorIndice = -1;
                    int maxConteo = -1;
                    for (Map.Entry<Integer, Integer> entry : conteo.entrySet()) {
                        if (entry.getValue() > maxConteo) {
                            maxConteo = entry.getValue();
                            mejorIndice = entry.getKey();
                        }
                    }
                    if (mejorIndice != -1) {
                        schema.setDefaultValue(p.getId(), new ValorRespuestaOpcionUnica(mejorIndice));
                    }
                }
            }
        }

        // Calcular numero total de dimensiones (tambien saltando la primera pregunta)
        int dims = 0;
        for (int idx = 1; idx < preguntas.size(); idx++) {
            Pregunta p = preguntas.get(idx);
            switch (p.getTipo()){
                case NUMERICA -> dims += 1;
                case TEXTO, OPCION_UNICA, OPCION_MULTIPLE -> {
                    List<String> opts = schema.getOptions(p.getId());
                    dims += Math.max(1, opts.size());
                }
            }
        }
        schema.setDimensions(dims);
    }

    public Point transform(Respuesta r, Cuestionario c){
        double[] v = new double[schema.getDimensions()];
        boolean[] m = new boolean[schema.getDimensions()];
        int off = 0;

        List<Pregunta> preguntas = c.getPreguntas();
        if (preguntas.isEmpty()) {
            return new Point(r.getIdParticipante(), v, m);
        }

        // empezamos en 1 para saltar la PRIMERA PREGUNTA (IDs)
        for (int idx = 1; idx < preguntas.size(); idx++) {
            Pregunta p = preguntas.get(idx);
            ValorRespuesta val = r.getValores().get(p.getId());

            if (p.getTipo() == TipoPregunta.NUMERICA && val instanceof ValorRespuestaNumerica n) {
                double[] limits = schema.getOutlierLimits(p.getId());
                if (limits != null) {
                    double x = n.getValor();
                    if (x < limits[0] || x > limits[1]) {
                        val = null;
                    }
                }
            }

            if (val == null){
                val = schema.getDefaultValue(p.getId());
            }

            switch (p.getTipo()){
                case NUMERICA -> {
                    if (val instanceof ValorRespuestaNumerica n){
                        double[] rg = schema.getRange(p.getId());
                        double norm = (rg[1]-rg[0]) == 0
                                ? 0.5
                                : ((n.getValor() - rg[0]) / (rg[1]-rg[0]));
                        v[off] = clamp(norm);
                        m[off] = false;
                    } else {
                        v[off] = 0;
                        m[off] = true;
                    }
                    off += 1;
                }
                case TEXTO -> {
                    List<String> opts = schema.getOptions(p.getId());
                    String sVal = (val instanceof ValorRespuestaTextual vt) ? vt.getValor() : null;
                    if (opts.isEmpty()){
                        v[off] = 0.0;
                        m[off] = (sVal == null);
                        off += 1;
                    } else {
                        for (int i = 0; i < opts.size(); i++){
                            if (sVal != null && sVal.equals(opts.get(i))){
                                v[off + i] = 1.0;
                                m[off + i] = false;
                            } else {
                                v[off + i] = 0.0;
                                m[off + i] = (sVal == null);
                            }
                        }
                        off += opts.size();
                    }
                }
                case OPCION_UNICA -> {
                    var opts = schema.getOptions(p.getId());
                    Integer indice = (val instanceof ValorRespuestaOpcionUnica vu) ? vu.getIndiceSeleccionado() : null;
                    for (int i = 0; i < opts.size(); i++){
                        if (indice != null && indice == i) { // Comparar con el índice
                            v[off + i] = 1.0;
                            m[off + i] = false;
                        } else {
                            v[off + i] = 0.0;
                            m[off + i] = (indice == null);
                        }
                    }
                    off += opts.size();
                }
                case OPCION_MULTIPLE -> {
                    var opts = schema.getOptions(p.getId());
                    Set<Integer> indicesSeleccionados = new HashSet<>();
                    if (val instanceof ValorRespuestaOpcionMultiple vm){
                        indicesSeleccionados.addAll(vm.getIndicesSeleccionados());
                    }
                    int k = Math.max(1, indicesSeleccionados.size());
                    for (int i = 0; i < opts.size(); i++){
                        boolean on = indicesSeleccionados.contains(i);
                        v[off + i] = on ? (1.0 / k) : 0.0;
                        m[off + i] = indicesSeleccionados.isEmpty(); // Es missing si no se seleccionó nada
                    }
                    off += opts.size();
                }
            }
        }

        return new Point(r.getIdParticipante(), v, m);
    }

    public List<Point> transformAll(List<Respuesta> rs, Cuestionario c){
        List<Point> out = new ArrayList<>();
        for (Respuesta r : rs) out.add(transform(r, c));
        return out;
    }

    private static List<String> observedOptions(String id, List<Respuesta> rs){
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Respuesta r : rs){
            ValorRespuesta v = r.getValores().get(id);
            if (v instanceof ValorRespuestaTextual s) {
                set.add(s.getValor()); // Usar getter
            }
        }
        return new ArrayList<>(set);
    }

    private static double clamp(double x){
        return Math.max(0, Math.min(1, x));
    }
}