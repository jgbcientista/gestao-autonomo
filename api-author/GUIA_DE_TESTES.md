# 🧪 Guia Completo de Testes - Módulo de IA

Este guia apresenta todas as formas de testar o Módulo de IA implementado no sistema de autenticação.

## 📋 Pré-requisitos

1. **Java 17+** instalado
2. **Maven 3.6+** instalado
3. **Servidor iniciado**: Execute `mvn spring-boot:run` no diretório `api-author`
4. **Porta 8080** disponível

## 🚀 Iniciando o Servidor

```bash
cd api-author
mvn spring-boot:run
```

Aguarde a mensagem: `Started AuthServiceApplication in X.XXX seconds`

## 🛠️ Métodos de Teste

### 1. **Script PowerShell Automatizado**

Execute o script de teste automatizado:

```powershell
cd api-author
.\test-simples.ps1
```

**O que o script testa:**
- ✅ Conectividade com o servidor
- 🤖 Treinamento dos modelos de IA
- 📊 Cálculo de scores
- 🔬 Análise comportamental

### 2. **Requisições HTTP (REST Client)**

Use o arquivo `test-requests.http` com:
- **VS Code REST Client Extension**
- **Postman**
- **Insomnia**

### 3. **Interface Swagger**

Acesse: `http://localhost:8080/swagger-ui.html`

Explore todos os endpoints disponíveis com interface gráfica.

### 4. **Testes via cURL**

```bash
# Health Check
curl -X GET http://localhost:8080/api/v1/health

# Treinar Modelos
curl -X POST http://localhost:8080/api/v1/test/ia/treinar

# Testar Score
curl -X GET http://localhost:8080/api/v1/test/ia/testar-score

# Análise Comportamental
curl -X GET http://localhost:8080/api/v1/test/ia/simular-analise/1
```

## 🎯 Cenários de Teste Específicos

### **Cenário 1: Comportamento Normal**
```json
POST /api/v1/auth/authenticate
{
    "email": "usuario@exemplo.com",
    "password": "senha123",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    "deviceFingerprint": "device-normal",
    "location": "São Paulo, SP"
}
```
**Resultado esperado:** Classificação `ESPERADO`, score < 0.5

### **Cenário 2: Comportamento Suspeito**
```json
POST /api/v1/auth/authenticate
{
    "email": "usuario@exemplo.com",
    "password": "senha123",
    "ipAddress": "203.0.113.1",
    "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)",
    "deviceFingerprint": "device-diferente",
    "location": "Rio de Janeiro, RJ"
}
```
**Resultado esperado:** Classificação `SUSPEITO`, score 0.5-0.7

### **Cenário 3: Comportamento Anômalo**
```json
POST /api/v1/auth/authenticate
{
    "email": "usuario@exemplo.com",
    "password": "senha123",
    "ipAddress": "8.8.8.8",
    "userAgent": "curl/7.68.0",
    "deviceFingerprint": "device-desconhecido",
    "location": "Tokyo, Japan"
}
```
**Resultado esperado:** Classificação `ANOMALO` ou `ALTAMENTE_SUSPEITO`, score > 0.7

## 📊 Endpoints de Teste Disponíveis

### **Endpoints Básicos**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/v1/health` | Status do servidor |
| POST | `/api/v1/test/ia/treinar` | Treinar modelos |
| GET | `/api/v1/test/ia/testar-score` | Testar com dados simulados |
| GET | `/api/v1/test/ia/simular-analise/{id}` | Análise para usuário específico |
| GET | `/api/v1/test/ia/estatisticas` | Estatísticas de anomalias |

### **Endpoints de Produção**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/ia/analisar-comportamento` | Análise comportamental |
| POST | `/api/v1/ia/feedback` | Feedback para melhoria |
| GET | `/api/v1/ia/historico/{id}` | Histórico de análises |
| POST | `/api/v1/ia/retreinar-modelo` | Retreinar modelo |
| GET | `/api/v1/ia/metricas-performance` | Métricas de performance |

## 🔍 Validando os Resultados

### **1. Scores Esperados**
- **Normal:** 0.0 - 0.5
- **Suspeito:** 0.5 - 0.7  
- **Anômalo:** 0.7 - 0.9
- **Altamente Suspeito:** 0.9 - 1.0

### **2. Classificações**
- `ESPERADO`: Comportamento normal, acesso permitido
- `SUSPEITO`: Comportamento questionável, log de auditoria
- `ANOMALO`: Comportamento anômalo, requer MFA
- `ALTAMENTE_SUSPEITO`: Comportamento crítico, acesso bloqueado

### **3. Algoritmos**
- **Isolation Forest**: Detecção não supervisionada de outliers
- **Random Forest**: Classificação supervisionada
- **Deep Learning**: Rede neural para padrões complexos
- **Ensemble**: Combinação ponderada dos 3 algoritmos

## 🐛 Troubleshooting

### **Erro: Servidor não responde**
```bash
# Verificar se o processo está rodando
netstat -an | findstr :8080

# Reiniciar o servidor
mvn spring-boot:run
```

### **Erro: Dependências não encontradas**
```bash
# Limpar e recompilar
mvn clean compile

# Baixar dependências
mvn dependency:resolve
```

### **Erro: Modelos não treinados**
```bash
# Treinar modelos via API
curl -X POST http://localhost:8080/api/v1/test/ia/treinar
```

### **Erro: Dados insuficientes**
- Os modelos criam dados sintéticos para treinamento inicial
- Execute alguns testes de autenticação para gerar dados reais
- Use o endpoint de feedback para melhorar a precisão

## 📈 Métricas de Performance

### **Tempos Esperados**
- **Análise individual:** < 500ms
- **Treinamento inicial:** < 30s
- **Retreinamento:** < 60s

### **Throughput**
- **Análises simultâneas:** > 100/segundo
- **Usuários concorrentes:** > 50

### **Acurácia Esperada**
- **Isolation Forest:** ~85%
- **Random Forest:** ~82%
- **Deep Learning:** ~88%
- **Ensemble:** ~91%

## 🎯 Casos de Uso Reais

### **1. Detecção de Fraude**
- Login de localização incomum
- Dispositivo não reconhecido
- Horário atípico de acesso

### **2. Análise Comportamental**
- Padrão de navegação suspeito
- Velocidade de digitação anormal
- Sequência de ações incomum

### **3. Prevenção de Ataques**
- Tentativas de força bruta
- Ataques automatizados
- Uso de proxies/VPNs

## 📝 Logs e Monitoramento

### **Logs da Aplicação**
```bash
# Verificar logs em tempo real
tail -f logs/application.log
```

### **Logs de IA**
- Análises realizadas
- Scores calculados
- Modelos retreinados
- Feedback recebido

### **Métricas de Sistema**
- CPU e memória utilizadas
- Tempo de resposta das APIs
- Taxa de acertos dos modelos

## 🔄 Ciclo de Vida dos Testes

1. **Inicialização:** Treinar modelos iniciais
2. **Teste Básico:** Verificar conectividade
3. **Teste Funcional:** Validar algoritmos
4. **Teste de Carga:** Verificar performance
5. **Teste de Integração:** Validar fluxo completo
6. **Monitoramento:** Acompanhar métricas

---

## 🆘 Suporte

Em caso de problemas:

1. Verifique os logs da aplicação
2. Confirme se todas as dependências estão instaladas
3. Teste os endpoints básicos primeiro
4. Consulte a documentação técnica em `MODULO_IA_GUIA.md`

**Contato:** Equipe de Desenvolvimento IA 