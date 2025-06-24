# Script de Demonstração - Configuração Hyperledger Fabric
# Este script simula como seria a coleta de informações com uma rede real

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "DEMONSTRACAO - CONFIGURACAO HYPERLEDGER FABRIC" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "SIMULANDO uma rede Hyperledger Fabric em execucao..." -ForegroundColor Yellow
Write-Host ""

# Simular containers em execução
Write-Host "CONTAINERS HYPERLEDGER FABRIC EM EXECUCAO:" -ForegroundColor Yellow
Write-Host "--------------------------------------------" -ForegroundColor Yellow
Write-Host "NAMES                    PORTS                    STATUS" -ForegroundColor White
Write-Host "peer0.org1.example.com   0.0.0.0:7051->7051/tcp   Up 2 hours" -ForegroundColor Green
Write-Host "peer0.org2.example.com   0.0.0.0:9051->7051/tcp   Up 2 hours" -ForegroundColor Green
Write-Host "orderer.example.com      0.0.0.0:7050->7050/tcp   Up 2 hours" -ForegroundColor Green
Write-Host "ca.org1.example.com      0.0.0.0:7054->7054/tcp   Up 2 hours" -ForegroundColor Green
Write-Host "ca.org2.example.com      0.0.0.0:8054->7054/tcp   Up 2 hours" -ForegroundColor Green
Write-Host ""

Write-Host "ANALISANDO CONTAINERS:" -ForegroundColor Yellow
Write-Host "------------------------" -ForegroundColor Yellow

Write-Host "PEERS encontrados:" -ForegroundColor Green
Write-Host "  - peer0.org1.example.com:7051" -ForegroundColor White
Write-Host "  - peer0.org2.example.com:9051" -ForegroundColor White
Write-Host ""

Write-Host "ORDERERS encontrados:" -ForegroundColor Green
Write-Host "  - orderer.example.com:7050" -ForegroundColor White
Write-Host ""

Write-Host "CERTIFICATE AUTHORITIES encontradas:" -ForegroundColor Green
Write-Host "  - https://ca.org1.example.com:7054" -ForegroundColor White
Write-Host "  - https://ca.org2.example.com:8054" -ForegroundColor White
Write-Host ""

Write-Host "TENTANDO OBTER INFORMACOES DETALHADAS:" -ForegroundColor Yellow
Write-Host "---------------------------------------" -ForegroundColor Yellow
Write-Host "MSP ID: Org1MSP" -ForegroundColor Green
Write-Host "Peer Address: peer0.org1.example.com:7051" -ForegroundColor Green
Write-Host ""

Write-Host "CANAIS encontrados:" -ForegroundColor Green
Write-Host "  - mychannel" -ForegroundColor White
Write-Host "  - businesschannel" -ForegroundColor White
Write-Host ""

Write-Host "CHAINCODES instalados:" -ForegroundColor Green
Write-Host "Package ID: auth-audit_1.0:abc123..." -ForegroundColor White
Write-Host "Label: auth-audit_1.0" -ForegroundColor White
Write-Host ""

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "RESUMO PARA CONFIGURACAO:" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "CONFIGURACAO SUGERIDA PARA application.yml:" -ForegroundColor Green
Write-Host "----------------------------------------------" -ForegroundColor Green
Write-Host ""

$config = @"
# Configuração para sua rede Hyperledger Fabric
blockchain:
  enabled: true
  network:
    type: hyperledger
    name: "production-network"
  hyperledger:
    # DADOS COLETADOS DA SUA REDE:
    channel: "mychannel"                    # Canal encontrado
    chaincode: "auth-audit"                 # Chaincode encontrado
    organization: "Org1MSP"                 # MSP ID detectado
    peer: "peer0.org1.example.com:7051"     # Peer principal
    ca-url: "https://ca.org1.example.com:7054"  # CA da organização
    orderer: "orderer.example.com:7050"     # Orderer da rede
    
    # Credenciais (AJUSTAR conforme necessário):
    user: "appUser"
    user-secret: "appUserSecret"
    admin-user: "admin"
    admin-secret: "adminpw"
    
    # Caminhos dos arquivos:
    connection-profile-path: "src/main/resources/connection-profile.yaml"
    wallet-path: "wallet"
    certificate-path: "/path/to/your/cert.pem"
    private-key-path: "/path/to/your/private.key"
    
    # Configurações de segurança:
    tls-enabled: true                       # Recomendado para produção
    simulation-mode: false                  # IMPORTANTE: false para dados reais
    
    # Configurações de conexão:
    connection-timeout: 30
    retry-attempts: 3
