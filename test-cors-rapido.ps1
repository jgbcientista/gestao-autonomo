#!/usr/bin/env pwsh

Write-Host "🔧 TESTE RÁPIDO DE CORS" -ForegroundColor Cyan

# Aguardar servidor inicializar
Start-Sleep -Seconds 15

# Testar se servidor está rodando
Write-Host "`n1. Verificando servidor..." -ForegroundColor Yellow
try {
    $status = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET -TimeoutSec 5
    Write-Host "✅ Servidor OK: $status" -ForegroundColor Green
} catch {
    Write-Host "❌ Servidor não está rodando!" -ForegroundColor Red
    exit 1
}

# Testar CORS com preflight request
Write-Host "`n2. Testando CORS..." -ForegroundColor Yellow
try {
    $headers = @{
        'Origin' = 'http://localhost:4200'
        'Access-Control-Request-Method' = 'POST'
        'Access-Control-Request-Headers' = 'Content-Type'
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method OPTIONS -Headers $headers -TimeoutSec 5
    
    $corsOrigin = $response.Headers['Access-Control-Allow-Origin']
    $corsCredentials = $response.Headers['Access-Control-Allow-Credentials']
    
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor White
    Write-Host "Access-Control-Allow-Origin: $corsOrigin" -ForegroundColor White
    Write-Host "Access-Control-Allow-Credentials: $corsCredentials" -ForegroundColor White
    
    if ($corsOrigin -eq 'http://localhost:4200' -or $corsOrigin -contains 'http://localhost:4200') {
        Write-Host "✅ CORS configurado corretamente!" -ForegroundColor Green
    } else {
        Write-Host "❌ CORS não configurado para localhost:4200" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erro no teste CORS: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
}

# Testar registro simples
Write-Host "`n3. Testando registro..." -ForegroundColor Yellow

$userData = @{
    name = "Teste CORS"
    email = "testecors@example.com"
    password = "123456"
} | ConvertTo-Json

$headers = @{
    'Content-Type' = 'application/json'
    'Origin' = 'http://localhost:4200'
}

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -Body $userData -Headers $headers -TimeoutSec 10
    
    if ($response.token) {
        Write-Host "✅ Registro funcionando! Token: $($response.token.Substring(0, 20))..." -ForegroundColor Green
    } else {
        Write-Host "❌ Token vazio!" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erro no registro: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Resposta: $responseBody" -ForegroundColor Yellow
    }
}

Write-Host "`n🎯 AGORA TESTE NO NAVEGADOR:" -ForegroundColor Cyan
Write-Host "1. Acesse: http://localhost:4200" -ForegroundColor White
Write-Host "2. Preencha o formulário de registro" -ForegroundColor White
Write-Host "3. Verifique o console (F12)" -ForegroundColor White 