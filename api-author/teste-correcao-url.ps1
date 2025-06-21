#!/usr/bin/env pwsh

# Script de teste para verificar se a correção da URL duplicada funcionou
Write-Host "=== TESTE DE CORREÇÃO URL DUPLICADA ===" -ForegroundColor Green

Write-Host "`n1. Verificando se backend está rodando..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/status" -Method GET -ErrorAction Stop
    Write-Host "✓ Backend funcionando - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ Backend não está rodando. Inicie com:" -ForegroundColor Red
    Write-Host "  cd api-author" -ForegroundColor Yellow
    Write-Host "  ./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n2. Verificando se frontend está rodando..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:4200" -Method GET -ErrorAction Stop
    Write-Host "✓ Frontend funcionando - Status: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ Frontend não está rodando. Inicie com:" -ForegroundColor Red
    Write-Host "  cd frontend-author" -ForegroundColor Yellow
    Write-Host "  npm start" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n3. Testando URL correta (deve funcionar)..." -ForegroundColor Cyan
$userData = @{
    name = "Teste Correção URL"
    email = "teste_correcao_$(Get-Random -Maximum 1000)@teste.com"
    password = "senha123"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/autenticacao/registrar" -Method POST -Body $userData -ContentType "application/json" -ErrorAction Stop
    Write-Host "✓ URL correta funcionou - Status: $($response.StatusCode)" -ForegroundColor Green
    
    # Parse da resposta para verificar se token foi gerado
    try {
        $responseData = $response.Content | ConvertFrom-Json
        if ($responseData.token) {
            Write-Host "✓ Token JWT gerado com sucesso" -ForegroundColor Green
            Write-Host "✓ Nome: $($responseData.name)" -ForegroundColor Green
            Write-Host "✓ Email: $($responseData.email)" -ForegroundColor Green
        } else {
            Write-Host "⚠ Resposta sem token" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "⚠ Não foi possível fazer parse da resposta" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Erro na URL correta: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "Erro: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n4. Confirmando que URL duplicada ainda falha..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/api/v1/autenticacao/registrar" -Method POST -Body $userData -ContentType "application/json" -ErrorAction Stop
    Write-Host "✗ PROBLEMA: URL duplicada ainda funciona!" -ForegroundColor Red
} catch {
    Write-Host "✓ URL duplicada falha conforme esperado" -ForegroundColor Green
}

Write-Host "`n=== RESULTADO FINAL ===" -ForegroundColor Yellow
Write-Host "Se o teste 3 funcionou e o teste 4 falhou, a correção está funcionando!" -ForegroundColor White
Write-Host "Agora o frontend deve usar apenas a URL correta." -ForegroundColor White

Write-Host "`n=== PRÓXIMOS PASSOS ===" -ForegroundColor Cyan
Write-Host "1. Acesse: http://localhost:4200/register" -ForegroundColor White
Write-Host "2. Faça um registro via interface" -ForegroundColor White
Write-Host "3. Verifique o Network tab no DevTools" -ForegroundColor White
Write-Host "4. Confirme que a URL da requisição é a correta" -ForegroundColor White 