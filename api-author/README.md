# API Author - Serviço de Autenticação e Autorização

## Descrição
Serviço de autenticação e autorização baseado em JWT (JSON Web Token) para sistemas de micro-serviços.

## ⚠️ **PROBLEMAS CONHECIDOS E SOLUÇÕES**

### **Problema 1**: Erro "Could not resolve placeholder 'jwt.secret'"
**Causa**: Placeholder não resolvido na configuração JWT
**✅ SOLUÇÃO**: Configuração JWT atualizada no `application.yml`

### **Problema 2**: Dependências do Spring Security não encontradas
O projeto pode falhar na compilação com erros como:
```
package org.springframework.security.authentication does not exist
```

### **✅ SOLUÇÃO RECOMENDADA: Usar Docker**

## 🚀 **Como Executar (SOLUÇÕES ORDENADAS POR EFICÁCIA)**

### **1. 🔥 Método Mais Fácil - Script Automático**
```bash
# No Windows
cd api-author
run.bat

# Escolha a opção 1 (Docker) no menu
```

### **2. 🐳 Docker (100% Funcional)**
```bash
# Na raiz do projeto (C:\micro-services\)
docker-compose up --build auth-service
```

### **3. 🔧 Maven Local (pode ter problemas)**
```bash
cd api-author
mvn clean spring-boot:run -s settings.xml
```

## Estrutura do Projeto

```
api-author/
├── src/
│   └── main/
│       ├── java/
│       │   └── br/
│       │       └── com/
│       │           └── auth/
│       │               ├── AuthServiceApplication.java
│       │               ├── config/
│       │               │   ├── JwtAuthenticationFilter.java
│       │               │   ├── SecurityConfig.java
│       │               │   └── SwaggerConfig.java
│       │               ├── controller/
│       │               │   └── AuthenticationController.java
│       │               ├── dto/
│       │               │   ├── AuthenticationRequest.java
│       │               │   ├── AuthenticationResponse.java
│       │               │   └── RegisterRequest.java
│       │               ├── entity/
│       │               │   ├── AuditLog.java
│       │               │   └── User.java
│       │               ├── exception/
│       │               │   └── GlobalExceptionHandler.java
│       │               ├── repository/
│       │               │   ├── AuditLogRepository.java
│       │               │   └── UserRepository.java
│       │               └── service/
│       │                   ├── AuthenticationService.java
│       │                   ├── ContextAnalysisService.java
│       │                   ├── JwtService.java
│       │                   └── UserDetailsServiceImpl.java
│       └── resources/
│           └── application.yml
├── pom.xml
├── settings.xml
├── run.bat
├── mvnw
└── mvnw.cmd
```

## 🔧 **Soluções Técnicas Implementadas**

### 1. **settings.xml Local**
- ✅ Configurado para usar Maven Central em vez do repositório corporativo
- ✅ Força download das dependências do repositório público

### 2. **Script de Execução (run.bat)**
- ✅ Menu interativo para escolher método de execução
- ✅ Prioriza Docker (100% funcional)
- ✅ Opção Maven local com configurações otimizadas

### 3. **Docker Configurado**
- ✅ `Dockerfile` otimizado para a estrutura do projeto
- ✅ `docker-compose.yml` configurado corretamente
- ✅ Contorna completamente problemas de repositório Maven

## 🏗️ **Configurações Realizadas**

### 1. Reorganização da Estrutura
- ✅ Movido código fonte para dentro da pasta `api-author/`
- ✅ Estrutura Maven padrão implementada
- ✅ Arquivos `mvnw` e `mvnw.cmd` movidos para permitir execução Maven sem instalação global

### 2. Configuração do Docker
- ✅ `Dockerfile` atualizado para trabalhar com a nova estrutura
- ✅ `docker-compose.yml` configurado para o novo contexto de build

### 3. Configuração do Repositório Maven
- ✅ Adicionado repositório Maven Central no `pom.xml`
- ✅ Criado `settings.xml` local para forçar uso do Maven Central
- ✅ Script `run.bat` para facilitar execução

## 🐛 **Solução de Problemas Detalhada**

### **Problema**: Dependências do Spring Security não encontradas
**Causa**: Repositório corporativo não possui todas as dependências do Spring Security

**Soluções em ordem de eficácia**:

