package domain.controllers;

import domain.classes.clustering.ClusterResult;
import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import domain.classes.model.Respuesta;
import domain.classes.model.Usuario; // Importar Usuario
import domain.classes.vector.Vectorizer;
import persistence.controllers.CtrlPersistencia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CtrlDominio {
    // Controladores especializados
    private final CtrlEncuesta ctrlEncuesta;
    private final CtrlRespuesta ctrlRespuesta;
    private final CtrlClustering ctrlClustering;

    // Referencia a persistencia para gestionar usuarios
    private final CtrlPersistencia ctrlPersistencia;

    // ESTADO DE LA SESIÓN
    private Usuario usuarioActual = null;

    public CtrlDominio(CtrlPersistencia ctrlPersistencia) {
        this.ctrlPersistencia = ctrlPersistencia;
        this.ctrlEncuesta = new CtrlEncuesta(ctrlPersistencia);
        this.ctrlRespuesta = new CtrlRespuesta(ctrlPersistencia);
        this.ctrlClustering = new CtrlClustering(ctrlPersistencia);
    }


    // GESTIÓN DE USUARIOS (LOGIN / REGISTRO)

    public void registrarUsuario(String email, String pass, String nombre, String ape, String nac) {
        if (ctrlPersistencia.Uexists(email)) {
            throw new IllegalArgumentException("El usuario ya existe.");
        }
        Usuario u = new Usuario(email, pass, nombre, ape, nac);
        ctrlPersistencia.Usave(u);
    }

    public boolean login(String email, String password) {
        if (!ctrlPersistencia.Uexists(email)) return false;

        Usuario u = ctrlPersistencia.Uget(email);
        if (u.checkPassword(password)) {
            this.usuarioActual = u;
            return true;
        }
        return false;
    }

    public void logout() {
        this.usuarioActual = null;
    }

    public String getUsuarioLogueadoNombre() {
        // Para la interfaz: Devuelve "nombre"
        return (usuarioActual != null) ? usuarioActual.getNombre() : "Invitado";
    }

    public String getUsuarioLogueadoEmail() {
        // Para la respuesta: Devuelve "nombre@mail.com"
        return (usuarioActual != null) ? usuarioActual.getEmail() : "anonimo";
    }

    public boolean estaLogueado() {
        return usuarioActual != null;
    }

    // MÉTODOS DE ENCUESTA
    public void crear(String id) {
        if (usuarioActual == null) throw new IllegalStateException("Debes iniciar sesión para crear encuestas.");

        // 1. Crear la encuesta base
        ctrlEncuesta.crear(id);

        // 2. Asignar el propietario inmediatamente
        Cuestionario c = ctrlEncuesta.obtener(id);
        c.setOwner(usuarioActual.getEmail());
        ctrlEncuesta.guardar(c);
    }


     //Guarda una encuesta. Si es una importación (owner es null), se la asigna al usuario actual.
    public void guardar(Cuestionario encuesta) {
        if (usuarioActual == null) throw new IllegalStateException("Debes iniciar sesión.");

        // Si la encuesta no tiene dueño (ej. importada), se la asignamos al usuario actual
        if (encuesta.getOwner() == null) {
            encuesta.setOwner(usuarioActual.getEmail());
        } else {
            // Protección: No guardar si intentas sobrescribir la de otro
            if (!encuesta.getOwner().equals(usuarioActual.getEmail())) {
                throw new SecurityException("No puedes modificar una encuesta que no es tuya.");
            }
        }
        ctrlEncuesta.guardar(encuesta);
    }

    public boolean eliminar(String id) {
        if (usuarioActual == null) return false;

        Cuestionario c = ctrlEncuesta.obtener(id);
        if (c != null) {
            // Solo permitir borrar si eres el dueño
            if (c.getOwner() != null && !c.getOwner().equals(usuarioActual.getEmail())) {
                throw new SecurityException("No puedes eliminar una encuesta de otro usuario.");
            }
        }
        return ctrlEncuesta.eliminar(id);
    }

    /**
     * Lista TODAS las encuestas del sistema.
     * USAR EN: Gestor de Respuestas (para verlas todas).
     */
    public Collection<Cuestionario> listar() {
        return ctrlEncuesta.listar();
    }

    /**
     * Lista SOLO las encuestas del usuario logueado.
     * USAR EN: Gestor de Encuestas (CRUD personal).
     */
    public Collection<Cuestionario> listarMisEncuestas() {
        if (usuarioActual == null) return new ArrayList<>();

        return ctrlEncuesta.listar().stream()
                .filter(c -> c.getOwner() != null && c.getOwner().equals(usuarioActual.getEmail()))
                .collect(Collectors.toList());
    }

    public Cuestionario obtener(String id) { return ctrlEncuesta.obtener(id); }
    public boolean existe(String id) { return ctrlEncuesta.existe(id); }

    // Delegación con protección de escritura
    public void agregarPregunta(String idEncuesta, Pregunta p) {
        verificarPropiedad(idEncuesta);
        ctrlEncuesta.agregarPregunta(idEncuesta, p);
    }

    public boolean eliminarPregunta(String idEncuesta, String preguntaId) {
        verificarPropiedad(idEncuesta);
        return ctrlEncuesta.eliminarPregunta(idEncuesta, preguntaId);
    }

    // Método para proteger modificaciones
    private void verificarPropiedad(String idEncuesta) {
        if (usuarioActual == null) throw new IllegalStateException("No hay sesión iniciada.");
        Cuestionario c = ctrlEncuesta.obtener(idEncuesta);
        if (c != null && c.getOwner() != null && !c.getOwner().equals(usuarioActual.getEmail())) {
            throw new SecurityException("Esta encuesta no te pertenece.");
        }
    }

    // MÉTODOS DE RESPUESTA
    public void agregar(String encuestaId, Respuesta r) { ctrlRespuesta.agregar(encuestaId, r); }
    public List<Respuesta> listarRespuestas(String encuestaId) { return ctrlRespuesta.listarRespuestas(encuestaId); }
    public boolean eliminarPorParticipante(String encuestaId, String participante) { return ctrlRespuesta.eliminarPorParticipante(encuestaId, participante); }
    public void eliminarTodas(String encuestaId) { ctrlRespuesta.eliminarTodas(encuestaId); }
    public int getSize(String encuestaId) { return ctrlRespuesta.getSize(encuestaId); }
    public void eliminarPreguntaR(String encuestaId, String preguntaId) { ctrlRespuesta.eliminarPreguntaR(encuestaId, preguntaId); }

    // MÉTODOS DE CLUSTERING
    public Cuestionario getEncuesta(String id) { return ctrlClustering.getEncuesta(id); }
    public int numRespuestas(String id) { return ctrlClustering.numRespuestas(id); }
    public ClusterResult getUltimoResultado() { return ctrlClustering.getUltimoResultado(); }
    public String getUltimoResultadoEncuestaId() { return ctrlClustering.getUltimoResultadoEncuestaId(); }
    public double getUltimoSse() { return ctrlClustering.getUltimoSse(); }
    public double getUltimoSilhouette() { return ctrlClustering.getUltimoSilhouette(); }
    public long getUltimoTiempoMs() { return ctrlClustering.getUltimoTiempoMs(); }
    public List<Vectorizer.Point> vectorizar(Cuestionario c) { return ctrlClustering.vectorizar(c); }
    public double[] evaluar(ClusterResult res, List<Vectorizer.Point> datos) { return ctrlClustering.evaluar(res, datos); }

    public ClusterResult ejecutarKMeans(Cuestionario c, int k, boolean kpp) {
        if(c.getOwner() != null && !c.getOwner().equals(usuarioActual.getEmail())) throw new SecurityException("Solo puedes clusterizar tus encuestas");
        return ctrlClustering.ejecutarKMeans(c, k, kpp);
    }

    public ClusterResult ejecutarKMedoids(Cuestionario c, int k) {
        return ctrlClustering.ejecutarKMedoids(c, k);
    }
    public List<Vectorizer.Point> calcularDatosUltimaEncuesta() { return ctrlClustering.calcularDatosUltimaEncuesta(); }
}