@echo off
REM Asegura que la terminal se ubique dentro de la carpeta EXE
cd /d "%~dp0"

echo Ejecutando programa con driver principal...
echo Pulsa ENTER para acceder al menu principal.
echo -----------------------------------------------------

java -cp program.jar drivers.Driver

