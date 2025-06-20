#!/usr/bin/env pwsh
# Script de Teste Completo - Hyperledger Fabric Funcional
# Versão: 1.0.0
# Data: 2025-01-20

param(
    [string]$BaseUrl = "http://localhost:8081",
    [string]$Username = "admin",
    [string]$Password = "admin123",
    [switch]$Verbose = $false
)

# Configurações
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# Cores para output
$Green = "Green"
$Red = "Red"
$Yellow = "Yellow"
$Cyan = "Cyan"
$White = "White"

# Variáveis globais
$script:Token = $null
$script:TestResults = @()
$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0

# Função para log colorido
function Write-ColorLog {
    param(
        [string]$Message,
        [string]$Color = "White",
        [string]$Level = "INFO"
    )
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    Write-Host $logMessage -ForegroundColor $Color
    
    if ($Verbose) {
        Add-Content -Path "hyperledger-test.log" -Value $logMessage
    }
}

# Função para fazer requisições HTTP
function Invoke-ApiRequest {
    param(
        [string]$Method = "GET",
        [string]$Endpoint,
        [hashtable]$Body = $null,
        [hashtable]$Headers = @{},
        [bool]$RequireAuth = $true
    )
    
    try {
        $uri = "$BaseUrl$Endpoint"
        
        if ($RequireAuth -and $script:Token) {
            $Headers["Authorization"] = "Bearer $script:Token"
        }
        
        $Headers["Content-Type"] = "application/json"
        
        $params = @{
            Uri = $uri
            Method = $Method
            Headers = $Headers
            TimeoutSec = 30
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        if ($Verbose) {
            Write-ColorLog "REQUEST: $Method $uri" $Cyan
            if ($Body) {
                Write-ColorLog "BODY: $($params.Body)" $Cyan
            }
        }
        
        $response = Invoke-RestMethod @params
        
        if ($Verbose) {
            Write-ColorLog "RESPONSE: $($response | ConvertTo-Json -Depth 5)" $Cyan
        }
        
        return $response
    }
    catch {
        Write-ColorLog "Erro na requisição: $($_.Exception.Message)" $Red "ERROR"
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode
            Write-ColorLog "Status Code: $statusCode" $Red "ERROR"
        }
        throw
    }
}

# Função para executar teste
function Execute-Test {
    param(
        [string]$TestName,
        [scriptblock]$TestBlock
    )
    
    $script:TotalTests++
    Write-ColorLog "🧪 Executando: $TestName" $Cyan "TEST"
    
    try {
        $result = & $TestBlock
        
        if ($result -eq $true -or $result -eq $null) {
            $script:PassedTests++
            Write-ColorLog "✅ PASSOU: $TestName" $Green "PASS"
            $script:TestResults += @{
                Name = $TestName
                Status = "PASSED"
                Message = "Teste executado com sucesso"
            }
        } else {
            $script:FailedTests++
            Write-ColorLog "❌ FALHOU: $TestName" $Red "FAIL"
            $script:TestResults += @{
                Name = $TestName
                Status = "FAILED"
                Message = "Teste retornou falso"
            }
        }
    }
    catch {
        $script:FailedTests++
        Write-ColorLog "❌ ERRO: $TestName - $($_.Exception.Message)" $Red "ERROR"
        $script:TestResults += @{
            Name = $TestName
            Status = "ERROR"
            Message = $_.Exception.Message
        }
    }
}

# Função de autenticação
function Authenticate {
    Write-ColorLog "🔐 Iniciando autenticação..." $Yellow
    
    try {
        # Primeiro, tenta fazer login
        $loginBody = @{
            email = $Username
            password = $Password
        }
        
        $response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/auth/login" -Body $loginBody -RequireAuth $false
        
        if ($response.token) {
            $script:Token = $response.token
            Write-ColorLog "✅ Autenticação bem-sucedida" $Green
            return $true
        }
    }
    catch {
        Write-ColorLog "❌ Login falhou, tentando registro..." $Yellow
        
        try {
            # Se login falhar, tenta registrar
            $registerBody = @{
                nome = "Admin Test"
                email = $Username
                password = $Password
            }
            
            $response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/auth/register" -Body $registerBody -RequireAuth $false
            
            if ($response.token) {
                $script:Token = $response.token
                Write-ColorLog "✅ Registro e autenticação bem-sucedidos" $Green
                return $true
            }
        }
        catch {
            Write-ColorLog "❌ Falha na autenticação: $($_.Exception.Message)" $Red
            return $false
        }
    }
    
    return $false
}

# Testes específicos do Hyperledger Fabric

