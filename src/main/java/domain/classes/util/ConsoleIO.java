package domain.classes.util;

import java.util.Scanner;

public class ConsoleIO {

    public enum Modo { MANUAL, AUTOMATICO }

    private final Scanner sc = new Scanner(System.in);
    private Modo modo = Modo.AUTOMATICO;

    public void setModo(Modo modo) {
        this.modo = modo;
    }

    public Modo getModo() {
        return modo;
    }

    public String prompt(String label){
        if (modo == Modo.MANUAL) {
            System.out.print(label + ": ");
        }
        if (!sc.hasNextLine()) {
            System.out.println("\nNo hay entrada interactiva.");
            System.exit(0);
        }
        return sc.nextLine().trim();
    }

    public String promptDefault(String label, String defaultVal){
        if (modo == Modo.MANUAL) {
            System.out.print(label + " [" + defaultVal + "]: ");
        }
        if (!sc.hasNextLine()) {
            System.out.println("\nNo hay entrada interactiva.");
            System.exit(0);
        }
        String s = sc.nextLine();
        return s.isBlank() ? defaultVal : s.trim();
    }

    public int promptInt(String label){
        while (true){
            try { return Integer.parseInt(prompt(label)); }
            catch (NumberFormatException e){
                if (modo == Modo.MANUAL) System.out.println("Introduce un entero válido.");
            }
        }
    }
}
