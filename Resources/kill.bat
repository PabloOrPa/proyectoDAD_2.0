@echo off

:: Comprobar si el script se está ejecutando como administrador
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Ejecutando como administrador
) else (
    echo No se tienen privilegios de administrador, intentando ejecutar como administrador...
    powershell -Command "Start-Process cmd -ArgumentList '/c %~dpnx0' -Verb runAs"
    exit /b
)

:: Detener Node.js
echo Deteniendo servidor Node.js...
taskkill /f /im node.exe

:: Detener Vert.x
echo Deteniendo Vert.x...
taskkill /f /im java.exe

:: Detener Mosquitto
echo Deteniendo Mosquitto...
taskkill /f /im mosquitto.exe

echo Todos los servicios han sido detenidos.
pause
