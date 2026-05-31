# Survey Clustering Analyzer

Aplicación Java para crear y gestionar encuestas con análisis de comportamiento por clustering. Permite agrupar automáticamente a los participantes según sus respuestas usando algoritmos de clustering sobre datos mixtos (numéricos, textuales y categóricos).

---

## ¿Qué hace la aplicación?

La app tiene dos modos de uso: **interfaz gráfica (Swing)** y **driver de terminal** para testing automatizado.

### Funcionalidades principales

- **Gestión de encuestas** — Crea encuestas con preguntas de cuatro tipos: numérica, texto libre, opción única y opción múltiple. Edita y elimina encuestas propias.
- **Gestión de respuestas** — Añade y elimina respuestas de participantes a cualquier encuesta del sistema.
- **Clustering de participantes** — Agrupa automáticamente las respuestas en *k* clusters usando tres algoritmos:
  - **K-Means** (inicialización aleatoria)
  - **K-Means++** (inicialización inteligente)
  - **K-Medoids / PAM**
- **Evaluación de resultados** — Calcula métricas de calidad del clustering: SSE, índice de Silhouette y Accuracy (contra etiquetas reales de un CSV).
- **Importar / Exportar** — Importa encuestas y respuestas desde CSV. Exporta resultados de clustering a CSV.
- **Gestión de usuarios** — Registro e inicio de sesión. Cada encuesta pertenece a su creador.
- **Persistencia automática** — Los datos se guardan al cerrar y se cargan al iniciar.

### Arquitectura

```
src/main/java/
├── presentation/   ← GUI Swing
├── domain/         ← Lógica de negocio, clustering, vectorización
├── persistence/    ← Repositorios y serialización a disco
└── drivers/        ← Driver de terminal para testing
```

La distancia entre respuestas se calcula con **distancia de Gower**, que maneja de forma nativa la mezcla de tipos de datos.

---

## Requisitos

- **Java 21** o superior
- No se necesita ninguna instalación adicional; el proyecto incluye el Gradle Wrapper

---

## Ejecución

Todos los scripts están en la carpeta `EXE/`. En Linux/Mac, la primera vez hay que dar permisos:

```bash
find EXE -name "*.sh" -exec chmod +x {} +
```

En Windows usa los equivalentes `.bat`.

### Lanzar la aplicación (GUI)

```bash
./EXE/ejecutar_app.sh
```

### Lanzar el driver de terminal

Útil para probar el flujo completo de forma interactiva desde la terminal:

```bash
./EXE/ejecutar_driver_principal.sh
```

### Recompilar

Si modificas el código fuente, regenera el `program.jar`:

```bash
./EXE/recompilar.sh
```

Esto ejecuta `./gradlew clean jar` y copia el resultado a `EXE/program.jar`.

---

## Tests

### Tests unitarios (JUnit)

Validan la lógica interna de clustering, modelo, persistencia y vectorización:

```bash
./EXE/ejecutar_tests_unitarios.sh
```

O directamente con Gradle:

```bash
./gradlew clean test
```

### Batería de tests de integración

24 juegos de prueba que lanzan el driver con entradas predefinidas y comparan la salida contra el resultado esperado:

```bash
./EXE/ejecutar_bateria_tests.sh
```

Al finalizar muestra un resumen de tests pasados y fallados. Si alguno falla, los detalles quedan en `EXE/bateria_error_log.txt`.

### Tests individuales

Los tests están organizados por categoría en `EXE/EjecutablesPorTest/`:

| Carpeta | Qué prueba |
|---|---|
| `EncuestasPreguntas/` | Crear, listar, editar y eliminar encuestas (A1–A4) |
| `Respuestas/` | Añadir, eliminar y validar respuestas (B1–B4) |
| `ImportarExportar/` | Importar y exportar encuestas via CSV (C1–C2) |
| `ClusteringBasico/` | K-Means, K-Means++, K-Medoids y vectorización (D1–D5) |
| `ClusteringAvanzado/` | Exportar clustering, Accuracy, comparación de algoritmos (E1–E4) |
| `ComparaciondeFicheros/` | Comparar ficheros de salida (F1–F2) |
| `FlujosIntegrados/` | Flujos completos de extremo a extremo (G1–G3) |

Para ejecutar un test individual:

```bash
cd EXE/EjecutablesPorTest/EncuestasPreguntas
./test_JuegoA1_CrearYListarEncuestaBasica.sh
```

Los inputs y outputs esperados de cada test están en `EXE/JuegosDePrueba/`.

---

## Comandos Gradle útiles

```bash
./gradlew test          # Ejecutar tests unitarios
./gradlew run           # Lanzar la app desde Gradle (con entrada interactiva)
./gradlew jar           # Generar el JAR en build/libs/
./gradlew clean         # Limpiar artefactos de compilación
```

---

## Estructura del proyecto

```
.
├── src/
│   ├── main/java/      ← Código fuente
│   └── test/java/      ← Tests unitarios
├── EXE/
│   ├── EjecutablesPorTest/   ← Scripts de test de integración
│   ├── JuegosDePrueba/       ← Inputs y outputs esperados
│   ├── csvFiles/             ← CSVs de ejemplo
│   └── *.sh / *.bat          ← Scripts de ejecución
├── DOCS/               ← Documentación y diagramas
├── data/               ← Datos persistidos (generado en ejecución)
└── build.gradle
```
