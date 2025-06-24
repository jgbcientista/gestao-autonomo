# Script PowerShell para coletar informações da rede Hyperledger Fabric
# Execute este script para descobrir os dados necessários para configuração

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "COLETANDO INFORMACOES DA REDE HYPERLEDGER FABRIC" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se Docker está rodando
try {
    docker info 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Docker esta rodando" -ForegroundColor Green
    } else {
        throw "Docker nao esta rodando"
    }
} catch {
    Write-Host "Docker nao esta rodando. Por favor, inicie o Docker primeiro." -ForegroundColor Red
    exit 1
}

Write-Host ""

# 1. Listar containers do Hyperledger Fabric
Write-Host "CONTAINERS HYPERLEDGER FABRIC EM EXECUCAO:" -ForegroundColor Yellow
Write-Host "--------------------------------------------" -ForegroundColor Yellow

$fabricContainers = docker ps --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}" | Select-String -Pattern "(peer|orderer|ca)"

if (-not $fabricContainers) {
    Write-Host "Nenhum container Hyperledger Fabric encontrado em execucao." -ForegroundColor Red
    Write-Host "Voce precisa iniciar sua rede Hyperledger Fabric primeiro." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Se voce nao tem uma rede, pode usar o test-network:" -ForegroundColor Cyan
    Write-Host "git clone https://github.com/hyperledger/fabric-samples.git" -ForegroundColor White
    Write-Host "cd fabric-samples/test-network" -ForegroundColor White
    Write-Host "./network.sh up createChannel -c mychannel -ca" -ForegroundColor White
    Write-Host ""
    exit 1
}

$fabricContainers | ForEach-Object { Write-Host $_ -ForegroundColor White }
Write-Host ""

# 2. Extrair informações dos containers
Write-Host "ANALISANDO CONTAINERS:" -ForegroundColor Yellow
Write-Host "------------------------" -ForegroundColor Yellow

# Peers
$peers = docker ps --format "{{.Names}}" | Select-String -Pattern "peer"
if ($peers) {
    Write-Host "PEERS encontrados:" -ForegroundColor Green
    foreach ($peer in $peers) {
        $peerName = $peer.ToString()
        $port = docker port $peerName 2>$null | Select-String -Pattern "7051" | ForEach-Object { ($_ -split ":")[1] } | Select-Object -First 1
        if (-not $port) { $port = "7051" }
        Write-Host "  - $peerName`:$port" -ForegroundColor White
    }
} else {
    Write-Host "Nenhum peer encontrado" -ForegroundColor Red
}

Write-Host ""

# Orderers
$orderers = docker ps --format "{{.Names}}" | Select-String -Pattern "orderer"
if ($orderers) {
    Write-Host "ORDERERS encontrados:" -ForegroundColor Green
    foreach ($orderer in $orderers) {
        $ordererName = $orderer.ToString()
        $port = docker port $ordererName 2>$null | Select-String -Pattern "7050" | ForEach-Object { ($_ -split ":")[1] } | Select-Object -First 1
        if (-not $port) { $port = "7050" }
        Write-Host "  - $ordererName`:$port" -ForegroundColor White
    }
} else {
    Write-Host "Nenhum orderer encontrado" -ForegroundColor Red
}

Write-Host ""

# Certificate Authorities
$cas = docker ps --format "{{.Names}}" | Select-String -Pattern "ca"
if ($cas) {
    Write-Host "CERTIFICATE AUTHORITIES encontradas:" -ForegroundColor Green
    foreach ($ca in $cas) {
        $caName = $ca.ToString()
        $port = docker port $caName 2>$null | Select-String -Pattern "7054" | ForEach-Object { ($_ -split ":")[1] } | Select-Object -First 1
        if (-not $port) { $port = "7054" }
        Write-Host "  - https://$caName`:$port" -ForegroundColor White
    }
} else {
    Write-Host "Nenhuma CA encontrada" -ForegroundColor Red
}

Write-Host ""

# 3. Tentar obter informações detalhadas se possível
Write-Host "TENTANDO OBTER INFORMACOES DETALHADAS:" -ForegroundColor Yellow
Write-Host "---------------------------------------" -ForegroundColor Yellow

