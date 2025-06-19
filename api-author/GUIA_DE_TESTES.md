# 🧪 Guia de Testes - API de Autenticação Inteligente

## 🚀 Preparação do Ambiente

### 1. **Configuração do Banco de Dados**

```bash
# PostgreSQL via Docker
docker run --name auth-postgres -e POSTGRES_DB=auth_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:13

# Ou configure suas variáveis de ambiente
export DB_URL=jdbc:postgresql://localhost:5432/auth_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

### 2. **Executar a Aplicação**

```bash
# Via Maven
cd api-author
./mvnw spring-boot:run

# Ou via JAR
./mvnw clean package
java -jar target/auth-service-1.0.0.jar
```

### 3. **Verificar Inicialização**

```bash
# A aplicação estará rodando em:
http://localhost:8081/api/v1

# Swagger UI disponível em:
http://localhost:8081/api/v1/swagger-ui.html
```

## 📋 Testes Funcionais

### **TESTE 1: Registro de Usuário**

```bash
curl -X POST "http://localhost:8081/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@teste.com",
    "password": "123456",
    "roles": ["USER_DEFAULT"]
  }'
```

**Resultado Esperado:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "name": "João Silva",
  "login": "joao@teste.com"
}
```

### **TESTE 2: Login com Análise de IA**

```bash
curl -X POST "http://localhost:8081/api/v1/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@teste.com",
    "password": "123456",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "location": "São Paulo, Brasil"
  }'
```

**Resultado Esperado:**
- Login bem-sucedido com token JWT
- Logs da análise de IA no console
- Registro automático na blockchain simulada

**Verifique os Logs:**
```
[AI Context Analysis] Starting context analysis for user joao@teste.com
[AI Context Analysis] Overall risk score: 0.25 (LOW)
[AI Context Analysis] Decision: ALLOW
[Blockchain] Authentication event recorded with hash: 0x...
```

### **TESTE 3: Login Suspeito (Simulado)**

```bash
# Login em horário suspeito (madrugada)
# Execute este teste entre 23h e 5h da manhã
curl -X POST "http://localhost:8081/api/v1/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@teste.com",
    "password": "123456",
    "ipAddress": "203.45.67.89",
    "userAgent": "Bot/1.0 Crawler",
    "location": "Unknown Location"
  }'
```

**Resultado Esperado:**
- Score de risco mais alto
- Possível requisição de MFA
- Logs de atividade suspeita

## 🤖 Testando APIs de Analytics

### **TESTE 4: Obter Padrão Comportamental**

```bash
# Primeiro, obtenha um token de admin (registre um usuário admin)
curl -X POST "http://localhost:8081/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin",
    "email": "admin@teste.com",
    "password": "admin123",
    "roles": ["ADMIN"]
  }'

# Faça login para obter o token
curl -X POST "http://localhost:8081/api/v1/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@teste.com",
    "password": "admin123",
    "ipAddress": "127.0.0.1",
    "userAgent": "Admin Console",
    "location": "Local"
  }'

# Use o token para acessar analytics (substitua o TOKEN)
curl -X GET "http://localhost:8081/api/v1/analytics/user/1/behavior-pattern" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### **TESTE 5: Métricas de Segurança**

```bash
curl -X GET "http://localhost:8081/api/v1/analytics/security-metrics" \
  -H "Authorization: Bearer SEU_TOKEN_ADMIN"
```

**Resultado Esperado:**
```json
{
  "totalUsers": 2,
  "averageRiskScore": 0.35,
  "highRiskUsers": 0,
  "highRiskTransactions": 0,
  "unverifiedTransactions": 2,
  "blockchainIntegrity": "GOOD"
}
```

### **TESTE 6: Transações Blockchain do Usuário**

```bash
curl -X GET "http://localhost:8081/api/v1/analytics/user/1/blockchain-transactions" \
  -H "Authorization: Bearer SEU_TOKEN_ADMIN"
```

### **TESTE 7: Análise de Contexto Manual**

```bash
curl -X POST "http://localhost:8081/api/v1/analytics/context-analysis" \
  -H "Authorization: Bearer SEU_TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
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
      "pageInteractions": 15,
      "typingPatterns": {
        "avgKeyInterval": 150,
        "totalKeys": 50
      }
    },
    "timestamp": "2024-12-19T10:30:00"
  }'
