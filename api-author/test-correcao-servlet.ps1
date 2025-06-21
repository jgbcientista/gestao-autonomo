#!/usr/bin/env pwsh

Write-Host "=== Teste de Correção do Erro de Configuração Servlet ===" -ForegroundColor Cyan
Write-Host ""

# Função para testar endpoint
function Test-Endpoint {
    param(
        [string]$Url,
        [string]$Description
    )
    
    Write-Host "Testando: $Description" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method GET -TimeoutSec 10
        Write-Host "✅ Sucesso: $response" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Aguardar a aplicação inicializar
Write-Host "Aguardando aplicação inicializar..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Testar endpoints
$statusTest = Test-Endpoint -Url "http://localhost:8081/api/v1/autenticacao/status" -Description "Endpoint de Status"

Write-Host ""
Write-Host "=== Resultados ===" -ForegroundColor Cyan

if ($statusTest) {
    Write-Host "✅ Correção do erro de configuração servlet foi SUCESSO!" -ForegroundColor Green
    Write-Host "✅ Aplicação Spring Boot está funcionando corretamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "URLs disponíveis:" -ForegroundColor White
    Write-Host "- Status: http://localhost:8081/api/v1/autenticacao/status" -ForegroundColor Gray
    Write-Host "- Swagger: http://localhost:8081/swagger-ui.html" -ForegroundColor Gray
    Write-Host "- API Docs: http://localhost:8081/api-docs" -ForegroundColor Gray
} else {
    Write-Host "❌ Ainda há problemas com a configuração" -ForegroundColor Red
    Write-Host "Verifique os logs da aplicação para mais detalhes" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Teste concluído." -ForegroundColor Cyan 