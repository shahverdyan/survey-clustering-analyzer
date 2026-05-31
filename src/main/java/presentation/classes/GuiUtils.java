package presentation.classes;

import javax.swing.*;
import java.awt.*;

public class GuiUtils {

    /**
     * Recorre recursivamente todos los componentes de un contenedor.
     * Si encuentra una etiqueta o botón sin tooltip, le asigna su propio texto como tooltip.
     */
    public static void asignarTooltipsGlobales(Container container) {
        for (Component c : container.getComponents()) {

            // 1. Si es JLabel
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (lbl.getToolTipText() == null && lbl.getText() != null && !lbl.getText().isEmpty()) {
                    // Quitamos etiquetas HTML si las hay para el tooltip simple, o lo dejamos tal cual
                    lbl.setToolTipText(lbl.getText().replaceAll("<[^>]*>", ""));
                }
            }

            // 2. Si es JButton
            else if (c instanceof JButton) {
                JButton btn = (JButton) c;
                // Solo asignamos si no tiene uno ya (para no sobrescribir los específicos de lógica)
                if (btn.getToolTipText() == null && btn.getText() != null && !btn.getText().isEmpty()) {
                    btn.setToolTipText(btn.getText());
                }
            }

            // 3. Si es CheckBox o RadioButton
            else if (c instanceof JToggleButton) {
                JToggleButton btn = (JToggleButton) c;
                if (btn.getToolTipText() == null && btn.getText() != null) {
                    btn.setToolTipText(btn.getText());
                }
            }

            // 4. RECURSIVIDAD: Si el componente es un panel o contenedor, entramos dentro
            if (c instanceof Container) {
                asignarTooltipsGlobales((Container) c);
            }
        }
    }
}