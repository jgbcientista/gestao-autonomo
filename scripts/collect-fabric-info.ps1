# Script PowerShell para coletar informações da rede Hyperledger Fabric
# Execute este script para descobrir os dados necessários para configuração

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🔍 COLETANDO INFORMAÇÕES DA REDE HYPERLEDGER FABRIC" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se Docker está rodando
try {
    docker info *>$null
    Write-Host "✅ Docker está rodando" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker não está rodando. Por favor, inicie o Docker primeiro." -ForegroundColor Red
    exit 1
}

Write-Host ""

# 1. Listar containers do Hyperledger Fabric
Write-Host "📋 CONTAINERS HYPERLEDGER FABRIC EM EXECUÇÃO:" -ForegroundColor Yellow
Write-Host "--------------------------------------------" -ForegroundColor Yellow

$fabricContainers = docker ps --format "table {{.Names}}`t{{.Ports}}`t{{.Status}}" | Select-String -Pattern "(peer|orderer|ca)"

if (-not $fabricContainers) {
    Write-Host "❌ Nenhum container Hyperledger Fabric encontrado em execução." -ForegroundColor Red
    Write-Host "💡 Você precisa iniciar sua rede Hyperledger Fabric primeiro." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Se você não tem uma rede, pode usar o test-network:" -ForegroundColor Cyan
    Write-Host "git clone https://github.com/hyperledger/fabric-samples.git" -ForegroundColor White
    Write-Host "cd fabric-samples/test-network" -ForegroundColor White
    Write-Host "./network.sh up createChannel -c mychannel -ca" -ForegroundColor White
    Write-Host ""
    exit 1
} else {
    $fabricContainers | ForEach-Object { Write-Host $_ -ForegroundColor White }
}

Write-Host ""

# 2. Extrair informações dos containers
Write-Host "🔍 ANALISANDO CONTAINERS:" -ForegroundColor Yellow
Write-Host "------------------------" -ForegroundColor Yellow

# Peers
$peers = docker ps --format "{{.Names}}" | Select-String -Pattern "^peer"
if ($peers) {
    Write-Host "📡 PEERS encontrados:" -ForegroundColor Green
    foreach ($peer in $peers) {
        $port = docker port $peer 2>$null | Select-String -Pattern "7051" | ForEach-Object { ($_ -split ":")[1] } | Select-Object -First 1
        if (-not $port) { $port = "7051" }
        Write-Host "  - $peer`:$port" -ForegroundColor White
    }
} else {
    Write-Host "❌ Nenhum peer encontrado" -ForegroundColor Red
}

Write-Host ""

# Orderers
$orderers = docker ps --format "{{.Names}}" | Select-String -Pattern "orderer"
if ($orderers) {
    Write-Host "📦 ORDERERS encontrados:" -ForegroundColor Green
    foreach ($orderer in $orderers) {
        $port = docker port $orderer 2>$null | Select-String -Pattern "7050" | ForEach-Object { ($_ -split ":")[1] } | Select-Object -First 1
        if (-not $port) { $port = "7050" }
        Write-Host "  - $orderer`:$port" -ForegroundColor White
    }
} else {
    Write-Host "❌ Nenhum orderer encontrado" -ForegroundColor Red
}

Write-Host ""

# Certificate Authorities
$cas = docker ps --format "{{.Names}}" | Select-String -Pattern "ca"
if ($cas) {
    Write-Host "🔐 CERTIFICATE AUTHORITIES encontradas:" -ForegroundColor Green
    foreach ($ca in $cas) {
        $port = docker port $ca 2>$null | Select-String -Pattern "7054" | ForEach-Object { ($_ -split ":")[1] } | Select-Object -First 1
        if (-not $port) { $port = "7054" }
        Write-Host "  - https://$ca`:$port" -ForegroundColor White
    }
} else {
    Write-Host "❌ Nenhuma CA encontrada" -ForegroundColor Red
}

Write-Host ""

# 3. Tentar obter informações detalhadas se possível
Write-Host "🔍 TENTANDO OBTER INFORMAÇÕES DETALHADAS:" -ForegroundColor Yellow
Write-Host "---------------------------------------" -ForegroundColor Yellow

# Verificar se temos variáveis de ambiente configuradas
if ($env:CORE_PEER_LOCALMSPID) {
    Write-Host "✅ MSP ID: $env:CORE_PEER_LOCALMSPID" -ForegroundColor Green
}

if ($env:CORE_PEER_ADDRESS) {
    Write-Host "✅ Peer Address: $env:CORE_PEER_ADDRESS" -ForegroundColor Green
}

