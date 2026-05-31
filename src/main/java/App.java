

import domain.controllers.CtrlDominio;
import persistence.controllers.CtrlPersistencia;
import presentation.controllers.CtrlPresentacion;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // 1. Inicialización de la Capa de Persistencia
                // (Al crearse, ahora cargará automáticamente los datos si existen)
                CtrlPersistencia ctrlPersistencia = new CtrlPersistencia();

                // 2. Inicialización de la Capa de Dominio
                CtrlDominio ctrlDominio = new CtrlDominio(ctrlPersistencia);

                // 3. Inicialización de la Capa de Presentación
                CtrlPresentacion ctrlPresentacion = new CtrlPresentacion(ctrlDominio);

                // GUARDADO AUTOMÁTICO AL CERRAR
                // Este bloque se ejecuta siempre que cierras la ventana o paras el programa.
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\n>> Cerrando aplicación... Guardando datos...");
                    ctrlPersistencia.guardarDatos();
                }));

                // 4. Inicio de la Aplicación
                ctrlPresentacion.inicializarPresentacion();
            }
        });
    }
}