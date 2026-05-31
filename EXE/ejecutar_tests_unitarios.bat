@echo off
REM Asegura que la terminal se ubique dentro de la carpeta EXE
cd /d "%~dp0"

REM Nos salimos de EXE y nos situamos en la raiz del proyecto
cd /d ".."

echo -----------------------------------------------------
echo Ejecutando tests unitarios...
echo -----------------------------------------------------

REM Ejecutamos Gradle Wrapper para limpiar y lanzar los tests
call gradlew clean test

echo.
echo Tests unitarios finalizados.