# Verificar se temos variáveis de ambiente configuradas
if ($env:CORE_PEER_LOCALMSPID) {
    Write-Host "MSP ID: $env:CORE_PEER_LOCALMSPID" -ForegroundColor Green
}

if ($env:CORE_PEER_ADDRESS) {
    Write-Host "Peer Address: $env:CORE_PEER_ADDRESS" -ForegroundColor Green
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "RESUMO PARA CONFIGURACAO:" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Gerar configuração baseada no que foi encontrado
$firstPeer = $peers | Select-Object -First 1
$firstOrderer = $orderers | Select-Object -First 1
$firstCA = $cas | Select-Object -First 1

if ($firstPeer -and $firstOrderer -and $firstCA) {
    Write-Host ""
    Write-Host "CONFIGURACAO SUGERIDA PARA application.yml:" -ForegroundColor Green
    Write-Host "----------------------------------------------" -ForegroundColor Green
    
    Write-Host "blockchain:" -ForegroundColor White
    Write-Host "  enabled: true" -ForegroundColor White
    Write-Host "  network:" -ForegroundColor White
    Write-Host "    type: hyperledger" -ForegroundColor White
    Write-Host "  hyperledger:" -ForegroundColor White
    Write-Host "    channel: `"mychannel`"  # VERIFICAR - canal padrao" -ForegroundColor White
    Write-Host "    chaincode: `"auth-audit`"  # VERIFICAR - nome do seu chaincode" -ForegroundColor White
    Write-Host "    organization: `"Org1MSP`"  # VERIFICAR - sua organizacao" -ForegroundColor White
    Write-Host "    peer: `"$firstPeer`:7051`"" -ForegroundColor White
    Write-Host "    ca-url: `"https://$firstCA`:7054`"" -ForegroundColor White
    Write-Host "    orderer: `"$firstOrderer`:7050`"" -ForegroundColor White
    Write-Host "    user: `"appUser`"  # VERIFICAR - usuario da aplicacao" -ForegroundColor White
    Write-Host "    user-secret: `"appUserSecret`"  # VERIFICAR - senha do usuario" -ForegroundColor White
    Write-Host "    admin-user: `"admin`"" -ForegroundColor White
    Write-Host "    admin-secret: `"adminpw`"" -ForegroundColor White
    Write-Host "    connection-profile-path: `"src/main/resources/connection-profile.yaml`"" -ForegroundColor White
    Write-Host "    wallet-path: `"wallet`"" -ForegroundColor White
    Write-Host "    tls-enabled: false  # AJUSTAR conforme sua rede" -ForegroundColor White
    Write-Host "    simulation-mode: false  # IMPORTANTE: false para usar dados reais" -ForegroundColor White
    
    Write-Host ""
    Write-Host "ATENCAO: Os valores marcados com VERIFICAR precisam ser ajustados!" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "PROXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "------------------" -ForegroundColor Cyan
Write-Host "1. Configure as variaveis de ambiente do peer para obter mais informacoes" -ForegroundColor White
Write-Host "2. Execute novamente este script para obter informacoes completas" -ForegroundColor White
Write-Host "3. Verifique os arquivos de configuracao da sua rede" -ForegroundColor White
Write-Host "4. Atualize o application.yml com os dados corretos" -ForegroundColor White
Write-Host "5. Configure o connection-profile.yaml" -ForegroundColor White
Write-Host ""

# Verificar se existe docker-compose na pasta atual
if (Test-Path "docker-compose.yaml" -or Test-Path "docker-compose.yml") {
    Write-Host "ARQUIVO DOCKER-COMPOSE ENCONTRADO:" -ForegroundColor Green
    Write-Host "------------------------------------" -ForegroundColor Green
    Write-Host "Analisando configuracoes..." -ForegroundColor White
    
    $composeFile = if (Test-Path "docker-compose.yaml") { "docker-compose.yaml" } else { "docker-compose.yml" }
    
    Write-Host "Servicos encontrados no $composeFile`:" -ForegroundColor Yellow
    Get-Content $composeFile | Select-String -Pattern "^\s+[a-zA-Z0-9_-]+:" | ForEach-Object {
        $service = ($_ -replace ":", "").Trim()
        Write-Host "  - $service" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "Script concluido! Use as informacoes acima para configurar sua aplicacao." -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan 