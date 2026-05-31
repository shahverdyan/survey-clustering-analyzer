#!/bin/bash

# 1. Situarse en EXE
cd "$(dirname "$0")"

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'
BOLD='\033[1m'

echo -e "${BOLD}=====================================================${NC}"
echo -e "${BOLD}      EJECUTANDO BATERÍA DE TESTS AUTOMÁTICA         ${NC}"
echo -e "${BOLD}=====================================================${NC}"
echo ""

# ------------------------------------------------------------------
# CONFIGURACIÓN DEL ORDEN DE LOS TESTS
# Escribe aquí la ruta de cada script RELATIVA a la carpeta EXE.
# El orden en el que los pongas aquí será el orden de ejecución.
# ------------------------------------------------------------------
LISTA_TESTS=(
    "EjecutablesPorTest/EncuestasPreguntas/test_JuegoA1_CrearYListarEncuestaBasica.sh"
    "EjecutablesPorTest/EncuestasPreguntas/test_JuegoA2_CrearConPreguntasDeTodosLosTipos.sh"
    "EjecutablesPorTest/EncuestasPreguntas/test_JuegoA3_EditarEncuesta_AgregarYEliminarPregunta.sh"
    "EjecutablesPorTest/EncuestasPreguntas/test_JuegoA4_EliminarEncuestaYRespuestas.sh"
    "EjecutablesPorTest/Respuestas/test_JuegoB1_AgregarVariasRespuestasDistintosParticipantes.sh"
    "EjecutablesPorTest/Respuestas/test_JuegoB2_EliminarRespuestaDeParticipante.sh"
    "EjecutablesPorTest/Respuestas/test_JuegoB3_EliminarTodasLasRespuestas.sh"
    "EjecutablesPorTest/Respuestas/test_JuegoB4_ValidacionesDeOpcionesInvalidas.sh"
    "EjecutablesPorTest/ImportarExportar/test_JuegoC1_ImportarYExportarMismaEncuesta.sh"
    "EjecutablesPorTest/ImportarExportar/test_JuegoC2_ExportarEncuestaSinRespuestas.sh"
    "EjecutablesPorTest/ClusteringBasico/test_JuegoD1_ClusteringKMeansRandomConPocasRespuestas.sh"
    "EjecutablesPorTest/ClusteringBasico/test_JuegoD2_ClusteringKMeansPlusPlus.sh"
    "EjecutablesPorTest/ClusteringBasico/test_JuegoD3_ClusteringKMedoidsPAM.sh"
    "EjecutablesPorTest/ClusteringBasico/test_JuegoD4_SinRespuestasNoSePuedeVectorizar.sh"
    "EjecutablesPorTest/ClusteringBasico/test_JuegoD5_VectorizarCuestionarioConTiposMixtos.sh"
    "EjecutablesPorTest/ClusteringAvanzado/test_JuegoE1_VerUltimoResultadoYExportarClustering.sh"
    "EjecutablesPorTest/ClusteringAvanzado/test_JuegoE2_CalcularAccuracyLoanTrain.sh"
    "EjecutablesPorTest/ClusteringAvanzado/test_JuegoE3_AccuracyConKIncorrectoDebeFallar.sh"
    "EjecutablesPorTest/ClusteringAvanzado/test_JuegoE4_ClusteringKMeansYKMedoidsComparacion.sh"
    "EjecutablesPorTest/ComparaciondeFicheros/test_JuegoF1_CompararFicherosIguales.sh"
    "EjecutablesPorTest/ComparaciondeFicheros/test_JuegoF2_CompararFicherosConDiferencias.sh"
    "EjecutablesPorTest/FlujosIntegrados/test_JuegoG1_FlujoCompleto_CrearEditarAnadir_ClusteringExportarComparar.sh"
    "EjecutablesPorTest/FlujosIntegrados/test_JuegoG2_FlujoImportar_AnadirRespuestas_DosAlgoritmos_Accuracy.sh"
    "EjecutablesPorTest/FlujosIntegrados/test_JuegoG3_MultiplesEncuestasConDiferentesRespuestas.sh"
)
# ------------------------------------------------------------------

# Contadores
TOTAL=0
PASSED=0
FAILED=0

# Archivo temporal para guardar logs si algo falla
LOG_FILE="bateria_error_log.txt"
> "$LOG_FILE" # Limpiamos el log anterior

# 2. BUCLE SOBRE LA LISTA ORDENADA
# Usamos un 'for' en lugar de 'find'. Esto respeta tu orden.
for script in "${LISTA_TESTS[@]}"; do

    # Verificamos si el archivo existe antes de intentar ejecutarlo
    if [ ! -f "$script" ]; then
        echo -e "${RED}[ERROR] No se encuentra el script: $script${NC}"
        echo "Revisa la lista en ejecutar_bateria_tests.sh"
        FAILED=$((FAILED+1))
        continue
    fi

    TOTAL=$((TOTAL+1))

    # Obtenemos el nombre bonito del archivo para mostrarlo
    test_name=$(basename "$script")

    # Imprimimos "Ejecutando test X..." sin salto de línea (-n)
    echo -n "Ejecutando $test_name ... "

    # 3. EJECUCIÓN MAESTRA
    # Llamamos al script pasando el argumento "auto".
    # &> silencia stdout y stderr del script hijo

    chmod +x "$script" # Aseguramos permisos por si acaso
    ./"$script" auto >> "$LOG_FILE" 2>&1

    # Capturamos el resultado ($? es 0 si fue bien, 1 si hizo exit 1)
    EXIT_CODE=$?

    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}[ OK ]${NC}"
        PASSED=$((PASSED+1))
    else
        echo -e "${RED}[ FALLO ]${NC}"
        FAILED=$((FAILED+1))
        echo "-----------------------------------" >> "$LOG_FILE"
        echo "FALLO EN: $test_name" >> "$LOG_FILE"
        echo "-----------------------------------" >> "$LOG_FILE"
    fi

    # NOTA: Ya no necesitamos guardar variables en .total/.passed
    # porque el bucle 'for' NO crea una subshell como lo hacía el pipe '|'
done

# Resumen final
echo ""
echo -e "${BOLD}=====================================================${NC}"
echo -e "RESUMEN FINAL:"
echo -e "Total Tests: $TOTAL"
echo -e "${GREEN}Pasados:     $PASSED${NC}"

if [ $FAILED -gt 0 ]; then
    echo -e "${RED}Fallados:    $FAILED${NC}"
    echo ""
    echo -e "${RED}Se han encontrado errores.${NC}"
    echo "Revisa el archivo '$LOG_FILE' para ver los detalles de los fallos."
else
    # Solo borramos el log si realmente se ejecutó algún test
    if [ $TOTAL -gt 0 ]; then
        echo -e "${GREEN}TODOS LOS TESTS HAN PASADO CORRECTAMENTE.${NC}"
        rm "$LOG_FILE"
    else
        echo -e "${RED}No se ha ejecutado ningún test. Revisa la lista.${NC}"
    fi
fi
echo -e "${BOLD}=====================================================${NC}"

# Pausa final de la batería
echo ""
read -p "Presiona ENTER para salir..."