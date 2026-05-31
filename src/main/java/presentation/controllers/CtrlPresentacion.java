package presentation.controllers;

import domain.controllers.CtrlDominio;
import presentation.classes.VistaPrincipal;

// Imports de Modelo y Utilidades
import domain.classes.clustering.ClusterResult;
import domain.classes.evaluation.ClusteringAccuracy;
import domain.classes.impexp.CsvClusterExporter;
import domain.classes.impexp.CsvSurveyImporter;
import domain.classes.impexp.CsvSurveyExporter;
import domain.classes.model.Cuestionario;
import domain.classes.model.Respuesta;
import domain.classes.vector.Vectorizer;

import java.nio.file.Path;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class CtrlPresentacion {

    private final CtrlDominio ctrlDominio;
    private final VistaPrincipal vistaPrincipal;

    private String currentSurveyId;

    public CtrlPresentacion(CtrlDominio ctrlDominio) {
        this.ctrlDominio = ctrlDominio;
        this.vistaPrincipal = new VistaPrincipal(this);
    }

    public void inicializarPresentacion() {
        // Al arrancar, hacemos visible la ventana (que mostrará el Login por defecto)
        vistaPrincipal.hacerVisible();
    }

    // GESTIÓN DE SESIÓN Y USUARIOS (NUEVO)
    public boolean login(String email, String password) {
        // Delegamos al dominio la comprobación
        boolean exito = ctrlDominio.login(email, password);

        if (exito) {
            // Si es correcto, decimos a la vista que cambie al Menú Principal
            vistaPrincipal.entrarAlSistema();
        }
        return exito;
    }

    public void registrarUsuario(String email, String pass, String nombre, String ape, String nac) {
        // Delegamos el registro
        ctrlDominio.registrarUsuario(email, pass, nombre, ape, nac);
    }

    public void logout() {
        // Cerramos sesión en dominio y volvemos a la pantalla de Login
        ctrlDominio.logout();
        vistaPrincipal.salirAlLogin();
    }

    public String getUsuarioNombre() {
        return ctrlDominio.getUsuarioLogueadoNombre();
    }

    // NAVEGACIÓN
    public void cambiarVista(String nombreVista) {
        vistaPrincipal.mostrarPanel(nombreVista);
    }

    public CtrlDominio getCtrlDominio() {
        return ctrlDominio;
    }

    // GESTIÓN DE DATOS (ENCUESTAS)
    //Devuelve SOLO las encuestas del usuario logueado.
    public List<String> obtenerIdsMisEncuestas() {
        return ctrlDominio.listarMisEncuestas().stream()
                .map(Cuestionario::getId)
                .collect(Collectors.toList());
    }

    //Devuelve TODAS las encuestas del sistema.
    public List<String> obtenerIdsTodasEncuestas() {
        return ctrlDominio.listar().stream()
                .map(Cuestionario::getId)
                .collect(Collectors.toList());
    }


    public List<String> obtenerIdsEncuestas() {
        return obtenerIdsMisEncuestas();
    }

    //IMPORTAR / EXPORTAR
    public void importarEncuesta(String rutaArchivo) {
        try {
            vistaPrincipal.log("Iniciando importación desde: " + rutaArchivo);
            CsvSurveyImporter importer = new CsvSurveyImporter();
            File f = new File(rutaArchivo);
            String nombreArchivo = f.getName(); // Usar nombre archivo como ID provisional si es necesario

            // Importamos
            CsvSurveyImporter.ImportResult resultado = importer.importSurvey(Path.of(rutaArchivo), nombreArchivo);

            // Guardamos en dominio (CtrlDominio asignará el Owner automáticamente al usuario actual)
            ctrlDominio.guardar(resultado.cuestionario);

            int count = 0;
            for (Respuesta r : resultado.respuestas) {
                ctrlDominio.agregar(resultado.cuestionario.getId(), r);
                count++;
            }

            this.currentSurveyId = resultado.cuestionario.getId();
            vistaPrincipal.log("¡Éxito! Encuesta cargada ID: " + currentSurveyId);
            vistaPrincipal.mostrarInfo("Importación Exitosa", "Se han cargado " + count + " respuestas.\nID asignado: " + currentSurveyId);

        } catch (Exception e) {
            e.printStackTrace();
            vistaPrincipal.log("ERROR Importar: " + e.getMessage());
            vistaPrincipal.mostrarError("Error de Importación", e.getMessage());
        }
    }

    public void exportarDatos(String idEncuesta, String rutaDestino) {
        if (idEncuesta == null || !ctrlDominio.existe(idEncuesta)) {
            vistaPrincipal.mostrarError("Error", "La encuesta seleccionada no existe.");
            return;
        }
        try {
            vistaPrincipal.log("Exportando encuesta '" + idEncuesta + "' a: " + rutaDestino);
            Cuestionario c = ctrlDominio.obtener(idEncuesta);
            List<Respuesta> respuestas = ctrlDominio.listarRespuestas(idEncuesta);

            CsvSurveyExporter exporter = new CsvSurveyExporter();
            exporter.exportSurvey(Path.of(rutaDestino), c, respuestas);

            vistaPrincipal.mostrarInfo("Exportación", "Datos de '" + idEncuesta + "' guardados en:\n" + rutaDestino);
        } catch (Exception e) {
            e.printStackTrace();
            vistaPrincipal.mostrarError("Error de Exportación", e.getMessage());
        }
    }

    // LÓGICA DE CLUSTERING
    public void ejecutarClustering(String idEncuesta, String algoritmo, int k) throws Exception {
        Cuestionario c = ctrlDominio.obtener(idEncuesta);
        if (c == null) throw new Exception("Encuesta no encontrada");
        if (ctrlDominio.getSize(idEncuesta) == 0) throw new Exception("La encuesta no tiene respuestas");

        vistaPrincipal.log("Ejecutando " + algoritmo + " con k=" + k + " sobre " + idEncuesta + "...");

        long t0 = System.currentTimeMillis();
        ClusterResult res = null;

        // Llamamos a los métodos originales del controlador de dominio
        if (algoritmo.equals("K-Means")) {
            res = ctrlDominio.ejecutarKMeans(c, k, false);
        } else if (algoritmo.equals("K-Means++")) {
            res = ctrlDominio.ejecutarKMeans(c, k, true);
        } else {
            res = ctrlDominio.ejecutarKMedoids(c, k);
        }
        long t1 = System.currentTimeMillis();

        vistaPrincipal.log("Fin Clustering. Tiempo: " + (t1 - t0) + "ms");
        vistaPrincipal.log("SSE: " + ctrlDominio.getUltimoSse());
        vistaPrincipal.log("Silhouette: " + ctrlDominio.getUltimoSilhouette());
    }

    public void exportarResultadoClustering(String rutaDestino) {
        ClusterResult res = ctrlDominio.getUltimoResultado();
        String id = ctrlDominio.getUltimoResultadoEncuestaId();

        if (res == null || id == null) {
            vistaPrincipal.mostrarError("Error", "No hay resultados recientes para exportar.");
            return;
        }
        try {
            Cuestionario c = ctrlDominio.obtener(id);
            List<Respuesta> respuestas = ctrlDominio.listarRespuestas(id);
            CsvClusterExporter exporter = new CsvClusterExporter();

            exporter.exportClusterResult(
                    Path.of(rutaDestino),
                    res,
                    ctrlDominio.getUltimoSse(),
                    ctrlDominio.getUltimoSilhouette(),
                    ctrlDominio.getUltimoTiempoMs(),
                    c,
                    respuestas
            );
            vistaPrincipal.mostrarInfo("Exportar Resultado", "Fichero de clusters guardado en:\n" + rutaDestino);
        } catch (Exception e) {
            vistaPrincipal.mostrarError("Error Exportando Clusters", e.getMessage());
        }
    }

    public double calcularAccuracy(String rutaEtiquetas) throws Exception {
        ClusterResult res = ctrlDominio.getUltimoResultado();
        if (res == null) throw new Exception("No hay resultado de clustering calculado.");
        return ClusteringAccuracy.accuracyFromCsv(res, Path.of(rutaEtiquetas));
    }

    public List<Vectorizer.Point> getPuntosUltimoClustering() {
        return ctrlDominio.calcularDatosUltimaEncuesta();
    }

    public Cuestionario obtenerEncuesta(String id) {
        return ctrlDominio.obtener(id);
    }

    public void agregarRespuesta(String idEncuesta, Respuesta r) {
        ctrlDominio.agregar(idEncuesta, r);
    }

    public List<Respuesta> listarRespuestas(String idEncuesta) {
        return ctrlDominio.listarRespuestas(idEncuesta);
    }

    public String getUsuarioEmail() {
        return ctrlDominio.getUsuarioLogueadoEmail();
    }
}

