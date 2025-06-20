# Configuração do Swagger para Produção

## ✅ Implementações Realizadas

### 1. **Configuração Global do Swagger**
- Arquivo: `src/main/java/br/com/auth/config/SwaggerConfig.java`
- **Habilitado para todos os perfis** (dev, prod, padrão)
- Interface rica com documentação detalhada
- Suporte a autenticação JWT
- Múltiplos servidores configurados

### 2. **Configuração no application.yml**

#### **Configuração Global:**
```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
  show-actuator: true
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

#### **Perfil de Produção:**
```yaml
# Profile prod
springdoc:
  api-docs:
    enabled: true
    path: /api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    operationsSorter: method
  show-actuator: true
```

### 3. **Configuração de Segurança**
- Arquivo: `src/main/java/br/com/auth/config/SecurityConfig.java`
- **Endpoints liberados para acesso público:**
  - `/api/v1/swagger-ui/**`
  - `/api/v1/api-docs/**`
  - `/api/v1/webjars/**`

### 4. **URLs de Acesso**

#### **Desenvolvimento (H2):**
```bash
# Executar aplicação
java -jar target/auth-service-1.0.0.jar --spring.profiles.active=dev

# URLs disponíveis:
- Swagger UI: http://localhost:8081/api/v1/swagger-ui.html
- API Docs: http://localhost:8081/api/v1/api-docs
- H2 Console: http://localhost:8081/api/v1/h2-console
```

#### **Produção (PostgreSQL):**
```bash
# Subir PostgreSQL
docker-compose up -d postgres

# Executar aplicação
java -jar target/auth-service-1.0.0.jar --spring.profiles.active=prod
# ou
java -jar target/auth-service-1.0.0.jar

# URLs disponíveis:
- Swagger UI: http://localhost:8081/api/v1/swagger-ui.html
- API Docs: http://localhost:8081/api/v1/api-docs
- PgAdmin: http://localhost:5050 (se habilitado)
```

### 5. **Recursos do Swagger Implementados**

#### **📋 Informações da API:**
- **Título:** Sistema de Autenticação Inteligente
- **Versão:** 1.0.0
- **Descrição completa** com funcionalidades
- **Contato e licença** configurados

#### **🔐 Segurança:**
- **Autenticação JWT** configurada
- **Bearer Token** suportado
- **Esquema de segurança** documentado

#### **🌐 Servidores:**
- **Desenvolvimento:** http://localhost:8081/api/v1
- **Produção:** https://api.authsystem.com/api/v1

#### **📚 Documentação Rica:**
- Funcionalidades principais listadas
- Algoritmos de IA explicados
- Níveis de confiança detalhados
- Emojis para melhor visualização

### 6. **Endpoints Documentados**

#### **🔐 Autenticação:**
- `POST /autenticacao/registrar` - Registro de usuário
- `POST /autenticacao/login` - Login com score de confiança

#### **📊 Score de Confiança:**
- `GET /trust-score/usuario/{email}` - Consultar score
- `POST /trust-score/ajustar/{email}` - Ajustar score
- `GET /trust-score/estatisticas` - Estatísticas gerais

#### **🤖 Análise de IA:**
- `POST /ia/analisar-comportamento` - Análise comportamental
- `GET /ia/historico/{userId}` - Histórico de análises

#### **🌍 Geolocalização:**
- `GET /geo/ip/{ip}` - Geolocalização por IP
- `GET /geo/endereco` - Coordenadas por endereço

#### **🏥 Monitoramento:**
- `GET /health` - Health check
- `GET /actuator/**` - Métricas do sistema

### 7. **Como Usar o Swagger**

#### **1. Acessar a Interface:**
```
http://localhost:8081/api/v1/swagger-ui.html
```

#### **2. Autenticar (se necessário):**
1. Fazer login via `/autenticacao/login`
2. Copiar o token JWT da resposta
3. Clicar em "Authorize" no Swagger
4. Inserir: `Bearer {seu-token-jwt}`

#### **3. Testar Endpoints:**
- Expandir seções de interesse
- Clicar em "Try it out"
- Preencher parâmetros
- Clicar em "Execute"

### 8. **Verificação de Funcionamento**

#### **Comandos de Teste:**
```bash
# Testar Swagger UI
curl http://localhost:8081/api/v1/swagger-ui.html

# Testar API Docs JSON
curl http://localhost:8081/api/v1/api-docs

# Testar endpoint específico
curl http://localhost:8081/api/v1/health
```

## ✅ **Status da Implementação**

- ✅ **SwaggerConfig.java:** Atualizado com documentação rica
- ✅ **application.yml:** Configurado para todos os perfis
- ✅ **SecurityConfig.java:** Endpoints liberados
- ✅ **Perfil dev:** Swagger habilitado
- ✅ **Perfil prod:** Swagger habilitado
- ✅ **Autenticação JWT:** Suportada no Swagger
- ✅ **Documentação:** Completa e detalhada

## 🚀 **Próximos Passos**

1. **Executar aplicação** com perfil desejado
2. **Acessar Swagger UI** no navegador
3. **Testar endpoints** através da interface
4. **Documentar APIs** adicionais conforme necessário

O Swagger está **100% configurado** para funcionar tanto em desenvolvimento quanto em produção! 