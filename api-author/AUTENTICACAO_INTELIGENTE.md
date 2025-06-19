# API de Autenticação Inteligente com IA e Blockchain

## Visão Geral

Este documento detalha as implementações realizadas para transformar a API de autenticação básica em um sistema inteligente que utiliza **Inteligência Artificial** para análise de contexto e **Blockchain** para registro imutável de transações de autenticação.

## 🚀 Funcionalidades Implementadas

### 1. Análise de Contexto com IA (`AiContextAnalysisService`)

Sistema avançado de análise comportamental que avalia múltiplos fatores para determinar o risco de uma tentativa de autenticação.

#### Componentes de Análise:

- **Análise de Localização**: Verifica consistência geográfica, distância de locais usuais e risco por país
- **Análise de Dispositivo**: Detecta novos dispositivos, alterações de fingerprint e integridade do browser
- **Análise Temporal**: Avalia padrões de horário, detecção de acessos em horários incomuns
- **Análise Comportamental**: Examina padrões de digitação, interações e duração de sessão
- **Análise de Rede**: Detecta VPN, proxy, TOR e verifica reputação do IP

#### Decisões Baseadas em IA:
- `ALLOW`: Risco baixo, acesso liberado
- `REQUIRE_MFA`: Risco médio, requer autenticação adicional
- `REQUIRE_ADDITIONAL_VERIFICATION`: Verificação extra necessária
- `DENY`: Risco alto, acesso negado

### 2. Registro Imutável em Blockchain (`BlockchainService`)

Sistema de registro de todas as transações de autenticação em blockchain para garantir auditoria completa e imutabilidade.

#### Características:
- **Registro Assíncrono**: Não impacta performance do login
- **Hash Criptográfico**: Cada evento gera hash único dos dados
- **Verificação de Confirmação**: Sistema verifica confirmações na blockchain
- **Fallback Simulado**: Funciona mesmo sem blockchain real configurado

#### Tipos de Eventos Registrados:
- `LOGIN_SUCCESS`: Login bem-sucedido
- `LOGIN_FAILED`: Falha na autenticação
- `LOGIN_DENIED_AI`: Acesso negado pela IA

### 3. Padrões de Comportamento (`UserBehaviorPattern`)

Sistema de aprendizado que constrói perfis comportamentais dos usuários ao longo do tempo.

#### Dados Coletados:
- Frequência de login por horário
- Localizações típicas de acesso
- Dispositivos habituais
- Scores de consistência por categoria
- Histórico de atividades suspeitas

### 4. APIs de Analytics (`AnalyticsController`)

Interface administrativa para monitoramento e análise de segurança.

#### Endpoints Disponíveis:

##### Análise de Contexto
- `POST /analytics/context-analysis` - Executa análise de contexto manual
- `GET /analytics/user/{userId}/behavior-pattern` - Obtém padrão comportamental

##### Blockchain
- `GET /analytics/user/{userId}/blockchain-transactions` - Transações do usuário
- `GET /analytics/blockchain/transaction/{hash}` - Detalhes da transação
- `GET /analytics/blockchain/high-risk-transactions` - Transações de alto risco
- `GET /analytics/blockchain/unverified-transactions` - Transações pendentes
- `GET /analytics/blockchain/transaction/{hash}/verify` - Verifica status da transação

##### Métricas de Segurança
- `GET /analytics/security-metrics` - Métricas gerais do sistema
- `GET /analytics/user/{userId}/risk-assessment` - Avaliação de risco detalhada

## 🛠️ Estrutura Técnica

### Novas Entidades

#### `UserBehaviorPattern`
```java
- Scores de consistência por categoria
- Localizações e dispositivos típicos
- Frequência de login por horário
- Contadores de atividades suspeitas
```

#### `BlockchainTransaction`
```java
- Hash da transação blockchain
- Dados da decisão de autenticação
- Status de confirmação
- Metadados de auditoria
```

### Novos Repositórios

- `UserBehaviorPatternRepository`: Consultas de padrões comportamentais
- `BlockchainTransactionRepository`: Gestão de transações blockchain

### DTOs de Análise

#### `ContextAnalysisRequest`
```java
- Dados de geolocalização
- Informações de rede
- Dados comportamentais
- Fingerprint do dispositivo
```

