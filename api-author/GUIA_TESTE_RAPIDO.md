# 🚀 Guia de Teste Rápido - Módulo de IA

## ⚡ Início Rápido

### 1. **Iniciar o Servidor**

```bash
cd api-author

# Modo Desenvolvimento (sem autenticação)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ou modo padrão (com autenticação)
mvn spring-boot:run
```

**Servidor rodará em:** `http://localhost:8081/api/v1`

### 2. **Verificar se está funcionando**

```powershell
# Teste básico
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/swagger-ui.html" -Method GET
```

## 🧪 Testes Essenciais

### **Modo Desenvolvimento (Profile: dev)**

Se iniciou com `-Dspring-boot.run.profiles=dev`:

```powershell
# 1. Treinar modelos de IA
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/test/ia/treinar" -Method POST

# 2. Testar score
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/test/ia/testar-score" -Method GET

# 3. Análise comportamental
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/test/ia/simular-analise/1" -Method GET

# 4. Estatísticas
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/test/ia/estatisticas" -Method GET
```

### **Modo Produção (com autenticação)**

Se iniciou sem perfil dev, precisa autenticar:

```powershell
# 1. Registrar usuário
$registerBody = @{
    name = "Usuario Teste"
    email = "teste@exemplo.com"
    password = "senha123"
} | ConvertTo-Json

$user = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/register" -Method POST -Body $registerBody -ContentType "application/json"

# 2. Fazer login
$loginBody = @{
    email = "teste@exemplo.com"
    password = "senha123"
    ipAddress = "192.168.1.100"
    userAgent = "PowerShell Test"
    deviceFingerprint = "test-device"
    location = "São Paulo, SP"
} | ConvertTo-Json

$auth = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/authenticate" -Method POST -Body $loginBody -ContentType "application/json"

# 3. Usar token para testes (substitua SEU_TOKEN)
$headers = @{ Authorization = "Bearer $($auth.token)" }
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/test/ia/treinar" -Method POST -Headers $headers
```

## 🔧 Resolução de Problemas

### **Erro 500 - Servidor não responde**

1. **Verificar logs do servidor** no terminal onde executou `mvn spring-boot:run`
2. **Banco H2 não inicializou** - Reinicie o servidor
3. **Dependências não baixadas** - Execute `mvn clean install`

### **Erro 401 - Não autorizado**

- Certifique-se de usar o **perfil dev**: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- Ou obtenha um token de autenticação conforme mostrado acima

### **Erro de conexão**

```powershell
# Verificar se servidor está rodando
netstat -an | findstr :8081

# Se não estiver, reiniciar
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📊 Endpoints Principais

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/test/ia/treinar` | POST | Treina todos os modelos |
| `/test/ia/testar-score` | GET | Testa score com dados simulados |
| `/test/ia/simular-analise/1` | GET | Simula análise para usuário |
| `/test/ia/estatisticas` | GET | Estatísticas de anomalias |
| `/auth/register` | POST | Registra usuário |
| `/auth/authenticate` | POST | Autentica usuário |
| `/swagger-ui.html` | GET | Interface Swagger |
| `/h2-console` | GET | Console do banco H2 (dev) |

## 🎯 Resultado Esperado

### **Treinamento bem-sucedido:**
```json
{
  "isolationForest": "Modelo treinado com sucesso",
  "randomForest": "Modelo treinado com sucesso", 
  "deepLearning": "Modelo treinado com sucesso"
}
```

### **Score de teste:**
```json
{
  "scoreFinal": 0.35,
  "classificacao": "ESPERADO",
  "confianca": 0.85,
  "algoritmo": "ENSEMBLE"
}
```

### **Análise comportamental:**
```json
{
  "usuarioId": 1,
  "scoreIsolationForest": 0.25,
  "scoreRandomForest": 0.30,
  "scoreDeepLearning": 0.20,
  "scoreEnsemble": 0.35,
  "classificacao": "ESPERADO"
}
```

## 🌐 Interfaces Web

- **Swagger UI:** http://localhost:8081/api/v1/swagger-ui.html
- **H2 Console:** http://localhost:8081/api/v1/h2-console (dev mode)
  - URL: `jdbc:h2:mem:testdb`
  - User: `sa`
  - Password: (vazio)

---

## 📞 Suporte Rápido

**Problema comum:** Erro 500 nos endpoints de IA
**Solução:** Reinicie o servidor e aguarde a inicialização completa

**Comando de teste rápido:**
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Aguarde ver: `Started AuthServiceApplication in X.XXX seconds`

## 🌍 **Testes de Geolocalização**

### **Script Específico de Geolocalização**
```powershell
cd api-author
.\test-geolocalizacao.ps1
```

### **Testes Manuais de Geolocalização**
```powershell
# Geolocalização por IP
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/geo/ip/8.8.8.8" -Method GET

# Coordenadas por endereço
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/geo/endereco?endereco=São Paulo, Brasil" -Method GET

# Distância entre coordenadas
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/geo/distancia?lat1=-23.5505&lon1=-46.6333&lat2=-22.9068&lon2=-43.1729" -Method GET

# Verificar país de risco
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/geo/pais-risco/CN" -Method GET

# Teste completo
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/geo/teste-completo" -Method GET
```

### **Resultados Esperados**

#### **Geolocalização por IP:**
```json
{
  "latitude": 37.751,
  "longitude": -97.822,
  "cidade": "United States",
  "pais": "United States",
  "fonte": "IP_API",
  "precisao": "MEDIA"
}
```

#### **Cálculo de Distância:**
```json
{
  "ponto1": {"latitude": -23.5505, "longitude": -46.6333},
  "ponto2": {"latitude": -22.9068, "longitude": -43.1729},
  "distanciaKm": 357.28,
  "distanciaMilhas": 222.02
}
``` 