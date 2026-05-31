package domain.classes.model;

public class ValorRespuestaNumerica extends ValorRespuesta {
    private final double valor;

    public ValorRespuestaNumerica(double valor) {
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }

    @Override
    public String getValorNormalizado() {
        return String.valueOf(valor);
    }

    @Override
    public String toString() {
        // Convierte el double a string.
        return String.valueOf(this.valor);
    }
}