1. **🥇 USAR DOCKER** (100% funcional):
```bash
# Na raiz (C:\micro-services\)
docker-compose up --build auth-service
```

2. **🥈 Script automático**:
```bash
cd api-author
run.bat
# Escolher opção 1 (Docker)
```

3. **🥉 Maven com settings.xml**:
```bash
cd api-author
mvn clean compile -s settings.xml
```

4. **Configurar settings.xml global** (última opção):
```xml
<!-- %USERPROFILE%\.m2\settings.xml -->
<settings>
    <mirrors>
        <mirror>
            <id>central</id>
            <mirrorOf>*</mirrorOf>
            <url>https://repo1.maven.org/maven2</url>
        </mirror>
    </mirrors>
</settings>
```

## 🌐 **Tecnologias Utilizadas**
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Token)
- Swagger/OpenAPI
- Lombok

## 📍 **Endpoints Principais**
- `POST /api/v1/auth/register` - Registro de usuário
- `POST /api/v1/auth/authenticate` - Autenticação
- `GET /swagger-ui.html` - Documentação da API

### Resposta de Autenticação Atualizada
Os endpoints de autenticação agora retornam:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "name": "Nome do Usuário",
  "login": "usuario@email.com"
}
```

## ⚙️ **Configuração de Variáveis de Ambiente**

### Variáveis Opcionais (com valores padrão)
- `SERVER_PORT` - Porta do servidor (padrão: 8081)
- `DB_URL` - URL do banco PostgreSQL (padrão: jdbc:postgresql://localhost:5432/auth_db)
- `DB_USERNAME` - Usuário do banco (padrão: postgres)
- `DB_PASSWORD` - Senha do banco (padrão: postgres)
- `JWT_SECRET` - Chave secreta para JWT (valor padrão configurado)
- `JWT_EXPIRATION` - Tempo de expiração do JWT em ms (padrão: 86400000 - 24h)

### Configuração JWT Atualizada
A configuração JWT foi corrigida no arquivo `application.yml`:
```yaml
jwt:
  secret: ***REMOVIDO***
  expiration: 86400000 # 24 horas em milissegundos
```

**Nota**: A chave secreta está em Base64 e pode ser substituída por uma variável de ambiente `JWT_SECRET` se necessário.

## 🔌 **Portas**
- **8081** - API REST
- **5432** - PostgreSQL
- **5050** - pgAdmin

## ✅ **Status Final**
- ✅ Projeto reorganizado com estrutura Maven padrão  
- ✅ Compatível com `mvn clean install` (via Docker)  
- ✅ Docker configurado e 100% funcional  
- ✅ Problema de dependências resolvido via Docker  
- ✅ Script automatizado para facilitar execução  
- ✅ **RECOMENDAÇÃO: USE DOCKER! 🐳**

## 🔄 **Funcionalidades Implementadas Recentemente**

### ✅ Correção do Placeholder JWT (2024)
- **Problema**: Erro "Could not resolve placeholder 'jwt.secret'"
- **Solução**: Configuração JWT atualizada no `application.yml`
- **Resultado**: Aplicação inicializa sem erros relacionados ao JWT

### ✅ Resposta de Autenticação Aprimorada (2024)
- **Implementação**: AuthenticationResponse agora inclui nome e login do usuário
- **Campos Adicionados**:
  - `name`: Nome completo do usuário
  - `login`: Email de acesso do usuário
- **Endpoints Afetados**:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/authenticate`
- **Benefício**: Frontend pode exibir informações do usuário sem chamadas adicionais

# Serviço de Autenticação e Autorização

Este é um microserviço de autenticação e autorização desenvolvido com Spring Boot, que implementa um sistema robusto de segurança com análise de contexto do usuário.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Tokens)
- Docker & Docker Compose
- Swagger 3.x
- Lombok
- Maven

## Funcionalidades

- Autenticação de usuários
- Autorização baseada em roles
- **Role padrão**: Usuários criados sem roles específicas recebem automaticamente `USER_DEFAULT`
- Análise de contexto do usuário:
  - Localização
  - Dispositivo
  - Horário de acesso
  - Padrões de comportamento
- Auditoria de acessos
- Geração e validação de tokens JWT
- Documentação da API com Swagger
- Tratamento global de exceções
- Validação de dados de entrada
- Bloqueio de conta após tentativas falhadas

