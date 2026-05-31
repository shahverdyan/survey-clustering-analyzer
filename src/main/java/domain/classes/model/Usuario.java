package domain.classes.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private String fechaNacimiento;

    public Usuario(String email, String password, String nombre, String apellido, String fechaNacimiento) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEmail() {
        return email;
    }
    public String getNombre() {
        return nombre;
    }

    public boolean checkPassword(String input) {
        return this.password.equals(input);
    }

    public String getPassword() {
        return password;
    }
    public String getApellido() {
        return apellido;
    }
    public String getFechaNacimiento() {
        return fechaNacimiento;
    }
}