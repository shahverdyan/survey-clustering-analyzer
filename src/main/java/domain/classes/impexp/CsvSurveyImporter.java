package domain.classes.impexp;

import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.Respuesta;
import domain.classes.model.TipoPregunta;
import domain.classes.model.ValorRespuestaNumerica;
import domain.classes.model.ValorRespuestaTextual;
import domain.classes.model.ValorRespuestaOpcionUnica;
import domain.classes.model.ValorRespuestaOpcionMultiple;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvSurveyImporter {

    public static class ImportResult {
        public final Cuestionario cuestionario;
        public final List<Respuesta> respuestas;
        public ImportResult(Cuestionario c, List<Respuesta> r){
            this.cuestionario = c;
            this.respuestas = r;
        }
    }

    private static TipoPregunta parseTipo(String raw) {
        if (raw == null) return TipoPregunta.TEXTO;
        String t = raw.trim().toUpperCase();
        return switch (t) {
            case "NUMERICA" -> TipoPregunta.NUMERICA;
            case "TEXTO", "TEXTUAL" -> TipoPregunta.TEXTO;
            case "OPCION_UNICA", "OPCIÓN_UNICA" -> TipoPregunta.OPCION_UNICA;
            case "OPCION_MULTIPLE", "OPCIÓN_MULTIPLE" -> TipoPregunta.OPCION_MULTIPLE;
            default -> TipoPregunta.TEXTO;
        };
    }

    public ImportResult importSurvey(Path path, String surveyId) throws IOException {
        List<Respuesta> respuestas = new ArrayList<>();
        Cuestionario cuestionario = new Cuestionario(surveyId);

        try (BufferedReader br = Files.newBufferedReader(path)) {
            // 1. Header
            String header = br.readLine();
            if (header == null) throw new IOException("CSV vacío: " + path);
            String[] cols = header.split(",", -1);

            // 2. Tipos
            String typeLine = br.readLine();
            if (typeLine == null) throw new IOException("CSV sin fila de tipos: " + path);
            String[] types = typeLine.split(",", -1);

            // 3. Opciones + Max Seleccion
            String optionsLine = br.readLine();
            String[] optionsRaw = (optionsLine != null) ? optionsLine.split(",", -1) : null;

            int numCols = cols.length;
            String[] preguntaIds = new String[numCols];
            TipoPregunta[] tipos = new TipoPregunta[numCols];

            for (int j = 0; j < numCols; j++) {
                String enunciado = cols[j].trim();
                if (enunciado.isEmpty()) enunciado = "Pregunta_" + (j + 1);
                preguntaIds[j] = enunciado;

                tipos[j] = parseTipo(types[j]);
                TipoPregunta tipo = tipos[j];

                List<String> opciones = new ArrayList<>();
                int maxSel = 0; // Por defecto 0 (sin límite)

                if (optionsRaw != null && j < optionsRaw.length) {
                    String rawOps = optionsRaw[j].trim();

                    // --- NUEVO: DETECTAR MAX SELECCION ---
                    // Buscamos si el string termina con <<MAX:n>>
                    if (rawOps.contains("<<MAX:")) {
                        try {
                            int start = rawOps.indexOf("<<MAX:");
                            int end = rawOps.indexOf(">>", start);
                            if (end > start) {
                                String numStr = rawOps.substring(start + 6, end);
                                maxSel = Integer.parseInt(numStr);
                                // Quitamos la etiqueta para dejar solo las opciones
                                rawOps = rawOps.substring(0, start);
                            }
                        } catch (Exception e) {
                            // Si falla el parseo, ignoramos el max
                        }
                    }
                    // -------------------------------------

                    if (!rawOps.isEmpty()) {
                        String[] parts = rawOps.split("\\|");
                        opciones = new ArrayList<>(Arrays.asList(parts));
                    }
                }

                cuestionario.addPregunta(new Pregunta(
                        preguntaIds[j], preguntaIds[j], tipo, opciones, maxSel
                ));
            }

            // 4. Datos (Respuestas)
            String line;
            int respIndex = 1;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] row = line.split(",", -1);

                String participanteId = "imp_" + respIndex++;
                Respuesta r = new Respuesta(participanteId);

                for (int j = 0; j < numCols; j++) {
                    if (j >= row.length) continue;
                    String raw = row[j].trim();
                    if (raw.isEmpty()) continue;

                    String pid = preguntaIds[j];
                    Pregunta p = cuestionario.getPregunta(pid);
                    List<String> opciones = p.getOpciones();

                    switch (tipos[j]) {
                        case NUMERICA -> {
                            try {
                                r.put(pid, new ValorRespuestaNumerica(Double.parseDouble(raw)));
                            } catch (NumberFormatException ignored) { }
                        }
                        case TEXTO -> r.put(pid, new ValorRespuestaTextual(raw));
                        case OPCION_UNICA -> {
                            int idx = opciones.indexOf(raw);
                            if (idx != -1) r.put(pid, new ValorRespuestaOpcionUnica(idx));
                        }
                        case OPCION_MULTIPLE -> {
                            String[] parts = raw.split("\\|");
                            List<Integer> indicesSel = new ArrayList<>();
                            for (String part : parts) {
                                int idx = opciones.indexOf(part.trim());
                                if (idx != -1) indicesSel.add(idx);
                            }
                            if (!indicesSel.isEmpty()) {
                                r.put(pid, new ValorRespuestaOpcionMultiple(indicesSel));
                            }
                        }
                    }
                }
                respuestas.add(r);
            }
        }
        return new ImportResult(cuestionario, respuestas);
    }
}