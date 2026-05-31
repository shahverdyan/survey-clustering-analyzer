package domain.classes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class Cuestionario implements Serializable {
    private final String id;
    private String ownerId;
    private final List<Pregunta> preguntas = new ArrayList<>();
    private final Map<String, Pregunta> idx = new HashMap<>();

    public Cuestionario(String id) {
        this.id = id;
    }

    public void setOwner(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwner() {
        return this.ownerId;
    }

    public String getId() {
        return this.id;
    }

    public List<Pregunta> getPreguntas() {
        return Collections.unmodifiableList(this.preguntas);
    }

    public boolean existsPregunta(String idPregunta) {
        return this.idx.containsKey(idPregunta);
    }

    public void addPregunta(Pregunta p) {
        if (this.idx.containsKey(p.getId())) {
            throw new IllegalArgumentException("Ya existe una pregunta con id: " + p.getId());
        } else {
            this.preguntas.add(p);
            this.idx.put(p.getId(), p);
        }
    }

    public Pregunta getPregunta(String idPregunta) {
        return this.idx.get(idPregunta);
    }

    public boolean eliminarPregunta(String idPregunta) {
        Pregunta p = this.idx.remove(idPregunta);
        if (p == null) {
            return false;
        } else {
            this.preguntas.remove(p);
            return true;
        }
    }

    public boolean reemplazarPregunta(String idPregunta, Pregunta nueva) {
        Pregunta old = this.idx.get(idPregunta);
        if (old == null) {
            return false;
        } else {
            int pos = this.preguntas.indexOf(old);
            this.preguntas.set(pos, nueva);
            this.idx.put(idPregunta, nueva);
            return true;
        }
    }
}