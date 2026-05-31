package domain.classes.model;

public class ValorRespuestaOpcionUnica extends ValorRespuesta {
    // Almacena el índice de la opción seleccionada.
    private final int indiceSeleccionado;

    public ValorRespuestaOpcionUnica(int indiceSeleccionado) {
        this.indiceSeleccionado = indiceSeleccionado;
    }

    public int getIndiceSeleccionado() {
        return indiceSeleccionado;
    }

    @Override
    public String getValorNormalizado() {
        // Devuelve el índice como String para normalización
        return String.valueOf(indiceSeleccionado);
    }

    @Override
    public String toString() {
        // Devuelve el índice + 1 para que sea legible (opción 1, opción 2...)
        // O simplemente el índice.
        return String.valueOf(this.indiceSeleccionado);
    }
}