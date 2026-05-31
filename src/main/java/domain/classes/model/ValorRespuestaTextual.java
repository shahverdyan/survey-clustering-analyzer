package domain.classes.model;

public class ValorRespuestaTextual extends ValorRespuesta {
    private final String valor;

    public ValorRespuestaTextual(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String getValorNormalizado() {
        return valor;
    }

    @Override
    public String toString() {
        return this.valor;
    }
}