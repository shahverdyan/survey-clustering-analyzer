package persistence.controllers;

import persistence.classes.SurveyRepository;
import persistence.classes.ResponseRepository;
import persistence.classes.UserRepository;
import domain.classes.model.Cuestionario;
import domain.classes.model.Respuesta;
import domain.classes.model.Usuario;

import java.util.Collection;
import java.util.List;
import java.io.File; // <--- Importante para manejar directorios

// Gestiona los repositorios y la carga/guardado de datos
public class CtrlPersistencia {

    private final SurveyRepository surveyRepository;
    private final ResponseRepository responseRepository;
    private final UserRepository userRepository;

    // Archivos de persistencia
    private final String FILE_ENCUESTAS = "data/datos_encuestas.bin";
    private final String FILE_RESPUESTAS = "data/datos_respuestas.bin";

    public CtrlPersistencia() {
        this.surveyRepository = new SurveyRepository();
        this.responseRepository = new ResponseRepository();
        this.userRepository = new UserRepository();

        cargarDatos();
    }

    private void cargarDatos() {
        // Carga inicial al arrancar
        //System.out.println("--- Iniciando Carga de Datos ---"); lo quito xq aparece al inicio del output de los juegos de prueba y no tiene por que aparecer
        userRepository.cargarDeDisco();
        surveyRepository.cargarDeDisco(FILE_ENCUESTAS);
        responseRepository.cargarDeDisco(FILE_RESPUESTAS);
        //System.out.println("--------------------------------");
    }

    public void guardarDatos() {
        System.out.println("--- Guardando Datos ---");

        // Aseguramos que la carpeta exista antes de escribir
        asegurarCarpetaData();

        // Guardamos
        userRepository.guardarEnDisco();
        surveyRepository.guardarEnDisco(FILE_ENCUESTAS);
        responseRepository.guardarEnDisco(FILE_RESPUESTAS);
        System.out.println("-----------------------");
    }

    //Por si se borra la carpeta data
    private void asegurarCarpetaData() {
        File directorio = new File("data");
        if (!directorio.exists()) {
            boolean creado = directorio.mkdirs();
            if (creado) {
                System.out.println(">> Carpeta 'data' creada automáticamente.");
            }
        }
    }

    // Delegación Encuestas
    public Collection<Cuestionario> Elist() { return surveyRepository.list(); }
    public void Ecreate(String id) { surveyRepository.create(id); }
    public void Eput(Cuestionario c) { surveyRepository.put(c); }
    public boolean Edelete(String id) { return surveyRepository.delete(id); }
    public boolean Eexists(String id) { return surveyRepository.exists(id); }
    public Cuestionario Eget(String id) { return surveyRepository.get(id); }

    // Delegación Respuestas
    public void Radd(String encuestaId, Respuesta r) { responseRepository.add(encuestaId, r); }
    public void Rclear(String encuestaId) { responseRepository.clear(encuestaId); }
    public int Rsize(String encuestaId) { return responseRepository.size(encuestaId); }
    public List<Respuesta> Rall(String encuestaId) { return responseRepository.all(encuestaId); }
    public boolean RremoveById(String eId, String pId) { return responseRepository.removeById(eId, pId); }
    public void RremoveQuestion(String eId, String qId) { responseRepository.removeQuestion(eId, qId); }

    // Delegación Usuarios
    public void Usave(Usuario u) { userRepository.guardarUsuario(u); }
    public Usuario Uget(String email) { return userRepository.getUsuario(email); }
    public boolean Uexists(String email) { return userRepository.existeUsuario(email); }
}