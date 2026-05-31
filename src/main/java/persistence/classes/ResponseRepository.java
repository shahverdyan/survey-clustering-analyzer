package persistence.classes;

import domain.classes.model.Respuesta;
import java.io.*;
import java.util.*;

public class ResponseRepository {
    private final Map<String, List<Respuesta>> porEncuesta = new HashMap<>();

    private List<Respuesta> bucket(String encuestaId) {
        return porEncuesta.computeIfAbsent(encuestaId, k -> new ArrayList<>());
    }

    public void add(String encuestaId, Respuesta r) {
        bucket(encuestaId).add(r);
    }

    public void clear(String encuestaId) {
        porEncuesta.remove(encuestaId);
    }

    public int size(String encuestaId) {
        return porEncuesta.getOrDefault(encuestaId, List.of()).size();
    }

    public List<Respuesta> all(String encuestaId) {
        return Collections.unmodifiableList(porEncuesta.getOrDefault(encuestaId, List.of()));
    }

    public boolean removeById(String encuestaId, String participanteId) {
        List<Respuesta> l = porEncuesta.get(encuestaId);
        if (l == null) return false;
        return l.removeIf(r -> r.getIdParticipante().equals(participanteId));
    }

    public void removeQuestion(String encuestaId, String preguntaId) {
        List<Respuesta> l = porEncuesta.get(encuestaId);
        if (l == null) return;
        for (Respuesta r : l) {
            r.removePregunta(preguntaId);
        }
    }

    // --- Persistencia (Binario) ---

    @SuppressWarnings("unchecked")
    public void cargarDeDisco(String ruta) {
        File f = new File(ruta);
        if (!f.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Map<String, List<Respuesta>> datos = (Map<String, List<Respuesta>>) ois.readObject();
            porEncuesta.clear();
            porEncuesta.putAll(datos);
        } catch (Exception e) {
            System.err.println("Error cargando respuestas: " + e.getMessage());
        }
    }

    public void guardarEnDisco(String ruta) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(porEncuesta);
        } catch (Exception e) {
            System.err.println("Error guardando respuestas: " + e.getMessage());
        }
    }
}