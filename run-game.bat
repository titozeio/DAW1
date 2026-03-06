@echo off
setlocal
cd /d %~dp0
if not exist "daw1\target\daw1-1.0-SNAPSHOT-all.jar" (
  echo No se encontro el ejecutable: daw1\target\daw1-1.0-SNAPSHOT-all.jar
  echo Ejecuta primero: mvn -q clean package -DskipTests -f .\daw1\pom.xml
  exit /b 1
)
java -jar "daw1\target\daw1-1.0-SNAPSHOT-all.jar"
