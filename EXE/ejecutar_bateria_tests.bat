@echo off
REM Situarse en la carpeta EXE (donde está este .bat)
cd /d "%~dp0"

echo =====================================================
echo       EJECUTANDO BATERIA DE TESTS AUTOMATICA
echo =====================================================
echo.

REM Lista de scripts de test (rutas relativas a EXE)
setlocal ENABLEDELAYEDEXPANSION

set TOTAL=0
set PASSED=0
set FAILED=0

set LOG_FILE=bateria_error_log.txt
if exist "%LOG_FILE%" del /f "%LOG_FILE%"

REM Definimos la lista de tests como indices numerados
set TEST_1=EjecutablesPorTest/EncuestasPreguntas/test_JuegoA1_CrearYListarEncuestaBasica.sh
set TEST_2=EjecutablesPorTest/EncuestasPreguntas/test_JuegoA2_CrearConPreguntasDeTodosLosTipos.sh
set TEST_3=EjecutablesPorTest/EncuestasPreguntas/test_JuegoA3_EditarEncuesta_AgregarYEliminarPregunta.sh
set TEST_4=EjecutablesPorTest/EncuestasPreguntas/test_JuegoA4_EliminarEncuestaYRespuestas.sh
set TEST_5=EjecutablesPorTest/Respuestas/test_JuegoB1_AgregarVariasRespuestasDistintosParticipantes.sh
set TEST_6=EjecutablesPorTest/Respuestas/test_JuegoB2_EliminarRespuestaDeParticipante.sh
set TEST_7=EjecutablesPorTest/Respuestas/test_JuegoB3_EliminarTodasLasRespuestas.sh
set TEST_8=EjecutablesPorTest/Respuestas/test_JuegoB4_ValidacionesDeOpcionesInvalidas.sh
set TEST_9=EjecutablesPorTest/ImportarExportar/test_JuegoC1_ImportarYExportarMismaEncuesta.sh
set TEST_10=EjecutablesPorTest/ImportarExportar/test_JuegoC2_ExportarEncuestaSinRespuestas.sh
set TEST_11=EjecutablesPorTest/ClusteringBasico/test_JuegoD1_ClusteringKMeansRandomConPocasRespuestas.sh
set TEST_12=EjecutablesPorTest/ClusteringBasico/test_JuegoD2_ClusteringKMeansPlusPlus.sh
set TEST_13=EjecutablesPorTest/ClusteringBasico/test_JuegoD3_ClusteringKMedoidsPAM.sh
set TEST_14=EjecutablesPorTest/ClusteringBasico/test_JuegoD4_SinRespuestasNoSePuedeVectorizar.sh
set TEST_15=EjecutablesPorTest/ClusteringBasico/test_JuegoD5_VectorizarCuestionarioConTiposMixtos.sh
set TEST_16=EjecutablesPorTest/ClusteringAvanzado/test_JuegoE1_VerUltimoResultadoYExportarClustering.sh
set TEST_17=EjecutablesPorTest/ClusteringAvanzado/test_JuegoE2_CalcularAccuracyLoanTrain.sh
set TEST_18=EjecutablesPorTest/ClusteringAvanzado/test_JuegoE3_AccuracyConKIncorrectoDebeFallar.sh
set TEST_19=EjecutablesPorTest/ClusteringAvanzado/test_JuegoE4_ClusteringKMeansYKMedoidsComparacion.sh
set TEST_20=EjecutablesPorTest/ComparaciondeFicheros/test_JuegoF1_CompararFicherosIguales.sh
set TEST_21=EjecutablesPorTest/ComparaciondeFicheros/test_JuegoF2_CompararFicherosConDiferencias.sh
set TEST_22=EjecutablesPorTest/FlujosIntegrados/test_JuegoG1_FlujoCompleto_CrearEditarAnadir_ClusteringExportarComparar.sh
set TEST_23=EjecutablesPorTest/FlujosIntegrados/test_JuegoG2_FlujoImportar_AnadirRespuestas_DosAlgoritmos_Accuracy.sh
set TEST_24=EjecutablesPorTest/FlujosIntegrados/test_JuegoG3_MultiplesEncuestasConDiferentesRespuestas.sh

set COUNT=1
:loop_tests
if %COUNT% GTR 24 goto end_loop

set CURRENT_TEST=!TEST_%COUNT%!
if not exist "!CURRENT_TEST!" (
    echo [ERROR] No se encuentra el script: !CURRENT_TEST!
    echo Revisa la lista en ejecutar_bateria_tests.bat
    set /a FAILED+=1
    set /a TOTAL+=1
    set /a COUNT+=1
    goto loop_tests
)

set /a TOTAL+=1
for %%F in (!CURRENT_TEST!) do set TEST_NAME=%%~nxF

echo Ejecutando !TEST_NAME! ...

REM Ejecutamos el script .sh con bash y redirigimos salida al log
bash "!CURRENT_TEST!" auto >> "%LOG_FILE%" 2>&1

if errorlevel 1 (
    echo [ FALLO ]
    set /a FAILED+=1
    echo ----------------------------------- >> "%LOG_FILE%"
    echo FALLO EN: !TEST_NAME! >> "%LOG_FILE%"
    echo ----------------------------------- >> "%LOG_FILE%"
) else (
    echo [ OK ]
    set /a PASSED+=1
)

set /a COUNT+=1
goto loop_tests

:end_loop

echo.
echo =====================================================
echo RESUMEN FINAL:
echo Total Tests: %TOTAL%
echo Pasados:     %PASSED%

if %FAILED% GTR 0 (
    echo Fallados:    %FAILED%
    echo.
    echo Se han encontrado errores.
    echo Revisa el archivo "%LOG_FILE%" para ver los detalles de los fallos.
) else (
    if %TOTAL% GTR 0 (
        echo TODOS LOS TESTS HAN PASADO CORRECTAMENTE.
        del /f "%LOG_FILE%" 2>nul
    ) else (
        echo No se ha ejecutado ningun test. Revisa la lista.
    )
)

echo =====================================================

echo.
pause

endlocal

