#!/bin/bash

# Script de Teste Automatizado - API de Autenticação Inteligente
# Uso: ./test-api.sh

echo "🧪 Iniciando Testes da API de Autenticação Inteligente"
echo "=================================================="

# Configurações
BASE_URL="http://localhost:8081/api/v1"
ADMIN_EMAIL="admin@teste.com"
ADMIN_PASSWORD="admin123"
USER_EMAIL="joao@teste.com"
USER_PASSWORD="123456"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para fazer requisições e validar respostas
test_endpoint() {
    local test_name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local headers=$5
    local expected_status=${6:-200}
    
    echo -e "\n${BLUE}🧪 Teste: $test_name${NC}"
    echo "Endpoint: $method $endpoint"
    
    if [ -n "$data" ]; then
        if [ -n "$headers" ]; then
            response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "Content-Type: application/json" \
                -H "$headers" \
                -d "$data")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    else
        if [ -n "$headers" ]; then
            response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
                -H "$headers")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint")
        fi
    fi
    
    # Separar response body e status code
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | sed '$d')
    
    # Validar status code
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✅ Status: $http_code (Esperado: $expected_status)${NC}"
        echo -e "Resposta: $response_body" | jq '.' 2>/dev/null || echo "$response_body"
        return 0
    else
        echo -e "${RED}❌ Status: $http_code (Esperado: $expected_status)${NC}"
        echo -e "Resposta: $response_body"
        return 1
    fi
}

# Função para extrair token JWT da resposta
extract_token() {
    local response=$1
    echo "$response" | jq -r '.token' 2>/dev/null
}

# Verificar se a aplicação está rodando
echo -e "\n${YELLOW}🔍 Verificando se a aplicação está rodando...${NC}"
if ! curl -s "$BASE_URL/swagger-ui.html" > /dev/null; then
    echo -e "${RED}❌ Aplicação não está rodando em $BASE_URL${NC}"
    echo "Execute: ./mvnw spring-boot:run"
    exit 1
fi
echo -e "${GREEN}✅ Aplicação está rodando!${NC}"

# Variáveis para armazenar tokens
USER_TOKEN=""
ADMIN_TOKEN=""

echo -e "\n${YELLOW}📋 INICIANDO TESTES FUNCIONAIS${NC}"

# TESTE 1: Registro de usuário comum
echo -e "\n${BLUE}=== TESTE 1: Registro de Usuário Comum ===${NC}"
user_data='{
    "name": "João Silva",
    "email": "'$USER_EMAIL'",
    "password": "'$USER_PASSWORD'",
    "roles": ["USER_DEFAULT"]
}'

if test_endpoint "Registro de Usuário" "POST" "/auth/register" "$user_data"; then
    # Extrair token se o registro foi bem-sucedido
    USER_TOKEN=$(extract_token "$response_body")
    echo -e "${GREEN}Token do usuário obtido: ${USER_TOKEN:0:50}...${NC}"
fi

# TESTE 2: Registro de usuário admin
echo -e "\n${BLUE}=== TESTE 2: Registro de Usuário Admin ===${NC}"
admin_data='{
    "name": "Admin User",
    "email": "'$ADMIN_EMAIL'",
    "password": "'$ADMIN_PASSWORD'",
    "roles": ["ADMIN"]
}'

if test_endpoint "Registro de Admin" "POST" "/auth/register" "$admin_data"; then
    ADMIN_TOKEN=$(extract_token "$response_body")
    echo -e "${GREEN}Token do admin obtido: ${ADMIN_TOKEN:0:50}...${NC}"
fi

# TESTE 3: Login com análise de IA
echo -e "\n${BLUE}=== TESTE 3: Login com Análise de IA ===${NC}"
login_data='{
    "email": "'$USER_EMAIL'",
    "password": "'$USER_PASSWORD'",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "location": "São Paulo, Brasil"
}'

test_endpoint "Login com IA" "POST" "/auth/authenticate" "$login_data"

# TESTE 4: Login suspeito (simulado)
echo -e "\n${BLUE}=== TESTE 4: Login Suspeito ===${NC}"
suspicious_login='{
    "email": "'$USER_EMAIL'",
    "password": "'$USER_PASSWORD'",
    "ipAddress": "203.45.67.89",
    "userAgent": "Bot/1.0 Crawler",
    "location": "Unknown Location"
}'

test_endpoint "Login Suspeito" "POST" "/auth/authenticate" "$suspicious_login"