# Tentar listar canais
Write-Host ""
Write-Host "📺 TENTANDO LISTAR CANAIS:" -ForegroundColor Yellow
try {
    $channels = peer channel list 2>$null | Select-String -Pattern "Channel Name:" | ForEach-Object { ($_ -split ":")[1].Trim() }
    if ($channels) {
        Write-Host "✅ Canais encontrados:" -ForegroundColor Green
        foreach ($channel in $channels) {
            Write-Host "  - $channel" -ForegroundColor White
        }
    } else {
        Write-Host "⚠️  Não foi possível listar canais (configure as variáveis de ambiente do peer)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Não foi possível listar canais (configure as variáveis de ambiente do peer)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🔗 TENTANDO LISTAR CHAINCODES:" -ForegroundColor Yellow
try {
    $chaincodes = peer lifecycle chaincode queryinstalled 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Chaincodes instalados:" -ForegroundColor Green
        Write-Host $chaincodes -ForegroundColor White
    } else {
        Write-Host "⚠️  Não foi possível listar chaincodes (configure as variáveis de ambiente do peer)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Não foi possível listar chaincodes (configure as variáveis de ambiente do peer)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "📝 RESUMO PARA CONFIGURAÇÃO:" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Gerar configuração baseada no que foi encontrado
$firstPeer = $peers | Select-Object -First 1
$firstOrderer = $orderers | Select-Object -First 1
$firstCA = $cas | Select-Object -First 1

if ($firstPeer -and $firstOrderer -and $firstCA) {
    Write-Host ""
    Write-Host "📄 CONFIGURAÇÃO SUGERIDA PARA application.yml:" -ForegroundColor Green
    Write-Host "----------------------------------------------" -ForegroundColor Green
    
    $config = @"
blockchain:
  enabled: true
  network:
    type: hyperledger
  hyperledger:
    channel: "mychannel"  # ⚠️ VERIFICAR - canal padrão
    chaincode: "auth-audit"  # ⚠️ VERIFICAR - nome do seu chaincode
    organization: "Org1MSP"  # ⚠️ VERIFICAR - sua organização
    peer: "$firstPeer:7051"
    ca-url: "https://$firstCA:7054"
    orderer: "$firstOrderer:7050"
    user: "appUser"  # ⚠️ VERIFICAR - usuário da aplicação
    user-secret: "appUserSecret"  # ⚠️ VERIFICAR - senha do usuário
    admin-user: "admin"
    admin-secret: "adminpw"
    connection-profile-path: "src/main/resources/connection-profile.yaml"
    wallet-path: "wallet"
    tls-enabled: false  # ⚠️ AJUSTAR conforme sua rede
    simulation-mode: false  # IMPORTANTE: false para usar dados reais
"@
    
    Write-Host $config -ForegroundColor White
    Write-Host ""
    Write-Host "⚠️  ATENÇÃO: Os valores marcados com ⚠️ precisam ser verificados/ajustados!" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "🔧 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "------------------" -ForegroundColor Cyan
Write-Host "1. Configure as variáveis de ambiente do peer para obter mais informações:" -ForegroundColor White
Write-Host "   `$env:CORE_PEER_LOCALMSPID = 'Org1MSP'" -ForegroundColor Gray
Write-Host "   `$env:CORE_PEER_ADDRESS = '$firstPeer:7051'" -ForegroundColor Gray
Write-Host "   `$env:CORE_PEER_MSPCONFIGPATH = '/path/to/msp'" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Execute novamente este script para obter informações completas" -ForegroundColor White
Write-Host ""
Write-Host "3. Verifique os arquivos de configuração da sua rede:" -ForegroundColor White
Write-Host "   - docker-compose.yaml" -ForegroundColor Gray
Write-Host "   - configtx.yaml" -ForegroundColor Gray
Write-Host "   - crypto-config.yaml" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Atualize o application.yml com os dados corretos" -ForegroundColor White
Write-Host ""
Write-Host "5. Configure o connection-profile.yaml" -ForegroundColor White
Write-Host ""

# Verificar se existe docker-compose na pasta atual
if (Test-Path "docker-compose.yaml" -or Test-Path "docker-compose.yml") {
    Write-Host "📄 ARQUIVO DOCKER-COMPOSE ENCONTRADO:" -ForegroundColor Green
    Write-Host "------------------------------------" -ForegroundColor Green
    Write-Host "Analisando configurações..." -ForegroundColor White
    
    $composeFile = if (Test-Path "docker-compose.yaml") { "docker-compose.yaml" } else { "docker-compose.yml" }
    
    Write-Host "🔍 Serviços encontrados no $composeFile`:" -ForegroundColor Yellow
    Get-Content $composeFile | Select-String -Pattern "^\s+[a-zA-Z0-9_-]+:" | ForEach-Object {
        $service = ($_ -replace ":", "").Trim()
        Write-Host "  - $service" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "✅ Script concluído! Use as informações acima para configurar sua aplicação." -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan 