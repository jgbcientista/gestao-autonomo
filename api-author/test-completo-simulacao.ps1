#!/usr/bin/env pwsh

# Script de Teste Completo - Simulação de Todos os Endpoints
# Sistema de Autenticação com IA, Blockchain e Auditoria

Write-Host "=== TESTE COMPLETO - TODOS OS ENDPOINTS ===" -ForegroundColor Green
Write-Host "Data/Hora: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

$baseUrl = "http://localhost:8080"
$testCount = 0
$successCount = 0

function Test-Endpoint {
    param($Url, $Method = "GET", $Headers = @{}, $Body = $null, $Name)
    
    $global:testCount++
    Write-Host "[$global:testCount] $Name" -ForegroundColor Cyan
    
    try {
        $params = @{ Uri = $Url; Method = $Method; Headers = $Headers; ContentType = "application/json" }
        if ($Body) { $params.Body = $Body }
        
        $response = Invoke-RestMethod @params
        Write-Host "  ✅ SUCESSO" -ForegroundColor Green
        $global:successCount++
        return $response
    }
    catch {
        Write-Host "  ❌ ERRO: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Health Check
Write-Host "`n🏥 HEALTH CHECK" -ForegroundColor Yellow
Test-Endpoint -Url "$baseUrl/health" -Name "Health Check"

# Autenticação
Write-Host "`n🔐 AUTENTICAÇÃO" -ForegroundColor Yellow

$adminData = @{ nome = "Admin"; email = "admin@test.com"; senha = "Admin123"; roles = @("ADMIN") } | ConvertTo-Json
Test-Endpoint -Url "$baseUrl/auth/register" -Method "POST" -Body $adminData -Name "Registro Admin"

$adminLogin = @{ email = "admin@test.com"; password = "Admin123" } | ConvertTo-Json
$adminAuth = Test-Endpoint -Url "$baseUrl/auth/login" -Method "POST" -Body $adminLogin -Name "Login Admin"
$token = $adminAuth.token
$headers = @{ "Authorization" = "Bearer $token" }

# Blockchain
Write-Host "`n⛓️ BLOCKCHAIN" -ForegroundColor Yellow
Test-Endpoint -Url "$baseUrl/blockchain/auditoria/estatisticas" -Headers $headers -Name "Estatísticas Blockchain"
Test-Endpoint -Url "$baseUrl/blockchain/auditoria/hyperledger/info" -Headers $headers -Name "Info Hyperledger"

# Resultados
Write-Host "`n📊 RESULTADOS" -ForegroundColor Magenta
Write-Host "Total: $global:testCount | Sucessos: $global:successCount" -ForegroundColor White
$rate = [Math]::Round(($global:successCount / $global:testCount) * 100, 1)
Write-Host "Taxa de Sucesso: $rate%" -ForegroundColor Green

Write-Host "`n=== FINALIZADO ===" -ForegroundColor Green

Write-Host "`n📝 OBSERVAÇÕES:" -ForegroundColor White
Write-Host "  • Testes executados em ambiente de desenvolvimento" -ForegroundColor Gray
Write-Host "  • Hyperledger pode estar em modo simulação" -ForegroundColor Gray
Write-Host "  • Algumas falhas são esperadas em ambiente local" -ForegroundColor Gray
Write-Host "  • Para produção, configurar redes blockchain reais" -ForegroundColor Gray

Write-Host "`n=== TESTE COMPLETO FINALIZADO ===" -ForegroundColor Green
Write-Host "Data/Hora: $(Get-Date)" -ForegroundColor Gray 