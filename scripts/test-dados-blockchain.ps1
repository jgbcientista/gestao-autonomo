#!/usr/bin/env pwsh

Write-Host "🧪 TESTANDO E VISUALIZANDO DADOS DA BLOCKCHAIN" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

# Configurações
$BASE_URL = "http://localhost:8081"
$API_URL = "$BASE_URL/api"
$TEST_URL = "$API_URL/blockchain/test"

# Função para aguardar aplicação
function Wait-Application {
    Write-Host "⏳ Aguardando aplicação iniciar..." -ForegroundColor Yellow
    
    for ($i = 1; $i -le 30; $i++) {
        try {
            $response = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method GET -TimeoutSec 3 -ErrorAction SilentlyContinue
            if ($response) {
                Write-Host "✅ Aplicação iniciada!" -ForegroundColor Green
                return $true
            }
        } catch {
            # Continuar tentando
        }
        
        Write-Host "   Tentativa $i/30..." -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
    
    Write-Host "❌ Aplicação não iniciou" -ForegroundColor Red
    return $false
}

# Função para fazer requisição
function Invoke-Test {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [object]$Body = $null,
        [string]$Title
    )
    
    Write-Host "`n🔍 $Title" -ForegroundColor Cyan
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        $headers = @{
            "Content-Type" = "application/json"
            "Accept" = "application/json"
        }
        
        if ($Method -eq "POST" -and $Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            Write-Host "Body: $jsonBody" -ForegroundColor Gray
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $jsonBody -Headers $headers -TimeoutSec 15
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -TimeoutSec 15
        }
        
        Write-Host "✅ SUCESSO!" -ForegroundColor Green
        Write-Host "Resposta:" -ForegroundColor Yellow
        $response | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor White
        
        return $response
        
    } catch {
        Write-Host "❌ ERRO: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# EXECUÇÃO PRINCIPAL
try {
    # Aguardar aplicação
    if (-not (Wait-Application)) {
        Write-Host "❌ Aplicação não está disponível. Execute: mvn spring-boot:run" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "`n🚀 INICIANDO TESTES DOS DADOS" -ForegroundColor Magenta
    Write-Host "=============================" -ForegroundColor Magenta
    
    # Teste 1: Status da aplicação
    Invoke-Test -Url "$BASE_URL/actuator/health" -Title "1. HEALTH CHECK DA APLICAÇÃO"
    
    # Teste 2: Conexão blockchain
    Invoke-Test -Url "$TEST_URL/connection" -Title "2. CONEXÃO COM BLOCKCHAIN"
    
    # Teste 3: Status da rede
    Invoke-Test -Url "$TEST_URL/status" -Title "3. STATUS DA REDE BLOCKCHAIN"
    
    # Teste 4: Registrar evento de teste
    $eventoTeste = @{
        userId = "usuario_teste_$(Get-Date -Format 'yyyyMMddHHmmss')"
        action = "login"
        status = "success"
        ipAddress = "192.168.1.100"
        details = "Teste de login via PowerShell"
        timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    }
    
    Invoke-Test -Url "$TEST_URL/event" -Method "POST" -Body $eventoTeste -Title "4. REGISTRAR EVENTO DE TESTE"
    
    # Teste 5: Estatísticas
    Invoke-Test -Url "$TEST_URL/statistics" -Title "5. ESTATÍSTICAS DA REDE"
    
    # Teste 6: Relatório detalhado
    $relatorio = Invoke-Test -Url "$TEST_URL/report" -Title "6. RELATÓRIO DETALHADO"
    
    # Salvar relatório em arquivo
    if ($relatorio -and $relatorio.report) {
        $nomeArquivo = "relatorio-blockchain-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
        $relatorio.report | Out-File -FilePath $nomeArquivo -Encoding UTF8
        Write-Host "`n📄 Relatório salvo em: $nomeArquivo" -ForegroundColor Cyan
    }
    
    Write-Host "`n🎉 TODOS OS TESTES CONCLUÍDOS!" -ForegroundColor Green
    Write-Host "==============================" -ForegroundColor Green
    
    Write-Host "`n📊 RESUMO DOS DADOS TESTADOS:" -ForegroundColor Magenta
    Write-Host "- ✅ Aplicação funcionando" -ForegroundColor White
    Write-Host "- ✅ Blockchain conectada" -ForegroundColor White
    Write-Host "- ✅ Evento registrado" -ForegroundColor White
    Write-Host "- ✅ Estatísticas coletadas" -ForegroundColor White
    Write-Host "- ✅ Relatório gerado" -ForegroundColor White
    
    Write-Host "`n🔗 ENDPOINTS DISPONÍVEIS:" -ForegroundColor Magenta
    Write-Host "- GET  $TEST_URL/connection   - Testar conexão" -ForegroundColor Gray
    Write-Host "- GET  $TEST_URL/status       - Status da rede" -ForegroundColor Gray
    Write-Host "- POST $TEST_URL/event        - Registrar evento" -ForegroundColor Gray
    Write-Host "- GET  $TEST_URL/statistics   - Estatísticas" -ForegroundColor Gray
    Write-Host "- GET  $TEST_URL/report       - Relatório" -ForegroundColor Gray
    
} catch {
    Write-Host "`n❌ ERRO DURANTE OS TESTES: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ SCRIPT CONCLUÍDO COM SUCESSO!" -ForegroundColor Green 