#### `ContextAnalysisResponse`
```java
- Score de risco geral
- Análises detalhadas por categoria
- Decisão da IA
- Recomendações de segurança
```

## ⚙️ Configurações

### Arquivo `application.yml`

```yaml
# Configurações de IA
ai:
  service:
    url: "" # URL do serviço de IA externo
    enabled: false

# Thresholds de Risco
risk:
  threshold:
    low: 0.3
    medium: 0.6
    high: 0.8

# Configurações de Blockchain
blockchain:
  enabled: false
  network:
    name: "localhost"
    url: "http://localhost:8545"
  private:
    key: ""
  contract:
    address: ""
```

## 🔧 Dependências Adicionadas

```xml
<!-- Machine Learning & IA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Blockchain -->
<dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>4.10.3</version>
</dependency>

<!-- HTTP Client -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>

<!-- Apache Commons Math -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-math3</artifactId>
    <version>3.6.1</version>
</dependency>
```

## 🔄 Fluxo de Autenticação Inteligente

1. **Recepção de Credenciais**: Sistema recebe dados de login
2. **Validação Básica**: Verifica usuário e senha
3. **Análise de Contexto Simples**: Verificações básicas (ContextAnalysisService)
4. **Análise de IA Avançada**: Processamento com algoritmos de ML (AiContextAnalysisService)
5. **Decisão Baseada em Risco**: IA determina ação (ALLOW/DENY/REQUIRE_MFA)
6. **Registro em Blockchain**: Transação registrada de forma imutável
7. **Atualização de Padrões**: Perfil comportamental do usuário éUpdatedAt
8. **Resposta ao Cliente**: Token JWT ou negação de acesso

## 📊 Algoritmo de Cálculo de Risco

### Pesos por Categoria:
- **Localização**: 25%
- **Comportamental**: 25%
- **Dispositivo**: 20%
- **Temporal**: 15%
- **Rede**: 15%

### Fórmula de Risco:
```
Risk Score = (LocationRisk × 0.25) + (BehavioralRisk × 0.25) + 
             (DeviceRisk × 0.20) + (TemporalRisk × 0.15) + 
             (NetworkRisk × 0.15)
```

## 🛡️ Recursos de Segurança

### Detecção de Ameaças
- Tentativas de login em horários incomuns
- Acessos de localizações distantes
- Novos dispositivos não reconhecidos
- Uso de VPN/Proxy/TOR
- Padrões comportamentais anômalos

### Auditoria Completa
- Registro imutável de todas as transações
- Rastreabilidade completa de decisões
- Métricas de segurança em tempo real
- Relatórios de risco por usuário

## 🚀 Como Usar

### 1. Configuração Básica
```bash
# Configurar as variáveis de ambiente no application.yml
# Executar migrations do banco de dados
# Iniciar a aplicação
```

### 2. Autenticação com IA
```bash
# O sistema agora analisa automaticamente cada tentativa de login
# Logs detalhados mostram as decisões da IA
# Transações são registradas automaticamente no blockchain simulado
```

### 3. Monitoramento
```bash
# Acessar /swagger-ui.html para ver as APIs
# Usar endpoints /analytics/* para métricas
# Monitorar logs para atividades suspeitas
```

## 📈 Benefícios Implementados

1. **Segurança Aprimorada**: Análise multi-dimensional de risco
2. **Auditoria Confiável**: Registro imutável em blockchain
3. **Aprendizado Contínuo**: Sistema evolui com padrões dos usuários
4. **Visibilidade Operacional**: Métricas e dashboards de segurança
5. **Escalabilidade**: Arquitetura assíncrona para alta performance
6. **Conformidade**: Rastro completo para auditorias regulatórias

## 🔮 Próximos Passos

1. **Integração com IA Externa**: Conectar com serviços de ML avançados
2. **Blockchain Real**: Configurar integração com Ethereum/Polygon
3. **Dashboard Visual**: Interface web para monitoramento
4. **Alertas em Tempo Real**: Notificações de atividades suspeitas
5. **Machine Learning Próprio**: Treinar modelos específicos da aplicação

---

**Observação**: Este sistema está configurado para funcionar em modo simulado por padrão, permitindo testes sem necessidade de infraestrutura blockchain real. Para produção, configure as variáveis de blockchain e IA conforme necessário. 