"@

Write-Host $config -ForegroundColor White
Write-Host ""

Write-Host "ARQUIVO DOCKER-COMPOSE ENCONTRADO:" -ForegroundColor Green
Write-Host "------------------------------------" -ForegroundColor Green
Write-Host "Servicos encontrados no docker-compose.yml:" -ForegroundColor Yellow
Write-Host "  - peer0.org1.example.com" -ForegroundColor White
Write-Host "  - peer0.org2.example.com" -ForegroundColor White
Write-Host "  - orderer.example.com" -ForegroundColor White
Write-Host "  - ca_org1" -ForegroundColor White
Write-Host "  - ca_org2" -ForegroundColor White
Write-Host "  - cli" -ForegroundColor White
Write-Host ""

Write-Host "PROXIMOS PASSOS PARA CONFIGURACAO REAL:" -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. INICIE O DOCKER DESKTOP" -ForegroundColor Yellow
Write-Host "   - Abra o Docker Desktop" -ForegroundColor White
Write-Host "   - Aguarde até que esteja rodando" -ForegroundColor White
Write-Host ""

Write-Host "2. INICIE SUA REDE HYPERLEDGER FABRIC" -ForegroundColor Yellow
Write-Host "   Se você não tem uma rede, use o test-network:" -ForegroundColor White
Write-Host "   git clone https://github.com/hyperledger/fabric-samples.git" -ForegroundColor Gray
Write-Host "   cd fabric-samples/test-network" -ForegroundColor Gray
Write-Host "   ./network.sh up createChannel -c mychannel -ca" -ForegroundColor Gray
Write-Host ""

Write-Host "3. EXECUTE O SCRIPT REAL" -ForegroundColor Yellow
Write-Host "   powershell -ExecutionPolicy Bypass -File scripts/collect-fabric-info-fixed.ps1" -ForegroundColor Gray
Write-Host ""

Write-Host "4. CONFIGURE OS ARQUIVOS" -ForegroundColor Yellow
Write-Host "   - Atualize api-author/src/main/resources/application.yml" -ForegroundColor White
Write-Host "   - Configure api-author/src/main/resources/connection-profile.yaml" -ForegroundColor White
Write-Host "   - Ajuste os caminhos dos certificados" -ForegroundColor White
Write-Host ""

Write-Host "5. TESTE A APLICACAO" -ForegroundColor Yellow
Write-Host "   - Compile: mvn clean install" -ForegroundColor White
Write-Host "   - Execute: mvn spring-boot:run" -ForegroundColor White
Write-Host "   - Verifique os logs para conexão com Hyperledger Fabric" -ForegroundColor White
Write-Host ""

Write-Host "ARQUIVOS DE CONFIGURACAO CRIADOS:" -ForegroundColor Green
Write-Host "-----------------------------------" -ForegroundColor Green
Write-Host "✓ GUIA_CONFIGURACAO_HYPERLEDGER.md" -ForegroundColor White
Write-Host "✓ scripts/collect-fabric-info-fixed.ps1" -ForegroundColor White
Write-Host "✓ api-author/src/main/resources/application-production.yml" -ForegroundColor White
Write-Host "✓ api-author/src/main/resources/connection-profile-production.yaml" -ForegroundColor White
Write-Host "✓ HyperledgerFabricConfig.java (atualizado)" -ForegroundColor White
Write-Host ""

Write-Host "OBSERVACOES IMPORTANTES:" -ForegroundColor Red
Write-Host "------------------------" -ForegroundColor Red
Write-Host "• Esta é uma DEMONSTRAÇÃO - os dados mostrados são simulados" -ForegroundColor Yellow
Write-Host "• Para usar dados reais, você precisa ter o Docker rodando" -ForegroundColor Yellow
Write-Host "• Execute o script real após iniciar sua rede Hyperledger Fabric" -ForegroundColor Yellow
Write-Host "• Ajuste todos os valores marcados com 'VERIFICAR' nas configurações" -ForegroundColor Yellow
Write-Host ""

Write-Host "Script de demonstração concluído!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan 