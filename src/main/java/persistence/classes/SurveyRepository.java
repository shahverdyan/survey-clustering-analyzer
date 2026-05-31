package persistence.classes;

import domain.classes.model.Cuestionario;
import java.io.*;
import java.util.*;

public class SurveyRepository {
    private final Map<String, Cuestionario> encuestas = new LinkedHashMap<>();

    public Collection<Cuestionario> list() {
        return Collections.unmodifiableCollection(encuestas.values());
    }

    public void create(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Id vacio");
        if (encuestas.containsKey(id)) throw new IllegalArgumentException("Ya existe ID: " + id);
        encuestas.put(id, new Cuestionario(id));
    }

    public void put(Cuestionario c) {
        if (c == null) throw new IllegalArgumentException("Cuestionario nulo");
        if (c.getId() == null || c.getId().isBlank()) throw new IllegalArgumentException("Sin ID");
        encuestas.put(c.getId(), c);
    }

    public boolean delete(String id) {
        if (id == null) return false;
        return encuestas.remove(id) != null;
    }

    public boolean exists(String id) {
        return id != null && encuestas.containsKey(id);
    }

    public Cuestionario get(String id) {
        if (id == null) return null;
        return encuestas.get(id);
    }

    @SuppressWarnings("unchecked")
    public void cargarDeDisco(String ruta) {
        File f = new File(ruta);
        if (!f.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Map<String, Cuestionario> datos = (Map<String, Cuestionario>) ois.readObject();
            encuestas.clear();
            encuestas.putAll(datos);
        } catch (Exception e) {
            System.err.println("Error cargando encuestas: " + e.getMessage());
        }
    }

    public void guardarEnDisco(String ruta) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(encuestas);
        } catch (Exception e) {
            System.err.println("Error guardando encuestas: " + e.getMessage());
        }
    }
}