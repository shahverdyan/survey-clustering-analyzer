package domain.controllers;

import domain.classes.model.Cuestionario;
import domain.classes.model.Pregunta;
import persistence.controllers.CtrlPersistencia;

import java.util.Collection;


public class CtrlEncuesta {

    private final CtrlPersistencia ctrlPersistencia;

    public CtrlEncuesta(CtrlPersistencia ctrlPersistencia) {
        this.ctrlPersistencia = ctrlPersistencia;
    }


    public void crear(String id) {
        ctrlPersistencia.Ecreate(id);
    }

    public Cuestionario obtener(String id) {
        return ctrlPersistencia.Eget(id);
    }

    public Collection<Cuestionario> listar() {
        return ctrlPersistencia.Elist();
    }

    public void guardar(Cuestionario encuesta) {
        ctrlPersistencia.Eput(encuesta);
    }

    public boolean eliminar(String id) {
        return ctrlPersistencia.Edelete(id);
    }

    public boolean existe(String id) {
        return ctrlPersistencia.Eexists(id);
    }

    public void agregarPregunta(String idEncuesta, Pregunta p) {
        Cuestionario e = obtener(idEncuesta);
        if (e == null) throw new IllegalArgumentException("La encuesta no existe");
        e.addPregunta(p);
        guardar(e);
    }

    public boolean eliminarPregunta(String idEncuesta, String preguntaId) {
        Cuestionario e = obtener(idEncuesta);
        if (e == null) throw new IllegalArgumentException("La encuesta no existe");
        boolean eliminado = e.eliminarPregunta(preguntaId);
        if (eliminado) guardar(e);
        return eliminado;
    }
}