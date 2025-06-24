#!/usr/bin/env pwsh

# Script simples para executar testes Java
Write-Host "🧪 EXECUTANDO TESTES JAVA BLOCKCHAIN" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

# Navegar para o diretório da API
Set-Location "../api-author"

# Executar compilação
Write-Host "📦 Compilando projeto..." -ForegroundColor Cyan
mvn clean compile

# Executar aplicação em background
Write-Host "🚀 Iniciando aplicação..." -ForegroundColor Cyan
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden

# Aguardar aplicação iniciar
Write-Host "⏳ Aguardando aplicação iniciar..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Testar endpoints via curl
Write-Host "🔍 Testando endpoints..." -ForegroundColor Cyan

# Teste 1: Health check
Write-Host "Teste 1: Health check"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 10
    Write-Host "✅ Health check: OK" -ForegroundColor Green
} catch {
    Write-Host "❌ Health check: FALHA" -ForegroundColor Red
}

# Teste 2: Status da aplicação
Write-Host "Teste 2: Status da aplicação"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/status" -Method GET -TimeoutSec 10
    Write-Host "✅ Status: OK" -ForegroundColor Green
} catch {
    Write-Host "❌ Status: FALHA" -ForegroundColor Red
}

Write-Host "✅ TESTES CONCLUÍDOS" -ForegroundColor Green 