## Requisitos

- Java 17 ou superior
- Docker e Docker Compose
- Maven

## Configuração do Ambiente

1. Clone o repositório
2. Configure as variáveis de ambiente (se necessário)
3. Execute o Docker Compose para iniciar os serviços:

```bash
docker-compose up -d --build
```

## Executando a Aplicação

### Com Docker (Recomendado)
```bash
docker-compose up -d --build
```

### Localmente
```bash
mvn spring-boot:run
```

## Acessando a Documentação

A documentação da API estará disponível em:
- Swagger UI: http://localhost:8081/api/v1/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/api/v1/api-docs

## Estrutura do Projeto

```
src/main/java/br/com/auth/
├── config/          # Configurações do Spring
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   └── SwaggerConfig.java
├── controller/      # Controladores REST
│   └── AuthenticationController.java
├── dto/            # Objetos de transferência de dados
│   ├── AuthenticationRequest.java
│   ├── AuthenticationResponse.java
│   └── RegisterRequest.java
├── entity/         # Entidades JPA
│   ├── User.java
│   └── AuditLog.java
├── exception/      # Tratamento de exceções
│   └── GlobalExceptionHandler.java
├── repository/     # Repositórios JPA
│   ├── UserRepository.java
│   └── AuditLogRepository.java
├── service/        # Lógica de negócios
│   ├── AuthenticationService.java
│   ├── ContextAnalysisService.java
│   ├── JwtService.java
│   └── UserDetailsServiceImpl.java
└── AuthServiceApplication.java
```

## Endpoints da API

### Autenticação
- `POST /api/v1/auth/register` - Registrar novo usuário
- `POST /api/v1/auth/authenticate` - Autenticar usuário

## Exemplos de Uso

### 1. Registrar usuário com roles específicas
```json
POST /api/v1/auth/register
{
  "name": "João Silva",
  "email": "joao@gmail.com",
  "password": "123456",
  "roles": ["ADMIN", "USER"]
}
```

### 2. Registrar usuário com role padrão (USER_DEFAULT será atribuída automaticamente)
```json
POST /api/v1/auth/register
{
  "name": "Maria Santos",
  "email": "maria@gmail.com",
  "password": "123456"
}
```

### 3. Fazer login
```json
POST /api/v1/auth/authenticate
{
  "email": "joao@gmail.com",
  "password": "123456"
}
```

## Sistema de Roles

- **Role Padrão**: `USER_DEFAULT` - Atribuída automaticamente quando nenhuma role é especificada
- **Roles Customizadas**: Podem ser especificadas durante o registro
- **Autoridades**: Cada role é prefixada com `ROLE_` no Spring Security (ex: `ROLE_USER_DEFAULT`)

## Segurança

- Autenticação via JWT
- Senhas criptografadas com BCrypt
- Proteção contra ataques comuns (CSRF, XSS)
- Validação de tokens
- Análise de contexto para detecção de atividades suspeitas
- Bloqueio automático de contas após múltiplas tentativas falhadas
- Auditoria completa de eventos de segurança

## Funcionalidades Implementadas

### ✅ Funcionalidades Básicas
- [x] Registro de usuários
- [x] Autenticação com JWT
- [x] Sistema de roles com valor padrão
- [x] Validação de dados de entrada
- [x] Tratamento global de exceções
- [x] Documentação com Swagger
- [x] Containerização com Docker

### ✅ Análise de Contexto
- [x] Detecção de horários incomuns
- [x] Monitoramento de mudanças de localização
- [x] Detecção de mudanças de dispositivo
- [x] Controle de múltiplas tentativas de login

### ✅ Auditoria e Segurança
- [x] Log de eventos de segurança
- [x] Bloqueio automático de contas
- [x] Histórico de acessos
- [x] Monitoramento de atividades suspeitas

## Melhorias Futuras

- [ ] Implementar autenticação de dois fatores (2FA)
- [ ] Adicionar integração com serviços de geolocalização
- [ ] Implementar cache para melhor performance
- [ ] Adicionar testes unitários e de integração
- [ ] Implementar rate limiting
- [ ] Adicionar métricas e monitoramento

## Contribuição

1. Faça o fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. 