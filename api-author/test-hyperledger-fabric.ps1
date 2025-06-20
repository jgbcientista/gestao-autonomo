#!/usr/bin/env pwsh

# Script para testar conectividade e funcionalidades do Hyperledger Fabric
Write-Host "=== TESTE HYPERLEDGER FABRIC ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# Função para requisições HTTP
function Invoke-ApiRequest {
    param($Url, $Method = "GET", $Headers = @{}, $Body = $null)
    try {
        $params = @{ Uri = $Url; Method = $Method; Headers = $Headers; ContentType = "application/json" }
        if ($Body) { $params.Body = $Body }
        return Invoke-RestMethod @params
    }
    catch {
        Write-Host "Erro: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Obter token de autenticação
$loginData = @{ email = "admin@auth.com"; password = "admin123" } | ConvertTo-Json
$response = Invoke-ApiRequest -Url "$baseUrl/auth/login" -Method "POST" -Body $loginData
$token = $response.token
$headers = @{ "Authorization" = "Bearer $token" }

# Teste 1: Informações da rede
Write-Host "1. Informações da rede Hyperledger..." -ForegroundColor Cyan
$networkInfo = Invoke-ApiRequest -Url "$baseUrl/blockchain/auditoria/hyperledger/info" -Headers $headers
if ($networkInfo) {
    $networkInfo | ConvertTo-Json -Depth 3 | Write-Host
}

# Teste 2: Conectividade
Write-Host "2. Teste de conectividade..." -ForegroundColor Cyan
$connectivity = Invoke-ApiRequest -Url "$baseUrl/blockchain/auditoria/hyperledger/test-connectivity" -Method "POST" -Headers $headers
if ($connectivity) {
    Write-Host "Conectado: $($connectivity.conectado)" -ForegroundColor $(if($connectivity.conectado) {"Green"} else {"Red"})
}

# Teste 3: Estatísticas
Write-Host "3. Estatísticas da blockchain..." -ForegroundColor Cyan
$stats = Invoke-ApiRequest -Url "$baseUrl/blockchain/auditoria/estatisticas" -Headers $headers
if ($stats) {
    Write-Host "Total transações: $($stats.totalTransacoes)" -ForegroundColor White
    Write-Host "Hyperledger: $($stats.transacoesHyperledger)" -ForegroundColor White
}

Write-Host "Teste concluído!" -ForegroundColor Green 