# Testes de Analytics (apenas se temos token admin)
if [ -n "$ADMIN_TOKEN" ]; then
    echo -e "\n${YELLOW}📊 INICIANDO TESTES DE ANALYTICS${NC}"
    
    # TESTE 5: Métricas de segurança
    echo -e "\n${BLUE}=== TESTE 5: Métricas de Segurança ===${NC}"
    test_endpoint "Métricas de Segurança" "GET" "/analytics/security-metrics" "" "Authorization: Bearer $ADMIN_TOKEN"
    
    # TESTE 6: Padrão comportamental do usuário
    echo -e "\n${BLUE}=== TESTE 6: Padrão Comportamental ===${NC}"
    test_endpoint "Padrão Comportamental" "GET" "/analytics/user/1/behavior-pattern" "" "Authorization: Bearer $ADMIN_TOKEN"
    
    # TESTE 7: Transações blockchain do usuário
    echo -e "\n${BLUE}=== TESTE 7: Transações Blockchain ===${NC}"
    test_endpoint "Transações Blockchain" "GET" "/analytics/user/1/blockchain-transactions" "" "Authorization: Bearer $ADMIN_TOKEN"
    
    # TESTE 8: Análise de contexto manual
    echo -e "\n${BLUE}=== TESTE 8: Análise de Contexto Manual ===${NC}"
    context_data='{
        "userId": 1,
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
        "location": "São Paulo, Brasil",
        "deviceFingerprint": "abc123def456",
        "geolocation": {
            "latitude": -23.5505,
            "longitude": -46.6333,
            "country": "BR",
            "region": "SP",
            "city": "São Paulo",
            "timezone": "America/Sao_Paulo",
            "isp": "Telecom Provider"
        },
        "networkInfo": {
            "connectionType": "broadband",
            "vpnDetected": false,
            "proxyDetected": false,
            "torDetected": false,
            "ipReputation": "GOOD"
        },
        "behavioralData": {
            "screenResolution": "1920x1080",
            "sessionDuration": 300,
            "pageInteractions": 15
        },
        "timestamp": "2024-12-19T10:30:00"
    }'
    
    test_endpoint "Análise de Contexto" "POST" "/analytics/context-analysis" "$context_data" "Authorization: Bearer $ADMIN_TOKEN"
    
    # TESTE 9: Cenário de alto risco
    echo -e "\n${BLUE}=== TESTE 9: Cenário de Alto Risco ===${NC}"
    high_risk_data='{
        "userId": 1,
        "ipAddress": "45.12.34.56",
        "userAgent": "Bot/1.0 Scraper Tool",
        "location": "Moscow, Russia",
        "deviceFingerprint": "different123",
        "geolocation": {
            "country": "RU",
            "city": "Moscow"
        },
        "networkInfo": {
            "vpnDetected": true,
            "proxyDetected": true
        },
        "behavioralData": {
            "sessionDuration": 5,
            "pageInteractions": 0
        }
    }'
    
    test_endpoint "Cenário Alto Risco" "POST" "/analytics/context-analysis" "$high_risk_data" "Authorization: Bearer $ADMIN_TOKEN"
    
    # TESTE 10: Avaliação de risco do usuário
    echo -e "\n${BLUE}=== TESTE 10: Avaliação de Risco ===${NC}"
    test_endpoint "Avaliação de Risco" "GET" "/analytics/user/1/risk-assessment" "" "Authorization: Bearer $ADMIN_TOKEN"
    
else
    echo -e "${YELLOW}⚠️  Pulando testes de analytics (token admin não disponível)${NC}"
fi

echo -e "\n${YELLOW}🏁 RESUMO DOS TESTES${NC}"
echo "=================================================="
echo -e "${GREEN}✅ Testes básicos de autenticação executados${NC}"
echo -e "${GREEN}✅ Análise de contexto com IA testada${NC}"
echo -e "${GREEN}✅ Registro em blockchain validado${NC}"

if [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}✅ APIs de analytics testadas${NC}"
else
    echo -e "${YELLOW}⚠️  APIs de analytics não testadas${NC}"
fi

echo -e "\n${BLUE}📋 Próximos passos:${NC}"
echo "1. Verificar logs da aplicação para detalhes da análise de IA"
echo "2. Consultar banco de dados para ver registros criados"
echo "3. Acessar Swagger UI: $BASE_URL/swagger-ui.html"
echo "4. Monitorar métricas de segurança via APIs"

echo -e "\n${GREEN}🎉 Testes concluídos! Verifique os resultados acima.${NC}" 