package drivers;

import domain.controllers.CtrlDominio;
import domain.classes.model.Cuestionario;
import domain.classes.model.Respuesta;
import domain.classes.impexp.CsvSurveyExporter;
import domain.classes.impexp.CsvSurveyImporter;
import domain.classes.util.ConsoleIO;

import java.nio.file.Path;
import java.util.List;

public class ExportarImportarMenu {

    private final ConsoleIO io;
    private final CtrlDominio ctrlDominio;
    private final ConsoleIO.Modo modo;

    public ExportarImportarMenu(ConsoleIO io, CtrlDominio ctrlDominio, ConsoleIO.Modo modo) {
        this.io = io;
        this.ctrlDominio = ctrlDominio;
        this.modo = modo;
    }

    public void ejecutar() {
        CsvSurveyImporter imp = new CsvSurveyImporter();
        CsvSurveyExporter exp = new CsvSurveyExporter();

        while (true) {
            if(modo == ConsoleIO.Modo.MANUAL) {
                System.out.println("\nImportar / Exportar");
                System.out.println("1) Importar encuesta (preguntas y/o respuestas) CSV");
                System.out.println("2) Exportar encuesta (preguntas y respuestas) CSV");
                System.out.println("0) Volver");
            }
            String opcion = io.prompt("Opcion");
            try {
                switch (opcion) {
                    case "1" -> importarEncuesta(imp);
                    case "2" -> exportarEncuesta(exp);
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Opcion invalida");
                }
            }
            catch (Exception e) {
                System.out.println("Error en importar/exportar: " + e.getMessage());
            }
        }
    }

    private void importarEncuesta(CsvSurveyImporter imp) throws Exception {
        String ruta = io.prompt("Ruta CSV a importar:");
        if (ruta.isBlank()) {
            System.out.println("Ruta vacia");
            return;
        }
        Path p = Path.of(ruta);
        String id = io.prompt("Id de encuesta para guardar:");
        if (id.isBlank()) {
            System.out.println("Id de encuesta vacio");
            return;
        }

        CsvSurveyImporter.ImportResult res = imp.importSurvey(p, id);

        ctrlDominio.guardar(res.cuestionario);

        ctrlDominio.eliminarTodas(id);
        for (var r : res.respuestas) {
            ctrlDominio.agregar(id, r);
        }

        System.out.println("Importada encuesta " + id + " con " + res.cuestionario.getPreguntas().size() + " preguntas y " + res.respuestas.size() + " respuestas");
    }

    private void exportarEncuesta(CsvSurveyExporter exp) throws Exception {
        Cuestionario en = FuncionesEncuesta.seleccionarEncuesta(io, ctrlDominio);
        if (en == null) return;
        String id = en.getId();

        String ruta = io.prompt("Ruta CSV de salida");
        if (ruta.isBlank()) {
            System.out.println("Ruta vacia, no se exporta nada");
            return;
        }
        Path out = Path.of(ruta);
        List<Respuesta> listaRespuestas = ctrlDominio.listarRespuestas(id);
        exp.exportSurvey(out, en, listaRespuestas); //Exportar

        // Usar println para asegurar salto de línea y flushing en consola
        System.out.println("Exportada encuesta " + id + " con " + listaRespuestas.size() + " respuestas a " + out);
    }
}