function Test-HyperledgerNetworkInfo {
    $response = Invoke-ApiRequest -Endpoint "/api/v1/blockchain/auditoria/info"
    
    if ($response) {
        Write-ColorLog "📊 Informações da rede Hyperledger:" $Cyan
        Write-ColorLog "   - Rede: $($response.network)" $White
        Write-ColorLog "   - Canal: $($response.channelName)" $White
        Write-ColorLog "   - Chaincode: $($response.chaincodeName)" $White
        Write-ColorLog "   - Organização: $($response.organizationMspId)" $White
        Write-ColorLog "   - Conectado: $($response.connected)" $White
        Write-ColorLog "   - Modo: $($response.mode)" $White
        Write-ColorLog "   - Descrição: $($response.description)" $White
        
        return $response.network -eq "Hyperledger Fabric"
    }
    
    return $false
}

function Test-HyperledgerConnectivity {
    $response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/blockchain/auditoria/test-connectivity"
    
    if ($response -and $response.conectividade) {
        Write-ColorLog "🔗 Conectividade Hyperledger: $($response.conectividade)" $Green
        return $response.conectividade -eq $true
    }
    
    return $false
}

function Test-AuthenticationWithHyperledger {
    # Faz login para gerar evento de autenticação
    $loginBody = @{
        email = "test@hyperledger.com"
        password = "test123"
    }
    
    try {
        # Tenta login que deve gerar evento blockchain
        Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/auth/login" -Body $loginBody -RequireAuth $false
    }
    catch {
        # Esperado falhar, mas deve gerar evento
    }
    
    # Aguarda processamento
    Start-Sleep -Seconds 2
    
    # Verifica se transações foram criadas
    $transacoes = Invoke-ApiRequest -Endpoint "/api/v1/blockchain/auditoria/transacoes/nao-verificadas"
    
    if ($transacoes -and $transacoes.Count -gt 0) {
        Write-ColorLog "🔒 Transações de autenticação geradas: $($transacoes.Count)" $Green
        
        # Verifica se alguma transação tem hash do Hyperledger
        $hyperledgerTx = $transacoes | Where-Object { $_.hashTransacao -like "hlf_*" }
        
        if ($hyperledgerTx) {
            Write-ColorLog "✅ Transação Hyperledger encontrada: $($hyperledgerTx[0].hashTransacao)" $Green
            return $true
        }
    }
    
    return $false
}

function Test-HyperledgerTransactionQuery {
    # Obtém lista de transações
    $transacoes = Invoke-ApiRequest -Endpoint "/api/v1/blockchain/auditoria/transacoes/nao-verificadas"
    
    if ($transacoes -and $transacoes.Count -gt 0) {
        $txHash = $transacoes[0].hashTransacao
        
        # Consulta transação específica
        $response = Invoke-ApiRequest -Endpoint "/api/v1/blockchain/auditoria/transacao/$txHash"
        
        if ($response) {
            Write-ColorLog "🔍 Consulta de transação bem-sucedida" $Green
            Write-ColorLog "   - Hash: $($response.hashTransacao)" $White
            Write-ColorLog "   - Status: $($response.statusConfirmacao)" $White
            Write-ColorLog "   - Usuário: $($response.usuarioEmail)" $White
            return $true
        }
    }
    
    return $false
}

function Test-HyperledgerHighRiskTransactions {
    $response = Invoke-ApiRequest -Endpoint "/api/v1/blockchain/auditoria/transacoes/alto-risco?limiteRisco=0.5"
    
    if ($response) {
        Write-ColorLog "⚠️  Transações de alto risco encontradas: $($response.Count)" $Yellow
        return $true
    }
    
    return $false
}

function Test-HyperledgerAnalytics {
    $response = Invoke-ApiRequest -Endpoint "/api/v1/analytics/blockchain/statistics"
    
    if ($response) {
        Write-ColorLog "📈 Estatísticas da blockchain:" $Cyan
        Write-ColorLog "   - Total de transações: $($response.totalTransacoes)" $White
        Write-ColorLog "   - Transações não verificadas: $($response.naoVerificadas)" $White
        Write-ColorLog "   - Transações de alto risco: $($response.altoRisco)" $White
        return $true
    }
    
    return $false
}

