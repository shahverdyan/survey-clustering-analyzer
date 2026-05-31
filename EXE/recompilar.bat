@echo off
REM Asegura que la terminal se ubique dentro de la carpeta EXE
cd /d "%~dp0"

REM Nos salimos de EXE y nos situamos en la raiz del proyecto
cd /d ".."

echo Recompilando proyecto...

REM Compilamos el proyecto haciendo un clean y generando el jar
call gradlew clean jar

REM Copiamos el .jar generado en la carpeta EXE como program.jar
for %%F in (build\libs\*.jar) do (
    copy /Y "%%F" "EXE\program.jar"
)

echo.
echo ¡Hecho! Nuevo program.jar copiado a EXE.

