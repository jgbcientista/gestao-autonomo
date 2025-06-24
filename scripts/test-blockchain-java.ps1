#!/usr/bin/env pwsh

<#
🧪 SCRIPT DE TESTES BLOCKCHAIN EM JAVA
======================================
Executa todos os testes da integração Hyperledger Fabric
usando Java e APIs REST
#>

Write-Host "🚀 INICIANDO TESTES BLOCKCHAIN EM JAVA" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green

# Configurações
$API_BASE = "http://localhost:8080/api"
$BLOCKCHAIN_API = "$API_BASE/blockchain/test"
$TIMEOUT = 30

# Função para fazer requisições HTTP
function Invoke-APITest {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Body = @{},
        [string]$TestName
    )
    
    try {
        Write-Host "🔍 Testando: $TestName" -ForegroundColor Cyan
        
        $headers = @{
            "Content-Type" = "application/json"
            "Accept" = "application/json"
        }
        
        if ($Method -eq "POST" -and $Body.Count -gt 0) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $jsonBody -Headers $headers -TimeoutSec $TIMEOUT
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -TimeoutSec $TIMEOUT
        }
        
        if ($response.success -eq $true) {
            Write-Host "✅ ${TestName} SUCESSO" -ForegroundColor Green
            return $response
        } else {
            Write-Host "❌ ${TestName} FALHA" -ForegroundColor Red
            Write-Host "   Erro: $($response.error)" -ForegroundColor Yellow
            return $null
        }
        
    } catch {
        Write-Host "❌ $TestName ERRO" -ForegroundColor Red
        Write-Host "   Exceção: $($_.Exception.Message)" -ForegroundColor Yellow
        return $null
    }
}

# Função para aguardar a aplicação iniciar
function Wait-ForApplication {
    Write-Host "⏳ Aguardando aplicação iniciar..." -ForegroundColor Yellow
    
    $maxAttempts = 30
    $attempt = 0
    
    do {
        try {
            $response = Invoke-RestMethod -Uri "$API_BASE/health" -Method GET -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response) {
                Write-Host "✅ Aplicação iniciada!" -ForegroundColor Green
                return $true
            }
        } catch {
            # Ignorar erro e tentar novamente
        }
        
        $attempt++
        Start-Sleep -Seconds 2
        Write-Host "   Tentativa $attempt/$maxAttempts..." -ForegroundColor Gray
        
    } while ($attempt -lt $maxAttempts)
    
    Write-Host "❌ Timeout: Aplicação não iniciou" -ForegroundColor Red
    return $false
}

# Função principal de testes
function Run-BlockchainTests {
    Write-Host "`n📋 EXECUTANDO BATERIA DE TESTES" -ForegroundColor Magenta
    Write-Host "================================" -ForegroundColor Magenta
    
    $testResults = @{}
    
    # Teste 1: Verificar conexão
    Write-Host "`n🔍 TESTE 1: CONEXÃO COM BLOCKCHAIN" -ForegroundColor Blue
    $connectionResult = Invoke-APITest -Url "$BLOCKCHAIN_API/connection" -TestName "Conexão Hyperledger Fabric"
    $testResults["connection"] = $connectionResult -ne $null
    
    if ($connectionResult) {
        Write-Host "   Modo: $($connectionResult.mode)" -ForegroundColor Gray
        Write-Host "   Conectado: $($connectionResult.connected)" -ForegroundColor Gray
    }
    
    # Teste 2: Status da rede
    Write-Host "`n📊 TESTE 2: STATUS DA REDE" -ForegroundColor Blue
    $statusResult = Invoke-APITest -Url "$BLOCKCHAIN_API/status" -TestName "Status da Rede"
    $testResults["status"] = $statusResult -ne $null
    
    if ($statusResult) {
        $status = $statusResult.status
        Write-Host "   Rede Online: $($status.networkOnline)" -ForegroundColor Gray
        Write-Host "   Modo Simulação: $($status.simulationMode)" -ForegroundColor Gray
        Write-Host "   Total Eventos: $($status.totalEvents)" -ForegroundColor Gray
    }
    
    # Teste 3: Registro de evento
    Write-Host "`n📝 TESTE 3: REGISTRO DE EVENTO" -ForegroundColor Blue
    $eventData = @{
        userId = "test_java_user_$(Get-Date -Format 'yyyyMMddHHmmss')"
        action = "java_api_test"
        status = "success"
        details = "Teste automatizado via PowerShell e Java API"
        timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    }
    
    $eventResult = Invoke-APITest -Url "$BLOCKCHAIN_API/event" -Method "POST" -Body $eventData -TestName "Registro de Evento"
    $testResults["event"] = $eventResult -ne $null
    
    if ($eventResult) {
        Write-Host "   Transaction ID: $($eventResult.transactionId)" -ForegroundColor Gray
        Write-Host "   Data Hash: $($eventResult.dataHash)" -ForegroundColor Gray
    }
    
    # Teste 4: Estatísticas
    Write-Host "`n📊 TESTE 4: ESTATÍSTICAS DA REDE" -ForegroundColor Blue
    $statsResult = Invoke-APITest -Url "$BLOCKCHAIN_API/statistics" -TestName "Estatísticas da Rede"
    $testResults["statistics"] = $statsResult -ne $null
    
    if ($statsResult) {
        $stats = $statsResult.statistics
        Write-Host "   Total Eventos: $($stats.totalEvents)" -ForegroundColor Gray
        Write-Host "   Eventos Bem-sucedidos: $($stats.successfulEvents)" -ForegroundColor Gray
        Write-Host "   Taxa de Sucesso: $($stats.successRate)%" -ForegroundColor Gray
    }
    
    # Teste 5: Relatório detalhado
    Write-Host "`n📋 TESTE 5: RELATÓRIO DETALHADO" -ForegroundColor Blue
    $reportResult = Invoke-APITest -Url "$BLOCKCHAIN_API/report" -TestName "Relatório Detalhado"
    $testResults["report"] = $reportResult -ne $null
    
    if ($reportResult) {
        Write-Host "   Relatório gerado com sucesso" -ForegroundColor Gray
        # Salvar relatório em arquivo
        $reportPath = "blockchain-test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
        $reportResult.report | Out-File -FilePath $reportPath -Encoding UTF8
        Write-Host "   Relatório salvo em: $reportPath" -ForegroundColor Gray
    }
    
    return $testResults
}

