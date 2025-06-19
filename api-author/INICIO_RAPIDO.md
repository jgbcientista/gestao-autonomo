# 🚀 Início Rápido - Testes da API

## 🎯 Como Testar (3 Passos Simples)

### **Passo 1: Configurar Banco de Dados**

```bash
# PostgreSQL via Docker (Windows/Linux/Mac)
docker run --name auth-postgres -e POSTGRES_DB=auth_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:13
```

### **Passo 2: Executar a Aplicação**

```bash
# Entrar na pasta do projeto
cd api-author

# Executar via Maven (Windows/Linux/Mac)
./mvnw spring-boot:run

# Windows PowerShell (alternativa)
.\mvnw.cmd spring-boot:run
```

### **Passo 3: Testar**

#### **🤖 OPÇÃO A: Script Automatizado (Linux/Mac)**
```bash
# Tornar executável
chmod +x test-api.sh

# Executar todos os testes
./test-api.sh
```

#### **🖥️ OPÇÃO B: Testes Manuais (Windows/Todos)**

**1. Registrar Usuário:**
```bash
curl -X POST "http://localhost:8081/api/v1/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"João Silva\",\"email\":\"joao@teste.com\",\"password\":\"123456\",\"roles\":[\"USER_DEFAULT\"]}"
```

**2. Login com Análise de IA:**
```bash
curl -X POST "http://localhost:8081/api/v1/auth/authenticate" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"joao@teste.com\",\"password\":\"123456\",\"ipAddress\":\"192.168.1.100\",\"userAgent\":\"Mozilla/5.0\",\"location\":\"São Paulo\"}"
```

**3. Registrar Admin:**
```bash
curl -X POST "http://localhost:8081/api/v1/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Admin\",\"email\":\"admin@teste.com\",\"password\":\"admin123\",\"roles\":[\"ADMIN\"]}"
```

**4. Obter Métricas (substitua SEU_TOKEN):**
```bash
curl -X GET "http://localhost:8081/api/v1/analytics/security-metrics" ^
  -H "Authorization: Bearer SEU_TOKEN_ADMIN"
```

#### **🌐 OPÇÃO C: Interface Swagger (Mais Fácil)**

1. Abrir no navegador: **http://localhost:8081/api/v1/swagger-ui.html**
2. Testar endpoints diretamente na interface
3. Ver documentação completa

## ✅ O que Esperar

### **Logs da Aplicação:**
```
[AI Context Analysis] Starting context analysis for user joao@teste.com
[AI Context Analysis] Overall risk score: 0.25 (LOW)
[AI Context Analysis] Decision: ALLOW
[Blockchain] Authentication event recorded with hash: 0x1234...
```

### **Resposta de Login:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "name": "João Silva", 
  "login": "joao@teste.com"
}
```

### **Métricas de Segurança:**
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

## 🔍 Verificações Rápidas

### **1. Aplicação Rodando:**
- ✅ http://localhost:8081/api/v1/swagger-ui.html abre
- ✅ Logs mostram "Started AuthServiceApplication"

### **2. Banco de Dados:**
```bash
# Verificar se PostgreSQL está rodando
docker ps | grep postgres

# Conectar ao banco
docker exec -it auth-postgres psql -U postgres -d auth_db
```

### **3. Funcionalidades:**
- ✅ Registro de usuário funciona
- ✅ Login retorna JWT token
- ✅ Logs mostram análise de IA
- ✅ Registros de blockchain são criados

## 🐛 Problemas Comuns

### **Erro de Porta:**
```bash
# Se porta 8081 estiver ocupada, mude no application.yml:
server:
  port: 8082
```

### **Erro de Banco:**
```bash
# Verificar se Docker está rodando
docker --version

# Resetar banco se necessário
docker rm -f auth-postgres
```

### **Erro de Token:**
- Gerar novo token fazendo login
- Verificar se usuário tem role ADMIN para analytics

## 🎯 Cenários de Teste Interessantes

### **1. Login Normal (Baixo Risco):**
- IP doméstico: 192.168.x.x
- User-Agent normal: Mozilla/Chrome
- Horário comercial: 9h-18h

### **2. Login Suspeito (Alto Risco):**
- IP estrangeiro: 45.123.x.x
- User-Agent: Bot/Crawler
- Horário: 2h da manhã
- Localização: Russia/China

### **3. Análise Blockchain:**
- Verificar transações criadas
- Confirmar hashes únicos
- Validar timestamps

## 📊 Resultados Esperados

| Cenário | Risk Score | Decisão | Flags |
|---------|------------|---------|-------|
| Login Normal | 0.1-0.3 | ALLOW | [] |
| Nova Localização | 0.4-0.6 | REQUIRE_MFA | [NEW_LOCATION] |
| VPN + Bot | 0.7-0.9 | DENY | [VPN_DETECTED, SUSPICIOUS_USER_AGENT] |

---

**🎉 Pronto! Com estes passos você pode testar todas as funcionalidades de autenticação inteligente implementadas!**

**💡 Dica:** Use o Swagger UI para testes mais fáceis e visuais! 