package persistence.classes;

import domain.classes.model.Usuario;
import java.io.*; // <--- Necesario para leer/escribir archivos
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    // Mapa en memoria: Email ---> Usuario
    private Map<String, Usuario> usuarios = new HashMap<>();

    // Nombre del archivo donde se guardarán los datos (se crea en la carpeta data del proyecto)
    private final String RUTA_CSV = "data/usuarios.csv";

    public void guardarUsuario(Usuario u) {
        if (u == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (usuarios.containsKey(u.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + u.getEmail());
        }
        usuarios.put(u.getEmail(), u);
    }

    public Usuario getUsuario(String email) {
        return usuarios.get(email);
    }

    public boolean existeUsuario(String email) {
        return usuarios.containsKey(email);
    }

    public Map<String, Usuario> getTodos() {
        return usuarios;
    }

    // Leer del archivo al iniciar la app
    public void cargarDeDisco() {
        File f = new File(RUTA_CSV);
        if (!f.exists()) return; // Si es la primera vez y no hay archivo, no hacemos nada

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // El formato esperado es: email,pass,nombre,apellido,nacimiento
                String[] partes = linea.split(",");
                if (partes.length == 5) {
                    Usuario u = new Usuario(partes[0], partes[1], partes[2], partes[3], partes[4]);
                    usuarios.put(u.getEmail(), u);
                }
            }
            System.out.println("-> Usuarios cargados desde disco: " + usuarios.size());
        } catch (IOException e) {
            System.err.println("Error leyendo usuarios: " + e.getMessage());
        }
    }

    // Guardar al archivo al cerrar la app
    public void guardarEnDisco() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_CSV))) {
            for (Usuario u : usuarios.values()) {
                // Escribimos una línea por usuario separada por comas
                String linea = String.join(",",
                        u.getEmail(), u.getPassword(), u.getNombre(), u.getApellido(), u.getFechaNacimiento());
                bw.write(linea);
                bw.newLine();
            }
            System.out.println("-> Usuarios guardados en " + RUTA_CSV);
        } catch (IOException e) {
            System.err.println("Error guardando usuarios: " + e.getMessage());
        }
    }
}