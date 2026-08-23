# Paper PDF Reader - Script de instalacion para PowerShell

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "  Paper PDF Reader - Instalador para Tablet USB" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $PSScriptRoot

Write-Host "[1/3] Verificando dispositivos Android conectados..." -ForegroundColor Yellow
adb devices

Write-Host ""
Write-Host "[2/3] Compilando e instalando APK Debug en la tablet..." -ForegroundColor Yellow
.\gradlew.bat installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "[3/3] Iniciando Paper en la tablet..." -ForegroundColor Yellow
    adb shell am start -n com.pablo.paper.debug/com.pablo.paper.MainActivity
    Write-Host ""
    Write-Host "=======================================================" -ForegroundColor Green
    Write-Host "  [EXITO] Paper ha sido instalado e iniciado en tu tablet." -ForegroundColor Green
    Write-Host "=======================================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[ERROR] Ocurrio un error al compilar o instalar." -ForegroundColor Red
}
