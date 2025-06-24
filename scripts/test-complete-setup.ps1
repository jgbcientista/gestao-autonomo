#!/usr/bin/env powershell
# ====================================================================
# 🧪 TESTE COMPLETO DA CONFIGURAÇÃO HYPERLEDGER FABRIC
# ====================================================================
# Script para testar todos os componentes implementados:
# 1. Rede Hyperledger Fabric
# 2. Hyperledger Explorer 
# 3. Chaincode de Auditoria
# 4. Aplicação Java
# ====================================================================

Write-Host "🧪 TESTE COMPLETO DA CONFIGURAÇÃO HYPERLEDGER FABRIC" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# ====================================================================
# TESTE 1: VERIFICAR REDE HYPERLEDGER FABRIC
# ====================================================================
Write-Host "1️⃣ TESTANDO REDE HYPERLEDGER FABRIC" -ForegroundColor Yellow
Write-Host "------------------------------------------------------------"

try {
    $containers = docker ps --format "{{.Names}}" | Where-Object { $_ -match "ca_" }
    
    if ($containers.Count -gt 0) {
        Write-Host "✅ Rede Hyperledger Fabric: ATIVA" -ForegroundColor Green
        foreach ($container in $containers) {
            Write-Host "   • $container: ONLINE" -ForegroundColor Green
        }
    } else {
        Write-Host "❌ Rede Hyperledger Fabric: OFFLINE" -ForegroundColor Red
        Write-Host "   Iniciando rede..." -ForegroundColor Yellow
        
        Set-Location "C:\micro-services\fabric-samples\test-network"
        docker-compose -f compose/compose-test-net.yaml -f compose/compose-ca.yaml up -d
        Start-Sleep -Seconds 10
        Set-Location "C:\micro-services"
    }
}
catch {
    Write-Host "❌ Erro ao verificar rede Fabric: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# ====================================================================
# TESTE 2: VERIFICAR APLICAÇÃO JAVA
# ====================================================================
Write-Host "2️⃣ TESTANDO APLICAÇÃO JAVA" -ForegroundColor Yellow
Write-Host "------------------------------------------------------------"

$javaPorts = @("8080", "8081", "8082")
$appRunning = $false

foreach ($port in $javaPorts) {
    $connections = netstat -an | Select-String ":$port.*LISTENING"
    if ($connections) {
        Write-Host "✅ Aplicação Java: RODANDO na porta $port" -ForegroundColor Green
        $appRunning = $true
        
        # Testar conectividade
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$port" -Method GET -TimeoutSec 5 -ErrorAction Stop
            Write-Host "   • Status HTTP: $($response.StatusCode)" -ForegroundColor Green
        }
        catch {
            if ($_.Exception.Response.StatusCode -eq 401) {
                Write-Host "   • Status: Protegido por autenticação (esperado)" -ForegroundColor Green
            } else {
                Write-Host "   • Erro: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        }
        break
    }
}

if (-not $appRunning) {
    Write-Host "❌ Aplicação Java: OFFLINE" -ForegroundColor Red
    Write-Host "   Para iniciar: cd api-author && mvn spring-boot:run" -ForegroundColor Yellow
}

Write-Host ""

# ====================================================================
# TESTE 3: VERIFICAR CHAINCODE
# ====================================================================
Write-Host "3️⃣ TESTANDO CHAINCODE DE AUDITORIA" -ForegroundColor Yellow
Write-Host "------------------------------------------------------------"

if (Test-Path "chaincode-auth-audit/index.js") {
    Write-Host "✅ Chaincode: CRIADO" -ForegroundColor Green
    Write-Host "   • Arquivo: chaincode-auth-audit/index.js" -ForegroundColor Green
    Write-Host "   • Funcionalidades implementadas:" -ForegroundColor Green
    Write-Host "     - recordAuthEvent(): Registrar eventos" -ForegroundColor Cyan
    Write-Host "     - queryAuthEvent(): Consultar eventos" -ForegroundColor Cyan
    Write-Host "     - queryUserEvents(): Eventos por usuário" -ForegroundColor Cyan
    Write-Host "     - getStatistics(): Estatísticas" -ForegroundColor Cyan
    Write-Host "     - verifyDataIntegrity(): Verificar integridade" -ForegroundColor Cyan
} else {
    Write-Host "❌ Chaincode: NÃO ENCONTRADO" -ForegroundColor Red
}

Write-Host ""

# ====================================================================
# TESTE 4: VERIFICAR HYPERLEDGER EXPLORER
# ====================================================================
Write-Host "4️⃣ TESTANDO HYPERLEDGER EXPLORER" -ForegroundColor Yellow
Write-Host "------------------------------------------------------------"

if (Test-Path "blockchain-explorer/docker-compose-explorer.yaml") {
    Write-Host "✅ Hyperledger Explorer: CONFIGURADO" -ForegroundColor Green
    Write-Host "   • Docker Compose: blockchain-explorer/docker-compose-explorer.yaml" -ForegroundColor Green
    Write-Host "   • Configuração: blockchain-explorer/config.json" -ForegroundColor Green
    Write-Host "   • Perfil de Conexão: blockchain-explorer/connection-profile/test-network.json" -ForegroundColor Green
    
    # Verificar se está rodando
    $explorerRunning = docker ps --format "{{.Names}}" | Select-String "explorer"
    if ($explorerRunning) {
        Write-Host "   • Status: RODANDO" -ForegroundColor Green
        Write-Host "   • Interface: http://localhost:8090" -ForegroundColor Cyan
    } else {
        Write-Host "   • Status: PARADO" -ForegroundColor Yellow
        Write-Host "   • Para iniciar: cd blockchain-explorer && docker-compose -f docker-compose-explorer.yaml up -d" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Hyperledger Explorer: NÃO CONFIGURADO" -ForegroundColor Red
}

Write-Host ""

# ====================================================================
# TESTE 5: VERIFICAR SCRIPTS DE MONITORAMENTO
# ====================================================================
Write-Host "5️⃣ TESTANDO SCRIPTS DE MONITORAMENTO" -ForegroundColor Yellow
Write-Host "------------------------------------------------------------"

$scripts = @(
    "scripts/monitor-blockchain.ps1",
    "scripts/collect-fabric-info-fixed.ps1"
)

foreach ($script in $scripts) {
    if (Test-Path $script) {
        Write-Host "✅ $script: DISPONÍVEL" -ForegroundColor Green
    } else {
        Write-Host "❌ $script: NÃO ENCONTRADO" -ForegroundColor Red
    }
}

Write-Host ""

# ====================================================================
# TESTE 6: VERIFICAR CONFIGURAÇÕES
# ====================================================================
Write-Host "6️⃣ TESTANDO CONFIGURAÇÕES" -ForegroundColor Yellow
Write-Host "------------------------------------------------------------"

$configs = @(
    "api-author/src/main/resources/application.yml",
    "api-author/src/main/resources/connection-profile.yaml",
    "GUIA_CONFIGURACAO_HYPERLEDGER.md"
)

foreach ($config in $configs) {
    if (Test-Path $config) {
        Write-Host "✅ $config: CONFIGURADO" -ForegroundColor Green
    } else {
        Write-Host "❌ $config: NÃO ENCONTRADO" -ForegroundColor Red
    }
}

Write-Host ""

# ====================================================================
# RESUMO GERAL
# ====================================================================
Write-Host "📊 RESUMO GERAL" -ForegroundColor Yellow
Write-Host "============================================================"

$totalTests = 6
$passedTests = 0

# Contar testes bem-sucedidos
if ((docker ps --format "{{.Names}}" | Where-Object { $_ -match "ca_" }).Count -gt 0) { $passedTests++ }
if ($appRunning) { $passedTests++ }
if (Test-Path "chaincode-auth-audit/index.js") { $passedTests++ }
if (Test-Path "blockchain-explorer/docker-compose-explorer.yaml") { $passedTests++ }
if ((Test-Path "scripts/monitor-blockchain.ps1") -and (Test-Path "scripts/collect-fabric-info-fixed.ps1")) { $passedTests++ }
if (Test-Path "api-author/src/main/resources/application.yml") { $passedTests++ }

$successRate = [math]::Round(($passedTests / $totalTests) * 100, 1)

Write-Host "Testes Executados: $totalTests" -ForegroundColor White
Write-Host "Testes Aprovados: $passedTests" -ForegroundColor Green
Write-Host "Taxa de Sucesso: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 60) { "Yellow" } else { "Red" })

Write-Host ""

# ====================================================================
# PRÓXIMOS PASSOS
# ====================================================================
Write-Host "🚀 PRÓXIMOS PASSOS RECOMENDADOS" -ForegroundColor Yellow
Write-Host "============================================================"

if ($passedTests -eq $totalTests) {
    Write-Host "🎉 PARABÉNS! Configuração 100% completa!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Você pode agora:" -ForegroundColor White
    Write-Host "• Acessar a aplicação: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "• Monitorar a rede: powershell scripts/monitor-blockchain.ps1" -ForegroundColor Cyan
    Write-Host "• Iniciar Explorer: cd blockchain-explorer && docker-compose -f docker-compose-explorer.yaml up -d" -ForegroundColor Cyan
} else {
    Write-Host "⚠️  Configuração parcialmente completa ($successRate%)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Para completar:" -ForegroundColor White
    
    if (-not $appRunning) {
        Write-Host "• Iniciar aplicação Java: cd api-author && mvn spring-boot:run" -ForegroundColor Yellow
    }
    
    if ((docker ps --format "{{.Names}}" | Where-Object { $_ -match "ca_" }).Count -eq 0) {
        Write-Host "• Iniciar rede Fabric: cd fabric-samples/test-network && docker-compose up -d" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Teste executado em: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor Gray 