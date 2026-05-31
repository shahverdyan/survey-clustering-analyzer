package domain.classes.impexp;

import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.Respuesta;
import domain.classes.model.TipoPregunta;
import domain.classes.model.ValorRespuesta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvSurveyExporter {

    private String tipoToString(TipoPregunta tp) {
        return switch (tp) {
            case TEXTO -> "TEXTUAL";
            case NUMERICA -> "NUMERICA";
            case OPCION_UNICA -> "OPCION_UNICA";
            case OPCION_MULTIPLE -> "OPCION_MULTIPLE";
        };
    }

    public void exportSurvey(Path path, Cuestionario c, List<Respuesta> respuestas) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            List<Pregunta> preguntas = c.getPreguntas();

            // Fila 0: IDs
            for (int j = 0; j < preguntas.size(); j++) {
                bw.write(preguntas.get(j).getId());
                if (j + 1 < preguntas.size()) bw.write(",");
            }
            bw.newLine();

            // Fila 1: Tipos
            for (int j = 0; j < preguntas.size(); j++) {
                bw.write(tipoToString(preguntas.get(j).getTipo()));
                if (j + 1 < preguntas.size()) bw.write(",");
            }
            bw.newLine();

            // Fila 2: OPCIONES + MAX SELECCION
            for (int j = 0; j < preguntas.size(); j++) {
                Pregunta p = preguntas.get(j);
                List<String> opts = p.getOpciones();
                if (opts != null && !opts.isEmpty()) {
                    String joined = String.join("|", opts);

                    // --- NUEVO: Si tiene límite de selección, lo pegamos al final ---
                    if (p.getTipo() == TipoPregunta.OPCION_MULTIPLE && p.getMaxSeleccion() > 0) {
                        joined += "<<MAX:" + p.getMaxSeleccion() + ">>";
                    }
                    // ----------------------------------------------------------------

                    bw.write(joined);
                } else {
                    bw.write("");
                }
                if (j + 1 < preguntas.size()) bw.write(",");
            }
            bw.newLine();

            // Resto: Respuestas
            for (Respuesta r : respuestas) {
                for (int j = 0; j < preguntas.size(); j++) {
                    Pregunta p = preguntas.get(j);
                    ValorRespuesta valWrapper = r.getValores().get(p.getId());
                    String out = "";
                    if (valWrapper != null) {
                        out = valWrapper.getValorNormalizado();
                    }
                    bw.write(out);
                    if (j + 1 < preguntas.size()) bw.write(",");
                }
                bw.newLine();
            }
        }
    }
}