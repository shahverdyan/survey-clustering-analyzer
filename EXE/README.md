Guia rapida para ejecutar el proyecto desde terminal


Dar permisos de ejecucion a los scripts .sh:
- Desde /EXE, ejecutar: find . -name "*.sh" -exec chmod +x {} +

Para recompilar el proyecto, (por si se modifica codigo en src), y que el ejecutable program.jar se actualice:
- Desde /EXE, ejecutar: ./recompilar.sh

Para ejecutar el proyecto con la capa de presentacion:
- Desde /EXE, ejecutar: ./ejecutar_app.sh

Por si es necesario ejecutar el programa con el driver interactivo de la terminal:
- Desde /EXE, ejecutar: ./ejecutar_driver_principal.sh

Para ejecutar los tests unitarios:
- Desde /EXE, ejecutar: ./ejecutar_tests_unitarios.sh

Para ejecutar un juego de prueba especifico, por ejemplo, el test A1:
- Desde /EXE, ejecutar:
     cd EjecutablesPorTest/EncuestasPreguntas
     ./Test_JuegoA1_CrearYListarEncuestaBasica.sh

Los tests estan ordenados por carpetas segun la funcionalidad que prueban, navega por las carpetas para encontrar el test que quieras ejecutar.

Desde cualquier IDE puedes hacer click derecho en el cualquiera de los .sh y ejecutarlo directamente.

Si usas windows, no hace falta dar permisos, y simplemnte ejecuta los .bat equivalentes