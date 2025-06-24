#!/bin/bash

# Script para coletar informações da rede Hyperledger Fabric
# Execute este script para descobrir os dados necessários para configuração

echo "=================================================="
echo "🔍 COLETANDO INFORMAÇÕES DA REDE HYPERLEDGER FABRIC"
echo "=================================================="
echo ""

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

echo "✅ Docker está rodando"
echo ""

# 1. Listar containers do Hyperledger Fabric
echo "📋 CONTAINERS HYPERLEDGER FABRIC EM EXECUÇÃO:"
echo "--------------------------------------------"
FABRIC_CONTAINERS=$(docker ps --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}" | grep -E "(peer|orderer|ca)")

if [ -z "$FABRIC_CONTAINERS" ]; then
    echo "❌ Nenhum container Hyperledger Fabric encontrado em execução."
    echo "💡 Você precisa iniciar sua rede Hyperledger Fabric primeiro."
    echo ""
    echo "Se você não tem uma rede, pode usar o test-network:"
    echo "git clone https://github.com/hyperledger/fabric-samples.git"
    echo "cd fabric-samples/test-network"
    echo "./network.sh up createChannel -c mychannel -ca"
    echo ""
    exit 1
else
    echo "$FABRIC_CONTAINERS"
fi
echo ""

# 2. Extrair informações dos containers
echo "🔍 ANALISANDO CONTAINERS:"
echo "------------------------"

# Peers
PEERS=$(docker ps --format "{{.Names}}" | grep "^peer")
if [ ! -z "$PEERS" ]; then
    echo "📡 PEERS encontrados:"
    for peer in $PEERS; do
        PORT=$(docker port $peer 2>/dev/null | grep "7051" | cut -d':' -f2 | head -1)
        if [ -z "$PORT" ]; then PORT="7051"; fi
        echo "  - $peer:$PORT"
    done
else
    echo "❌ Nenhum peer encontrado"
fi
echo ""

# Orderers
ORDERERS=$(docker ps --format "{{.Names}}" | grep "orderer")
if [ ! -z "$ORDERERS" ]; then
    echo "📦 ORDERERS encontrados:"
    for orderer in $ORDERERS; do
        PORT=$(docker port $orderer 2>/dev/null | grep "7050" | cut -d':' -f2 | head -1)
        if [ -z "$PORT" ]; then PORT="7050"; fi
        echo "  - $orderer:$PORT"
    done
else
    echo "❌ Nenhum orderer encontrado"
fi
echo ""

# Certificate Authorities
CAS=$(docker ps --format "{{.Names}}" | grep "ca")
if [ ! -z "$CAS" ]; then
    echo "🔐 CERTIFICATE AUTHORITIES encontradas:"
    for ca in $CAS; do
        PORT=$(docker port $ca 2>/dev/null | grep "7054" | cut -d':' -f2 | head -1)
        if [ -z "$PORT" ]; then PORT="7054"; fi
        echo "  - https://$ca:$PORT"
    done
else
    echo "❌ Nenhuma CA encontrada"
fi
echo ""

# 3. Tentar obter informações detalhadas se possível
echo "🔍 TENTANDO OBTER INFORMAÇÕES DETALHADAS:"
echo "---------------------------------------"

# Verificar se temos variáveis de ambiente configuradas
if [ ! -z "$CORE_PEER_LOCALMSPID" ]; then
    echo "✅ MSP ID: $CORE_PEER_LOCALMSPID"
fi

if [ ! -z "$CORE_PEER_ADDRESS" ]; then
    echo "✅ Peer Address: $CORE_PEER_ADDRESS"
fi

# Tentar listar canais (pode falhar se não houver variáveis de ambiente)
echo ""
echo "📺 TENTANDO LISTAR CANAIS:"
CHANNELS=$(peer channel list 2>/dev/null | grep "Channel Name:" | cut -d':' -f2 | tr -d ' ')
if [ ! -z "$CHANNELS" ]; then
    echo "✅ Canais encontrados:"
    echo "$CHANNELS" | while read channel; do
        echo "  - $channel"
    done
