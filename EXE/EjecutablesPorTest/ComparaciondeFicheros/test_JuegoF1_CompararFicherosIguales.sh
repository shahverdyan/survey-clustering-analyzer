#!/bin/bash

# Aseguramos que la terminal se ubique dentro de la carpeta del test
cd "$(dirname "$0")"

# CONFIGURACIÓN DE RUTAS

# Ruta al JAR (subir 2 niveles hasta EXE)
JAR_PATH="../../program.jar"

# Ruta al INPUT
INPUT_FILE="../../JuegosDePrueba/ComparaciondeFicheros/JuegoF1_CompararFicherosIguales.txt"

# Ruta al OUTPUT ESPERADO
EXPECTED_FILE="../../JuegosDePrueba/ComparaciondeFicheros/output_esperado_F1.txt"

# Archivo temporal para comparar
TEMP_OUTPUT="resultado_actual.tmp"

# Clase principal (Driver)
MAIN_CLASS="drivers.Driver"

# Colores para la terminal
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo "-----------------------------------------------------"
echo "Ejecutando Test: Juego F1 (Comparar ficheros iguales)"
echo "Input: $INPUT_FILE"
echo "-----------------------------------------------------"

# Verificamos que existen los 3 archivos clave antes de empezar
if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}ERROR:${NC} No encuentro el archivo program.jar en: $JAR_PATH"
    exit 1
fi

if [ ! -f "$INPUT_FILE" ]; then
    echo -e "${RED}ERROR:${NC} No encuentro el input en: $INPUT_FILE"
    exit 1
fi

if [ ! -f "$EXPECTED_FILE" ]; then
    echo -e "${RED}ERROR:${NC} No encuentro el output esperado en: $EXPECTED_FILE"
    exit 1
fi

echo -e "${GREEN}--- SALIDA DEL PROGRAMA ---${NC}"

# Usamos 'tee' para mostrar el output al usuario y guardarlo a la vez en un archivo temporal
java -cp "$JAR_PATH" "$MAIN_CLASS" < "$INPUT_FILE" | tee "$TEMP_OUTPUT"

echo -e "${GREEN}--- FIN DEL PROGRAMA ---${NC}"

# Comparamos output generado con el esperado
echo ""
echo "Verificando resultados..."

# diff banderas:
# -w (ignora espacios), -B (ignora líneas vacías), --strip-trailing-cr (ignora formato Windows)
DIFF_RESULT=$(diff -w -B --strip-trailing-cr "$EXPECTED_FILE" "$TEMP_OUTPUT")

if [ -z "$DIFF_RESULT" ]; then
    # ÉXITO: No hay diferencias
    echo -e "${GREEN}RESULTADO: CORRECTO (El output del juego de prueba coincide perfectamente con el output esperado)${NC}"
    # Limpiamos el archivo temporal
    rm "$TEMP_OUTPUT"
else
    # FALLO: Hay diferencias
    echo -e "${RED}RESULTADO: FALLIDO (Hay diferencias)${NC}"
    echo "-----------------------------------------------------"
    echo "Izquierda (<): Esperado  |  Derecha (>): Obtenido"
    echo "-----------------------------------------------------"
    diff -w -B --strip-trailing-cr "$EXPECTED_FILE" "$TEMP_OUTPUT"
    echo "-----------------------------------------------------"
    echo "El archivo con tu resultado se ha guardado en: $TEMP_OUTPUT"

    # Si estamos en la llamada del script de la bateria de tests, nos salimos con código de error
    if [ "$1" == "auto" ]; then
        exit 1
    fi
fi

# Pausa al final, si no somos llamados por el script de la bateria de tests
if [ "$1" != "auto" ]; then
    echo ""
    echo "-----------------------------------------------------"
    read -p "Presiona ENTER para cerrar..."
fi