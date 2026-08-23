@echo off
setlocal enabledelayedexpansion

echo =======================================================
echo   Paper PDF Reader - Instalador para Tablet USB
echo =======================================================
echo.

cd /d "%~dp0"

echo [1/3] Verificando dispositivos Android conectados por USB...
adb devices
echo.

echo [2/3] Compilando e instalando Paper en el dispositivo...
call gradlew.bat installDebug
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] La compilacion o instalacion ha fallado.
    echo Asegurate de tener la depuracion USB activada en tu tablet y que este autorizada.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Iniciando Paper en la tablet...
adb shell am start -n com.pablo.paper.debug/com.pablo.paper.MainActivity

echo.
echo =======================================================
echo   [EXITO] Paper ha sido instalado e iniciado en tu tablet.
echo =======================================================
pause
