# Survey Clustering Analyzer

A Java application for creating and managing surveys with behavior analysis through clustering. It automatically groups participants based on their responses using clustering algorithms over mixed-type data (numeric, text, and categorical).

---

## What does it do?

The app has two usage modes: a **graphical interface (Swing)** and a **terminal driver** for automated testing.

### Main features

- **Survey management** — Create surveys with four question types: numeric, free text, single choice, and multiple choice. Edit and delete your own surveys.
- **Response management** — Add and remove participant responses to any survey in the system.
- **Participant clustering** — Automatically group responses into *k* clusters using three algorithms:
  - **K-Means** (random initialization)
  - **K-Means++** (smart initialization)
  - **K-Medoids / PAM**
- **Result evaluation** — Computes clustering quality metrics: SSE, Silhouette index, and Accuracy (against ground-truth labels from a CSV).
- **Import / Export** — Import surveys and responses from CSV. Export clustering results to CSV.
- **User management** — Register and log in. Each survey belongs to its creator.
- **Automatic persistence** — Data is saved on exit and loaded on startup.

### Architecture

```
src/main/java/
├── presentation/   ← Swing GUI
├── domain/         ← Business logic, clustering, vectorization
├── persistence/    ← Repositories and disk serialization
└── drivers/        ← Terminal driver for testing
```

Distance between responses is computed using **Gower distance**, which natively handles mixed data types.

---

## Requirements

- **Java 21** or higher
- No additional installation needed — the project includes the Gradle Wrapper

---

## Running the app

All scripts are in the `EXE/` folder. On Linux/Mac, grant execution permissions the first time:

```bash
find EXE -name "*.sh" -exec chmod +x {} +
```

On Windows, use the equivalent `.bat` files.

### Launch the application (GUI)

```bash
./EXE/ejecutar_app.sh
```

### Launch the terminal driver

Useful for interactively testing the full flow from the terminal:

```bash
./EXE/ejecutar_driver_principal.sh
```

### Recompile

If you modify the source code, regenerate `program.jar`:

```bash
./EXE/recompilar.sh
```

This runs `./gradlew clean jar` and copies the result to `EXE/program.jar`.

---

## Tests

### Unit tests (JUnit)

Validate the internal logic of clustering, model, persistence, and vectorization:

```bash
./EXE/ejecutar_tests_unitarios.sh
```

Or directly with Gradle:

```bash
./gradlew clean test
```

### Integration test suite

24 test cases that launch the driver with predefined inputs and compare the output against the expected result:

```bash
./EXE/ejecutar_bateria_tests.sh
```

At the end it prints a summary of passed and failed tests. If any fail, details are saved to `EXE/bateria_error_log.txt`.

### Individual tests

Tests are organized by category inside `EXE/EjecutablesPorTest/`:

| Folder | What it tests |
|---|---|
| `EncuestasPreguntas/` | Create, list, edit and delete surveys (A1–A4) |
| `Respuestas/` | Add, remove and validate responses (B1–B4) |
| `ImportarExportar/` | Import and export surveys via CSV (C1–C2) |
| `ClusteringBasico/` | K-Means, K-Means++, K-Medoids and vectorization (D1–D5) |
| `ClusteringAvanzado/` | Export clustering, Accuracy, algorithm comparison (E1–E4) |
| `ComparaciondeFicheros/` | Compare output files (F1–F2) |
| `FlujosIntegrados/` | Full end-to-end flows (G1–G3) |

To run an individual test:

```bash
cd EXE/EjecutablesPorTest/EncuestasPreguntas
./test_JuegoA1_CrearYListarEncuestaBasica.sh
```

Inputs and expected outputs for each test are in `EXE/JuegosDePrueba/`.

---

## Useful Gradle commands

```bash
./gradlew test          # Run unit tests
./gradlew run           # Launch the app from Gradle (with interactive input)
./gradlew jar           # Generate the JAR in build/libs/
./gradlew clean         # Clean compilation artifacts
```

---

## Project structure

```
.
├── src/
│   ├── main/java/      ← Source code
│   └── test/java/      ← Unit tests
├── EXE/
│   ├── EjecutablesPorTest/   ← Integration test scripts
│   ├── JuegosDePrueba/       ← Test inputs and expected outputs
│   ├── csvFiles/             ← Sample CSV files
│   └── *.sh / *.bat          ← Execution scripts
├── DOCS/               ← Documentation and diagrams
├── data/               ← Persisted data (generated at runtime)
└── build.gradle
```
