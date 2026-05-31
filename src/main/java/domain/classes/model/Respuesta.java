package domain.classes.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.Serializable;

public class Respuesta implements Serializable {

    private final String idParticipante;
    private final Map<String, ValorRespuesta> valores = new LinkedHashMap<>();

    public Respuesta(String idParticipante) {
        this.idParticipante = idParticipante;
    }

    public String getIdParticipante() {
        return this.idParticipante;
    }

    public Map<String, ValorRespuesta> getValores() {
        return Collections.unmodifiableMap(this.valores);
    }

    public void put(String idPregunta, ValorRespuesta valor) {
        this.valores.put(idPregunta, valor);
    }

    public void removePregunta(String idPregunta) {
        this.valores.remove(idPregunta);
    }
}