else
    echo "⚠️  Não foi possível listar canais (configure as variáveis de ambiente do peer)"
fi

echo ""
echo "🔗 TENTANDO LISTAR CHAINCODES:"
CHAINCODES=$(peer lifecycle chaincode queryinstalled 2>/dev/null)
if [ $? -eq 0 ]; then
    echo "✅ Chaincodes instalados:"
    echo "$CHAINCODES"
else
    echo "⚠️  Não foi possível listar chaincodes (configure as variáveis de ambiente do peer)"
fi

echo ""
echo "=================================================="
echo "📝 RESUMO PARA CONFIGURAÇÃO:"
echo "=================================================="

# Gerar configuração baseada no que foi encontrado
FIRST_PEER=$(echo "$PEERS" | head -1)
FIRST_ORDERER=$(echo "$ORDERERS" | head -1)
FIRST_CA=$(echo "$CAS" | head -1)

if [ ! -z "$FIRST_PEER" ] && [ ! -z "$FIRST_ORDERER" ] && [ ! -z "$FIRST_CA" ]; then
    echo ""
    echo "📄 CONFIGURAÇÃO SUGERIDA PARA application.yml:"
    echo "----------------------------------------------"
    cat << EOF
blockchain:
  enabled: true
  network:
    type: hyperledger
  hyperledger:
    channel: "mychannel"  # ⚠️ VERIFICAR - canal padrão
    chaincode: "auth-audit"  # ⚠️ VERIFICAR - nome do seu chaincode
    organization: "Org1MSP"  # ⚠️ VERIFICAR - sua organização
    peer: "$FIRST_PEER:7051"
    ca-url: "https://$FIRST_CA:7054"
    orderer: "$FIRST_ORDERER:7050"
    user: "appUser"  # ⚠️ VERIFICAR - usuário da aplicação
    user-secret: "appUserSecret"  # ⚠️ VERIFICAR - senha do usuário
    admin-user: "admin"
    admin-secret: "adminpw"
    connection-profile-path: "src/main/resources/connection-profile.yaml"
    wallet-path: "wallet"
    tls-enabled: false  # ⚠️ AJUSTAR conforme sua rede
    simulation-mode: false  # IMPORTANTE: false para usar dados reais
EOF
    echo ""
    echo "⚠️  ATENÇÃO: Os valores marcados com ⚠️ precisam ser verificados/ajustados!"
    echo ""
fi

echo "🔧 PRÓXIMOS PASSOS:"
echo "------------------"
echo "1. Configure as variáveis de ambiente do peer para obter mais informações:"
echo "   export CORE_PEER_LOCALMSPID=Org1MSP"
echo "   export CORE_PEER_ADDRESS=$FIRST_PEER:7051"
echo "   export CORE_PEER_MSPCONFIGPATH=/path/to/msp"
echo ""
echo "2. Execute novamente este script para obter informações completas"
echo ""
echo "3. Verifique os arquivos de configuração da sua rede:"
echo "   - docker-compose.yaml"
echo "   - configtx.yaml"
echo "   - crypto-config.yaml"
echo ""
echo "4. Atualize o application.yml com os dados corretos"
echo ""
echo "5. Configure o connection-profile.yaml"
echo ""

# Verificar se existe docker-compose na pasta atual
if [ -f "docker-compose.yaml" ] || [ -f "docker-compose.yml" ]; then
    echo "📄 ARQUIVO DOCKER-COMPOSE ENCONTRADO:"
    echo "------------------------------------"
    echo "Analisando configurações..."
    
    # Extrair informações do docker-compose
    COMPOSE_FILE="docker-compose.yaml"
    if [ ! -f "$COMPOSE_FILE" ]; then
        COMPOSE_FILE="docker-compose.yml"
    fi
    
    echo "🔍 Serviços encontrados no $COMPOSE_FILE:"
    grep -E "^\s+[a-zA-Z0-9_-]+:" "$COMPOSE_FILE" | sed 's/://g' | sed 's/^[ ]*/  - /'
fi

echo ""
echo "✅ Script concluído! Use as informações acima para configurar sua aplicação."
echo "==================================================" 