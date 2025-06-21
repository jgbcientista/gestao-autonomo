#!/usr/bin/env pwsh

Write-Host "=== TESTE LOGIN JOÃO GUEDES ===" -ForegroundColor Yellow

# Dados de teste fornecidos pelo usuário
$email = "joaoguedes@gmail.com"
$password = "1234567890"

Write-Host "`nDados do teste:" -ForegroundColor Cyan
Write-Host "Email: $email" -ForegroundColor White
Write-Host "Password: $password" -ForegroundColor White

# 1. Verificar se servidor está rodando
Write-Host "`n1. Verificando se servidor está online..." -ForegroundColor Cyan

try {
    $statusResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET -TimeoutSec 5
    Write-Host "✓ Servidor online: $statusResponse" -ForegroundColor Green
} catch {
    Write-Host "✗ Servidor OFFLINE ou não respondendo" -ForegroundColor Red
    Write-Host "Erro: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "`nPara iniciar o servidor execute:" -ForegroundColor Yellow
    Write-Host "  .\iniciar-servidor.ps1" -ForegroundColor Cyan
    exit 1
}

# 2. Registrar usuário (caso não exista)
Write-Host "`n2. Registrando usuário (caso não exista)..." -ForegroundColor Cyan

$registerData = @{
    name = "João Guedes"
    email = $email
    password = $password
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -Body $registerData -ContentType "application/json" -TimeoutSec 10
    Write-Host "✓ Usuário registrado com sucesso!" -ForegroundColor Green
    Write-Host "Token: $($registerResponse.token.Substring(0,30))..." -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 409) {
        Write-Host "✓ Usuário já existe - continuando com login..." -ForegroundColor Yellow
    } else {
        Write-Host "⚠ Problema no registro: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
        Write-Host "Continuando mesmo assim..." -ForegroundColor White
    }
}

# 3. TESTAR LOGIN - URL CORRETA
Write-Host "`n3. Testando LOGIN com URL CORRETA..." -ForegroundColor Cyan
Write-Host "URL: http://localhost:8081/api/v1/autenticacao/entrar" -ForegroundColor White

$loginData = @{
    email = $email
    password = $password
    location = "São Paulo, BR"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/autenticacao/entrar" -Method POST -Body $loginData -ContentType "application/json" -TimeoutSec 10
    
    Write-Host "🎉 LOGIN REALIZADO COM SUCESSO!" -ForegroundColor Green
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
    Write-Host "Token: $($loginResponse.token.Substring(0,40))..." -ForegroundColor Green
    Write-Host "Nome: $($loginResponse.name)" -ForegroundColor Green
    Write-Host "Email: $($loginResponse.email)" -ForegroundColor Green
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
    
} catch {
    Write-Host "✗ ERRO NO LOGIN!" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Erro: $($_.Exception.Message)" -ForegroundColor Red
    
    # Tentar obter mais detalhes do erro
    if ($_.Exception.Response) {
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $responseBody = $reader.ReadToEnd()
            Write-Host "Detalhes: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Não foi possível obter detalhes do erro" -ForegroundColor Yellow
        }
    }
}

# 4. TESTAR URL INCORRETA (deve falhar)
Write-Host "`n4. Testando URL INCORRETA (duplicada)..." -ForegroundColor Cyan
Write-Host "URL: http://localhost:8081/api/v1/api/v1/autenticacao/entrar" -ForegroundColor Red

try {
    $wrongResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/api/v1/autenticacao/entrar" -Method POST -Body $loginData -ContentType "application/json" -TimeoutSec 10
    Write-Host "✗ URL duplicada funcionou (PROBLEMA DE CONFIGURAÇÃO!)" -ForegroundColor Red
} catch {
    Write-Host "✓ URL duplicada falhou corretamente (404)" -ForegroundColor Green
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Green
}

Write-Host "`n=== RESUMO ===" -ForegroundColor Yellow
Write-Host "✅ Use sempre: http://localhost:8081/api/v1/autenticacao/entrar" -ForegroundColor Green
Write-Host "❌ NÃO use: http://localhost:8081/api/v1/api/v1/autenticacao/entrar" -ForegroundColor Red
Write-Host "`nSe o login não funcionou, verifique se o usuário foi registrado corretamente." -ForegroundColor White 