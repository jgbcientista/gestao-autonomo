#!/usr/bin/env pwsh

Write-Host "=== INICIANDO SERVIDOR DE AUTENTICAÇÃO ===" -ForegroundColor Yellow

# Verificar se há processos Java rodando e terminar
Write-Host "`nVerificando processos Java..." -ForegroundColor Cyan
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "Terminando processos Java existentes..." -ForegroundColor Yellow
    $javaProcesses | Stop-Process -Force
    Start-Sleep -Seconds 3
}

# Verificar porta 8081
Write-Host "`nVerificando porta 8081..." -ForegroundColor Cyan
$port8081 = netstat -an | findstr :8081
if ($port8081) {
    Write-Host "Porta 8081 já está em uso" -ForegroundColor Red
    Write-Host "Execute: netstat -ano | findstr :8081 para ver qual processo está usando" -ForegroundColor Yellow
} else {
    Write-Host "Porta 8081 está livre" -ForegroundColor Green
}

# Compilar projeto
Write-Host "`nCompilando projeto..." -ForegroundColor Cyan
mvn clean compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erro na compilação!" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Compilação concluída com sucesso" -ForegroundColor Green

# Iniciar servidor
Write-Host "`nIniciando servidor Spring Boot..." -ForegroundColor Cyan
Write-Host "Profile: dev (H2 Database)" -ForegroundColor Green
Write-Host "Porta: 8081" -ForegroundColor Green
Write-Host "Aguarde aproximadamente 30-60 segundos..." -ForegroundColor Yellow

# Executar servidor
Write-Host "`nExecutando: mvn spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor Cyan
mvn spring-boot:run -Dspring.profiles.active=dev 