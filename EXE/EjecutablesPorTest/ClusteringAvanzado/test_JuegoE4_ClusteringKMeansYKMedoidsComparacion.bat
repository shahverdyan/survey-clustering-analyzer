@echo off
REM Test: Juego E4 (Clustering KMeans y KMedoids comparacion)
pushd "%~dp0"

set "JAR_PATH=..\..\program.jar"
set "INPUT_FILE=..\..\JuegosDePrueba\ClusteringAvanzado\JuegoE4_ClusteringKMeansYKMedoidsComparacion.txt"
set "EXPECTED_FILE=..\..\JuegosDePrueba\ClusteringAvanzado\output_esperado_E4.txt"
set "TEMP_OUTPUT=resultado_actual.tmp"
set "MAIN_CLASS=drivers.Driver"

echo -----------------------------------------------------
echo Ejecutando Test: Juego E4 (Clustering KMeans y KMedoids comparacion)
echo Input: %INPUT_FILE%
echo -----------------------------------------------------

if not exist "%JAR_PATH%" (
  echo ERROR: No encuentro el archivo program.jar en: %JAR_PATH%
  popd
  exit /b 1
)
if not exist "%INPUT_FILE%" (
  echo ERROR: No encuentro el input en: %INPUT_FILE%
  popd
  exit /b 1
)
if not exist "%EXPECTED_FILE%" (
  echo ERROR: No encuentro el output esperado en: %EXPECTED_FILE%
  popd
  exit /b 1
)

echo --- SALIDA DEL PROGRAMA ---
java -cp "%JAR_PATH%" %MAIN_CLASS% < "%INPUT_FILE%" > "%TEMP_OUTPUT%"
type "%TEMP_OUTPUT%"
echo --- FIN DEL PROGRAMA ---
echo.
echo Verificando resultados...

REM fc /W ignores extra white space
fc /W "%EXPECTED_FILE%" "%TEMP_OUTPUT%" >nul
if %ERRORLEVEL% EQU 0 (
  echo RESULTADO: CORRECTO
  del "%TEMP_OUTPUT%"
) ELSE IF %ERRORLEVEL% EQU 1 (
  echo RESULTADO: FALLIDO (Hay diferencias)
  echo -----------------------------------------------------
  fc "%EXPECTED_FILE%" "%TEMP_OUTPUT%"
  echo -----------------------------------------------------
  echo El archivo con tu resultado se ha guardado en: %TEMP_OUTPUT%
  if "%1"=="auto" exit /b 1
) ELSE (
  echo ERROR comparando archivos
)

if not "%1"=="auto" pause
popd
exit /b 0
