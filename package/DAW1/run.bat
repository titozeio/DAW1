@echo off
cd /d "%~dp0"
java -jar daw1.jar
if errorlevel 1 pause
