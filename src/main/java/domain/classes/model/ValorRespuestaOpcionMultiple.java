package domain.classes.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValorRespuestaOpcionMultiple extends ValorRespuesta {
    // Almacena una lista de los índices de las opciones seleccionadas.
    private final List<Integer> indicesSeleccionados;

    public ValorRespuestaOpcionMultiple(List<Integer> indicesSeleccionados) {
        this.indicesSeleccionados = Collections.unmodifiableList(indicesSeleccionados);
    }

    public List<Integer> getIndicesSeleccionados() {
        return indicesSeleccionados;
    }

    @Override
    public String getValorNormalizado() {
        return indicesSeleccionados.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.indicesSeleccionados.toString();
    }
}