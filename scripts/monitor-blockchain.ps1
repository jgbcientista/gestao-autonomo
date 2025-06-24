#!/usr/bin/env powershell
# ====================================================================
# 🔍 MONITOR DA REDE BLOCKCHAIN HYPERLEDGER FABRIC
# ====================================================================
# Script para visualizar e monitorar a rede blockchain em tempo real
# Autor: Sistema de Autenticação
# Data: 24/06/2025
# ====================================================================

Write-Host "🚀 MONITOR DA REDE BLOCKCHAIN HYPERLEDGER FABRIC" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host ""

# Função para exibir status colorido
function Show-Status {
    param(
        [string]$Service,
        [string]$Status,
        [string]$Details = ""
    )
    
    $color = switch ($Status) {
        "ONLINE" { "Green" }
        "OFFLINE" { "Red" }
        "WARNING" { "Yellow" }
        default { "White" }
    }
    
    Write-Host "[$Status]" -ForegroundColor $color -NoNewline
    Write-Host " $Service" -ForegroundColor White
    if ($Details) {
        Write-Host "    └─ $Details" -ForegroundColor Gray
    }
}

# Função para testar conectividade
function Test-ServiceConnection {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 5
    )
    
    try {
        $response = Invoke-WebRequest -Uri $Url -Method HEAD -TimeoutSec $TimeoutSeconds -ErrorAction Stop
        return $true
    }
    catch {
        return $false
    }
}

# ====================================================================
# 1. VERIFICAR CONTAINERS DOCKER
# ====================================================================
Write-Host "🐳 CONTAINERS DOCKER:" -ForegroundColor Yellow
Write-Host "-" * 40

try {
    $containers = docker ps --format "{{.Names}};{{.Status}};{{.Ports}}"
    $filteredContainers = $containers | Where-Object { $_ -match "ca_|peer|orderer" }
    
    if ($filteredContainers) {
        foreach ($container in $filteredContainers) {
            $parts = $container -split ";"
            $name = $parts[0]
            $status = $parts[1]
            $ports = $parts[2]
            
            if ($status -match "Up") {
                Show-Status -Service $name -Status "ONLINE" -Details $ports
            } else {
                Show-Status -Service $name -Status "OFFLINE" -Details $status
            }
        }
    } else {
        Show-Status -Service "Nenhum container encontrado" -Status "WARNING"
    }
}
catch {
    Show-Status -Service "Docker" -Status "OFFLINE" -Details "Docker não está rodando"
}

Write-Host ""

# ====================================================================
# 2. TESTAR CERTIFICATE AUTHORITIES
# ====================================================================
Write-Host "🔐 CERTIFICATE AUTHORITIES:" -ForegroundColor Yellow
Write-Host "-" * 40

$cas = @(
    @{ Name = "CA Org1"; Url = "http://localhost:7054"; Port = "7054" },
    @{ Name = "CA Org2"; Url = "http://localhost:8054"; Port = "8054" },
    @{ Name = "CA Orderer"; Url = "http://localhost:9054"; Port = "9054" }
)

foreach ($ca in $cas) {
    $isOnline = Test-ServiceConnection -Url $ca.Url -TimeoutSeconds 3
    
    if ($isOnline) {
        Show-Status -Service $ca.Name -Status "ONLINE" -Details "Porta $($ca.Port)"
    } else {
        Show-Status -Service $ca.Name -Status "OFFLINE" -Details "Porta $($ca.Port) não responde"
    }
}

Write-Host ""

# ====================================================================
# 3. VERIFICAR APLICAÇÃO JAVA
# ====================================================================
Write-Host "☕ APLICAÇÃO JAVA:" -ForegroundColor Yellow
Write-Host "-" * 40

$javaPorts = @("8080", "8081", "8082")
$appFound = $false

