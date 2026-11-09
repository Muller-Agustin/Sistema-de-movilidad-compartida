@echo off
chcp 65001 >nul
if not exist out mkdir out
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
if errorlevel 1 (
    del fuentes.txt
    echo.
    echo Hubo errores de compilacion.
    pause
    exit /b
)
del fuentes.txt
java -cp out VistaMain.Main