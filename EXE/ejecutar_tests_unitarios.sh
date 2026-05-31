#!/bin/bash

# Asegura que la terminal se ubique dentro de la carpeta EXE
cd "$(dirname "$0")"

# Nos salimos de EXE y nos situamos en la raíz del proyecto
cd ..

# Damos permisos de ejecución al gradlew (por si se perdieron al copiar archivos)
chmod +x gradlew

echo "-----------------------------------------------------"
echo "Ejecutando tests unitarios..."
echo "-----------------------------------------------------"

./gradlew clean test

echo ""
echo "Tests unitarios finalizados."