foreach ($port in $javaPorts) {
    $connections = netstat -an | Select-String ":$port.*LISTENING"
    if ($connections) {
        $appFound = $true
        $isResponding = Test-ServiceConnection -Url "http://localhost:$port" -TimeoutSeconds 3
        
        if ($isResponding) {
            Show-Status -Service "API Auth Service" -Status "ONLINE" -Details "Porta $port - Respondendo"
        } else {
            Show-Status -Service "API Auth Service" -Status "WARNING" -Details "Porta $port - Escutando mas não responde"
        }
    }
}

if (-not $appFound) {
    Show-Status -Service "API Auth Service" -Status "OFFLINE" -Details "Nenhuma porta encontrada"
}

Write-Host ""

# ====================================================================
# 4. ESTATÍSTICAS DA REDE
# ====================================================================
Write-Host "📊 ESTATÍSTICAS DA REDE:" -ForegroundColor Yellow
Write-Host "-" * 40

try {
    # Contar containers ativos
    $activeContainers = (docker ps --filter "name=ca_" --format "{{.Names}}").Count
    $totalContainers = (docker ps -a --filter "name=ca_" --format "{{.Names}}").Count
    
    Write-Host "Containers Ativos: $activeContainers/$totalContainers" -ForegroundColor Green
    
    # Verificar uso de portas
    $usedPorts = @()
    foreach ($port in @("7050", "7051", "7054", "8054", "9054", "8080", "8081")) {
        $inUse = netstat -an | Select-String ":$port.*LISTENING"
        if ($inUse) {
            $usedPorts += $port
        }
    }
    
    Write-Host "Portas em Uso: $($usedPorts -join ', ')" -ForegroundColor Cyan
    
    # Tempo de atividade dos containers
    $containers = docker ps --filter "name=ca_" --format "{{.Names}} {{.Status}}"
    foreach ($container in $containers) {
        if ($container -match "Up (.+)") {
            $uptime = $matches[1]
            $name = ($container -split " ")[0]
            Write-Host "Uptime $name`: $uptime" -ForegroundColor Gray
        }
    }
}
catch {
    Write-Host "Erro ao coletar estatísticas" -ForegroundColor Red
}

Write-Host ""

# ====================================================================
# 5. LINKS ÚTEIS
# ====================================================================
Write-Host "🔗 LINKS ÚTEIS:" -ForegroundColor Yellow
Write-Host "-" * 40

Write-Host "• CA Org1: http://localhost:7054" -ForegroundColor Cyan
Write-Host "• CA Org2: http://localhost:8054" -ForegroundColor Cyan
Write-Host "• CA Orderer: http://localhost:9054" -ForegroundColor Cyan

if ($appFound) {
    foreach ($port in $javaPorts) {
        $connections = netstat -an | Select-String ":$port.*LISTENING"
        if ($connections) {
            Write-Host "• API Auth: http://localhost:$port" -ForegroundColor Green
            break
        }
    }
}

Write-Host ""

# ====================================================================
# 6. COMANDOS ÚTEIS
# ====================================================================
Write-Host "🛠️  COMANDOS ÚTEIS:" -ForegroundColor Yellow
Write-Host "-" * 40

Write-Host "Verificar logs:" -ForegroundColor White
Write-Host "  docker logs ca_org1" -ForegroundColor Gray
Write-Host "  docker logs ca_org2" -ForegroundColor Gray

Write-Host "Reiniciar rede:" -ForegroundColor White
Write-Host "  cd fabric-samples/test-network" -ForegroundColor Gray
Write-Host "  docker-compose -f compose/compose-test-net.yaml -f compose/compose-ca.yaml restart" -ForegroundColor Gray

Write-Host "Parar rede:" -ForegroundColor White
Write-Host "  docker-compose -f compose/compose-test-net.yaml -f compose/compose-ca.yaml down" -ForegroundColor Gray

Write-Host ""
Write-Host "🔄 Para atualizar este monitor, execute novamente:" -ForegroundColor Yellow
Write-Host "powershell -ExecutionPolicy Bypass -File scripts/monitor-blockchain.ps1" -ForegroundColor Cyan

Write-Host ""
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "Monitor executado em: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor Gray 