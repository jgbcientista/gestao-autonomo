#!/usr/bin/env pwsh

# Script de teste para registro de usuário
# Teste básico de persistência

Write-Host "=== TESTE DE REGISTRO DE USUÁRIO ===" -ForegroundColor Green

# Verifica se a aplicação está rodando
try {
    $statusResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET -ErrorAction Stop
    Write-Host "✓ Aplicação está rodando: $($statusResponse.Content)" -ForegroundColor Green
} catch {
    Write-Host "✗ Aplicação não está rodando. Inicie com: ./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor Red
    exit 1
}

# Dados do usuário de teste
$userData = @{
    name = "Usuário Teste"
    email = "teste$(Get-Random -Maximum 1000)@teste.com"
    password = "senha123"
} | ConvertTo-Json

Write-Host "Testando registro com dados: $userData" -ForegroundColor Yellow

# Testa o registro
try {
    $registerResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -Body $userData -ContentType "application/json" -ErrorAction Stop
    
    Write-Host "✓ Registro realizado com sucesso!" -ForegroundColor Green
    Write-Host "Status Code: $($registerResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($registerResponse.Content)" -ForegroundColor Green
    
    # Tenta fazer parse da resposta JSON
    try {
        $responseData = $registerResponse.Content | ConvertFrom-Json
        Write-Host "Token gerado: $($responseData.token -ne $null)" -ForegroundColor Green
        Write-Host "Nome: $($responseData.name)" -ForegroundColor Green
        Write-Host "Email: $($responseData.email)" -ForegroundColor Green
    } catch {
        Write-Host "Resposta não é um JSON válido" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "✗ Erro no registro:" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "Erro: $($_.Exception.Message)" -ForegroundColor Red
    
    # Tenta ler o corpo da resposta de erro
    try {
        $errorStream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorStream)
        $errorResponse = $reader.ReadToEnd()
        Write-Host "Resposta de erro: $errorResponse" -ForegroundColor Red
    } catch {
        Write-Host "Não foi possível ler a resposta de erro" -ForegroundColor Red
    }
}

Write-Host "=== FIM DO TESTE ===" -ForegroundColor Green 