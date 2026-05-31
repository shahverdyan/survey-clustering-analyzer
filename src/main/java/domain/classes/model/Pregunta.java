package domain.classes.model;

import java.util.List;
import java.io.Serializable;

public class Pregunta implements Serializable {
    private final String id;
    private final String enunciado;
    private final TipoPregunta tipo;
    private final List<String> opciones;
    private final int maxSeleccion;

    public Pregunta(String id, String enunciado, TipoPregunta tipo, List<String> opciones, int maxSeleccion) {
        this.id = id;
        this.enunciado = enunciado;
        this.tipo = tipo;
        this.opciones = opciones;
        this.maxSeleccion = maxSeleccion;
    }

    public String getId() {
        return this.id;
    }
    public String getEnunciado() {
        return this.enunciado;
    }
    public TipoPregunta getTipo() {
        return this.tipo;
    }
    public List<String> getOpciones() {
        return this.opciones;
    }
    public int getMaxSeleccion() {
        return this.maxSeleccion;
    }
}