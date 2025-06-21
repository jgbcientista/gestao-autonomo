#!/usr/bin/env pwsh

# Script de diagnóstico para problema de URL duplicada
Write-Host "=== DIAGNÓSTICO URL DUPLICADA ===" -ForegroundColor Yellow

# Teste 1: Verificar se aplicação está rodando
Write-Host "`n1. Testando status da aplicação:" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET -ErrorAction Stop
    Write-Host "✓ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "✓ URL original funcionando corretamente" -ForegroundColor Green
} catch {
    Write-Host "✗ Erro na URL original: $($_.Exception.Message)" -ForegroundColor Red
}

# Teste 2: Verificar se URL duplicada realmente existe
Write-Host "`n2. Testando URL duplicada:" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/api/v1/autenticacao/status" -Method GET -ErrorAction Stop
    Write-Host "✗ URL duplicada também funciona (PROBLEMA CONFIRMADO)" -ForegroundColor Red
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Red
} catch {
    Write-Host "✓ URL duplicada NÃO funciona (comportamento esperado)" -ForegroundColor Green
    Write-Host "Erro: $($_.Exception.Response.StatusCode)" -ForegroundColor Green
}

# Teste 3: Testar registro com URL correta
Write-Host "`n3. Testando registro com URL correta:" -ForegroundColor Cyan
$userData = '{"name":"Teste Diagnóstico","email":"teste_diagnostico@teste.com","password":"senha123"}'
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -Body $userData -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ Registro funcionou com URL correta - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ Erro no registro com URL correta: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# Teste 4: Testar registro com URL duplicada 
Write-Host "`n4. Testando registro com URL duplicada:" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/api/v1/autenticacao/registrar" -Method POST -Body $userData -ContentType "application/json" -ErrorAction Stop
    Write-Host "✗ URL duplicada funciona (PROBLEMA NO BACKEND)" -ForegroundColor Red
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Red
} catch {
    Write-Host "✓ URL duplicada NÃO funciona (problema está no frontend)" -ForegroundColor Green
    Write-Host "Erro: $($_.Exception.Response.StatusCode)" -ForegroundColor Green
}

Write-Host "`n=== CONCLUSÃO ===" -ForegroundColor Yellow
Write-Host "Se ambas as URLs funcionarem, o problema está no servidor backend." -ForegroundColor White
Write-Host "Se apenas a URL correta funcionar, o problema está no frontend." -ForegroundColor White 