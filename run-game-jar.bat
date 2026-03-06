@echo off
setlocal
cd /d %~dp0

set JAR_PATH=daw1\target\daw1-1.0-SNAPSHOT-all.jar

if not exist "%JAR_PATH%" (
  echo No se encontro %JAR_PATH%
  echo Compila primero con: mvn -q package -DskipTests -f .\daw1\pom.xml
  exit /b 1
)

java --enable-native-access=ALL-UNNAMED -jar "%JAR_PATH%"