# Função para gerar relatório final
function Generate-FinalReport {
    param([hashtable]$TestResults)
    
    Write-Host "`n📊 RELATÓRIO FINAL DOS TESTES" -ForegroundColor Magenta
    Write-Host "==============================" -ForegroundColor Magenta
    
    $totalTests = $TestResults.Count
    $passedTests = ($TestResults.Values | Where-Object { $_ -eq $true }).Count
    $failedTests = $totalTests - $passedTests
    $successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 2) } else { 0 }
    
    Write-Host "Data/Hora: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor Gray
    Write-Host "Total de Testes: $totalTests" -ForegroundColor White
    Write-Host "Testes Aprovados: $passedTests" -ForegroundColor Green
    Write-Host "Testes Falhados: $failedTests" -ForegroundColor Red
    Write-Host "Taxa de Sucesso: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 60) { "Yellow" } else { "Red" })
    
    Write-Host "`nDetalhes dos Testes:" -ForegroundColor White
    foreach ($test in $TestResults.GetEnumerator()) {
        $status = if ($test.Value) { "✅ PASSOU" } else { "❌ FALHOU" }
        $color = if ($test.Value) { "Green" } else { "Red" }
        Write-Host "  $($test.Key): $status" -ForegroundColor $color
    }
    
    # Salvar relatório final
    $finalReportPath = "blockchain-java-test-summary-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
    $reportContent = @"
RELATÓRIO FINAL DOS TESTES BLOCKCHAIN JAVA
==========================================
Data/Hora: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')
Total de Testes: $totalTests
Testes Aprovados: $passedTests
Testes Falhados: $failedTests
Taxa de Sucesso: $successRate%

Detalhes dos Testes:
$($TestResults.GetEnumerator() | ForEach-Object { "  $($_.Key): $(if ($_.Value) { 'PASSOU' } else { 'FALHOU' })" } | Out-String)

Status Geral: $(if ($successRate -ge 80) { 'SUCESSO' } elseif ($successRate -ge 60) { 'PARCIAL' } else { 'FALHA' })
"@
    
    $reportContent | Out-File -FilePath $finalReportPath -Encoding UTF8
    Write-Host "`nRelatório final salvo em: $finalReportPath" -ForegroundColor Cyan
    
    return $successRate -ge 80
}

# EXECUÇÃO PRINCIPAL
try {
    # Verificar se a aplicação está rodando
    if (-not (Wait-ForApplication)) {
        Write-Host "❌ Aplicação não está disponível. Inicie com: mvn spring-boot:run" -ForegroundColor Red
        exit 1
    }
    
    # Executar testes
    $results = Run-BlockchainTests
    
    # Gerar relatório final
    $success = Generate-FinalReport -TestResults $results
    
    if ($success) {
        Write-Host "`n🎉 TODOS OS TESTES CONCLUÍDOS COM SUCESSO!" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "`n⚠️ ALGUNS TESTES FALHARAM" -ForegroundColor Yellow
        exit 1
    }
    
} catch {
    Write-Host "`n❌ ERRO DURANTE EXECUÇÃO DOS TESTES" -ForegroundColor Red
    Write-Host "Erro: $($_.Exception.Message)" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n✅ TESTES BLOCKCHAIN JAVA CONCLUÍDOS" -ForegroundColor Green 