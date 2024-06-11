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

:: Iniciar Mosquitto
echo Iniciando Mosquitto...
net start mosquitto

:: Esperar un momento para asegurar que Mosquitto esté completamente iniciado
timeout /t 2

:: Iniciar Vert.x
echo Iniciando Vert.x...
start "" "java" -jar "C:\Users\Pablo\Documents\GitHub\proyectoDAD_2.0\Proyecto Final\target\DomoticaHUB-1.0.jar"

:: Esperar un momento para asegurar que Vert.x esté completamente iniciado
timeout /t 5

:: Iniciar Node.js
echo Iniciando servidor Node.js...
start "" cmd /c "cd /d C:\Users\Pablo\Documents\GitHub\proyectoDAD_2.0\Proyecto_Final_FrontEnd\frontEndDADServer && npm start"

echo Todos los servicios han sido iniciados.
timeout /t 3