function Test-HyperledgerIntegration {
    # Testa integração completa
    $testUser = @{
        nome = "Hyperledger Test User"
        email = "hyperledger@test.com"
        password = "hlf123456"
    }
    
    try {
        # Registra usuário
        $registerResponse = Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/auth/register" -Body $testUser -RequireAuth $false
        
        if ($registerResponse.token) {
            Write-ColorLog "👤 Usuário de teste criado com sucesso" $Green
            
            # Aguarda processamento da transação
            Start-Sleep -Seconds 3
            
            # Verifica se transação foi registrada no Hyperledger
            $transacoes = Invoke-ApiRequest -Endpoint "/api/v1/blockchain/auditoria/transacoes/nao-verificadas"
            
            $hyperledgerTx = $transacoes | Where-Object { 
                $_.usuarioEmail -eq $testUser.email -and $_.hashTransacao -like "hlf_*" 
            }
            
            if ($hyperledgerTx) {
                Write-ColorLog "🎯 Integração Hyperledger confirmada" $Green
                Write-ColorLog "   - Hash da transação: $($hyperledgerTx.hashTransacao)" $White
                Write-ColorLog "   - Tipo de evento: $($hyperledgerTx.tipoEvento)" $White
                return $true
            }
        }
    }
    catch {
        Write-ColorLog "❌ Erro na integração: $($_.Exception.Message)" $Red
    }
    
    return $false
}

# Função principal
function Main {
    Write-ColorLog "🚀 Iniciando Teste Completo - Hyperledger Fabric Funcional" $Green
    Write-ColorLog "🔗 URL Base: $BaseUrl" $White
    Write-ColorLog "👤 Usuário: $Username" $White
    Write-ColorLog "📝 Verbose: $Verbose" $White
    Write-ColorLog "=" * 60 $White
    
    # Verifica se o serviço está rodando
    try {
        $healthCheck = Invoke-ApiRequest -Endpoint "/actuator/health" -RequireAuth $false
        Write-ColorLog "✅ Serviço está rodando" $Green
    }
    catch {
        Write-ColorLog "❌ Serviço não está acessível em $BaseUrl" $Red
        Write-ColorLog "Por favor, inicie o serviço e tente novamente." $Yellow
        exit 1
    }
    
    # Autenticação
    if (-not (Authenticate)) {
        Write-ColorLog "❌ Falha na autenticação. Abortando testes." $Red
        exit 1
    }
    
    Write-ColorLog "🧪 Iniciando testes do Hyperledger Fabric..." $Cyan
    Write-ColorLog "=" * 60 $White
    
    # Executa todos os testes
    Execute-Test "Informações da Rede Hyperledger" { Test-HyperledgerNetworkInfo }
    Execute-Test "Conectividade Hyperledger" { Test-HyperledgerConnectivity }
    Execute-Test "Autenticação com Hyperledger" { Test-AuthenticationWithHyperledger }
    Execute-Test "Consulta de Transação Hyperledger" { Test-HyperledgerTransactionQuery }
    Execute-Test "Transações de Alto Risco" { Test-HyperledgerHighRiskTransactions }
    Execute-Test "Analytics da Blockchain" { Test-HyperledgerAnalytics }
    Execute-Test "Integração Completa Hyperledger" { Test-HyperledgerIntegration }
    
    # Relatório final
    Write-ColorLog "=" * 60 $White
    Write-ColorLog "📊 RELATÓRIO FINAL - HYPERLEDGER FABRIC" $White
    Write-ColorLog "=" * 60 $White
    Write-ColorLog "📈 Total de testes: $script:TotalTests" $White
    Write-ColorLog "✅ Testes aprovados: $script:PassedTests" $Green
    Write-ColorLog "❌ Testes falharam: $script:FailedTests" $Red
    Write-ColorLog "🎯 Taxa de sucesso: $([math]::Round(($script:PassedTests / $script:TotalTests) * 100, 2))%" $White
    
    if ($script:FailedTests -eq 0) {
        Write-ColorLog "🎉 TODOS OS TESTES PASSARAM! Hyperledger Fabric está funcionando corretamente!" $Green
        $exitCode = 0
    } else {
        Write-ColorLog "⚠️  ALGUNS TESTES FALHARAM. Verifique os logs acima." $Yellow
        $exitCode = 1
    }
    
    Write-ColorLog "=" * 60 $White
    
    # Salva relatório se verbose
    if ($Verbose) {
        $reportPath = "hyperledger-test-report.json"
        $report = @{
            timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            totalTests = $script:TotalTests
            passedTests = $script:PassedTests
            failedTests = $script:FailedTests
            successRate = [math]::Round(($script:PassedTests / $script:TotalTests) * 100, 2)
            results = $script:TestResults
        }
        
        $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $reportPath -Encoding UTF8
        Write-ColorLog "📝 Relatório salvo em: $reportPath" $Cyan
    }
    
    exit $exitCode
}

# Executa o script principal
try {
    Main
}
catch {
    Write-ColorLog "❌ Erro fatal: $($_.Exception.Message)" $Red
    exit 1
} 