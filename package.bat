@echo off
setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0"
set "PROJECT_POM=%ROOT_DIR%daw1\pom.xml"
set "TARGET_DIR=%ROOT_DIR%daw1\target"
set "OUTPUT_DIR=%ROOT_DIR%package\DAW1"
set "JAR_FILE="

echo.
echo [1/4] Compilando el proyecto...
call mvn clean package -DskipTests -f "%PROJECT_POM%"
if errorlevel 1 (
    echo.
    echo Error: fallo al generar el paquete Maven.
    exit /b 1
)

echo.
echo [2/4] Buscando el jar ejecutable...
for %%F in ("%TARGET_DIR%\*-all.jar") do (
    set "JAR_FILE=%%~fF"
    goto :jar_found
)

echo.
echo Error: no se encontro ningun archivo *-all.jar en "%TARGET_DIR%".
exit /b 1

:jar_found
echo Encontrado: !JAR_FILE!

echo.
echo [3/4] Preparando carpeta para compartir...
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
mkdir "%OUTPUT_DIR%"
copy /y "!JAR_FILE!" "%OUTPUT_DIR%\daw1.jar" >nul

(
    echo @echo off
    echo cd /d "%%~dp0"
    echo java -jar daw1.jar
    echo if errorlevel 1 pause
) > "%OUTPUT_DIR%\run.bat"

(
    echo DAW1 - paquete para pruebas
    echo.
    echo Requisitos:
    echo - Tener Java 21 o superior instalado.
    echo.
    echo Para ejecutar:
    echo - Haz doble click en run.bat
    echo - O abre una terminal en esta carpeta y ejecuta: java -jar daw1.jar
) > "%OUTPUT_DIR%\README.txt"

echo.
echo [4/4] Paquete generado correctamente.
echo Carpeta lista para compartir:
echo %OUTPUT_DIR%
echo.
echo Puedes comprimir esa carpeta en ZIP y enviarla.

endlocal
