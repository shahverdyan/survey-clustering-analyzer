
package domain.classes.vector;

import domain.classes.model.ValorRespuesta;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//definción de la clase
public class VectorSchema {
    private final Map<String, List<String>> oneHot = new LinkedHashMap<>();
    private final Map<String, double[]> numericRanges = new LinkedHashMap<>();
    private final Map<String, ValorRespuesta> defaultValues = new LinkedHashMap<>();
    private final Map <String, double[]> outlierLimits = new LinkedHashMap<>();
    private int dimensions;

    public void setOneHot(String questionId, List<String> options) {
        this.oneHot.put(questionId, List.copyOf(options));
    }

    public void setNumericRange(String questionId, double min, double max) {
        this.numericRanges.put(questionId, new double[]{min, max});
    }

    public void setDefaultValue(String questionId, ValorRespuesta valor) {
        System.out.println("DEBUG: Dado que la respuesta es nula o vacia para la pregunta '" + questionId + "', guardaremos como respuesta la media: " + valor);
        this.defaultValues.put(questionId, valor);
    }

    public ValorRespuesta getDefaultValue(String questionId) {
        return this.defaultValues.get(questionId);
    }

    public void setOutlierLimits(String questionId, double lowerLimit, double upperLimit) {
        this.outlierLimits.put(questionId, new double[]{lowerLimit, upperLimit});
    }

    public double[] getOutlierLimits(String questionId) {
        return this.outlierLimits.get(questionId);
    }

    public List<String> getOptions(String questionId) {
        return oneHot.getOrDefault(questionId, List.of());
    }

    public double[] getRange(String questionId) {
        return (double[])this.numericRanges.get(questionId);
    }

    public void setDimensions(int d) {
        this.dimensions = d;
    }

    public int getDimensions() {
        return this.dimensions;
    }
}