```

**Resultado Esperado:**
```json
{
  "analysisId": "uuid-analysis-id",
  "userId": 1,
  "overallRiskScore": 0.25,
  "riskLevel": "LOW",
  "decision": "ALLOW",
  "confidenceScore": 0.8,
  "analysisDetails": {
    "locationAnalysis": {
      "consistencyScore": 0.8,
      "distanceFromUsual": 0.0,
      "isKnownLocation": true,
      "countryRiskLevel": "LOW",
      "flags": []
    },
    "deviceAnalysis": {
      "consistencyScore": 0.8,
      "isKnownDevice": true,
      "deviceReputation": "UNKNOWN",
      "browserIntegrity": 0.9,
      "flags": []
    }
  },
  "recommendations": [],
  "processedAt": "2024-12-19T10:30:05"
}
```

## 🔍 Cenários de Teste Avançados

### **CENÁRIO 1: Login de Localização Suspeita**

```bash
# Simula login de outro país
curl -X POST "http://localhost:8081/api/v1/analytics/context-analysis" \
  -H "Authorization: Bearer SEU_TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "ipAddress": "45.12.34.56",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    "location": "Moscow, Russia",
    "deviceFingerprint": "different123",
    "geolocation": {
      "country": "RU",
      "city": "Moscow"
    },
    "networkInfo": {
      "vpnDetected": true
    }
  }'
```

**Resultado Esperado:**
- Score de risco alto (>0.6)
- Decision: "REQUIRE_MFA" ou "DENY"
- Flags: ["NEW_LOCATION", "HIGH_RISK_COUNTRY", "VPN_DETECTED"]

### **CENÁRIO 2: Atividade de Bot Suspeita**

```bash
curl -X POST "http://localhost:8081/api/v1/analytics/context-analysis" \
  -H "Authorization: Bearer SEU_TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "ipAddress": "192.168.1.100",
    "userAgent": "Bot/1.0 Scraper Tool",
    "location": "São Paulo, Brasil",
    "behavioralData": {
      "sessionDuration": 5,
      "pageInteractions": 0
    },
    "networkInfo": {
      "proxyDetected": true
    }
  }'
```

## 📊 Interpretando os Resultados

### **Scores de Risco:**
- **0.0 - 0.3**: 🟢 Baixo Risco (ALLOW)
- **0.3 - 0.6**: 🟡 Médio Risco (REQUIRE_MFA)
- **0.6 - 0.8**: 🟠 Alto Risco (REQUIRE_ADDITIONAL_VERIFICATION)
- **0.8 - 1.0**: 🔴 Crítico (DENY)

### **Flags Comuns:**
- `NEW_LOCATION`: Nova localização detectada
- `NEW_DEVICE`: Novo dispositivo
- `LATE_NIGHT_ACCESS`: Acesso em horário incomum
- `VPN_DETECTED`: VPN detectada
- `HIGH_RISK_COUNTRY`: País de alto risco

### **Status de Blockchain:**
- `PENDING`: Transação enviada, aguardando confirmação
- `CONFIRMED`: Transação confirmada na blockchain
- `FAILED`: Falha na transação

## 🛠️ Testes de Desenvolvimento

### **Verificar Logs da Aplicação:**
```bash
# Acompanhar logs em tempo real
tail -f logs/application.log

# Ou via Docker
docker logs -f nome-do-container
```

### **Verificar Banco de Dados:**
```sql
-- Padrões comportamentais
SELECT * FROM user_behavior_patterns;

-- Transações blockchain
SELECT * FROM blockchain_transactions ORDER BY created_at DESC;

-- Logs de auditoria
SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 10;
```

### **Testar com Postman/Insomnia:**

1. Importe a collection do Swagger: `http://localhost:8081/api/v1/api-docs`
2. Configure variáveis de ambiente:
   - `base_url`: `http://localhost:8081/api/v1`
   - `admin_token`: Token do usuário admin
3. Execute os testes em sequência

## ✅ Checklist de Testes

### **Funcionalidades Básicas:**
- [ ] Registro de usuário funciona
- [ ] Login básico funciona
- [ ] JWT é gerado corretamente
- [ ] Logs de auditoria são criados

### **IA e Análise de Contexto:**
- [ ] Análise de contexto é executada no login
- [ ] Scores de risco são calculados corretamente
- [ ] Decisões da IA são aplicadas
- [ ] Padrões comportamentais são atualizados

### **Blockchain:**
- [ ] Transações são registradas automaticamente
- [ ] Hashes são gerados corretamente
- [ ] Status de confirmação é atualizado
- [ ] Consultas de transações funcionam

### **APIs de Analytics:**
- [ ] Todas as APIs retornam dados
- [ ] Autenticação admin funciona
- [ ] Métricas são calculadas corretamente
- [ ] Relatórios de risco são precisos

## 🐛 Resolução de Problemas

### **Erro de Conexão com Banco:**
```bash
# Verificar se PostgreSQL está rodando
docker ps | grep postgres

# Testar conexão
psql -h localhost -p 5432 -U postgres -d auth_db
```

### **Erro 403 (Forbidden):**
- Verificar se o token JWT está correto
- Confirmar que o usuário tem role ADMIN para analytics

### **Blockchain não funciona:**
- É normal no modo simulado
- Para blockchain real, configure as variáveis no application.yml

### **Performance lenta:**
- Verificar logs de análise de IA
- Considerar ajustar thresholds de risco

---

**🎯 Com estes testes você pode validar todas as funcionalidades de autenticação inteligente implementadas!** 