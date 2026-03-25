# Sistema Autonomo de Autenticacao Inteligente

Sistema de autenticacao adaptativa que utiliza modelos de Inteligencia Artificial para analise comportamental, blockchain para auditoria imutavel e score de confianca dinamico para tomada de decisao em tempo real.

## Visao Geral

O sistema combina tecnicas de Machine Learning, analise contextual e registro distribuido para criar uma camada de autenticacao inteligente que vai alem do modelo tradicional usuario/senha. A cada tentativa de login, o sistema analisa o comportamento do usuario, calcula um score de confianca e decide automaticamente se deve permitir o acesso, exigir autenticacao adicional (MFA) ou bloquear a tentativa.

## Arquitetura

```
+-------------------+       +-------------------+       +-------------------+
|                   |       |                   |       |                   |
|  Frontend Angular | <---> |  API Spring Boot  | <---> |   PostgreSQL      |
|  (Nginx :4200)    |       |  (JWT :8080)      |       |   (login_intel.)  |
|                   |       |                   |       |                   |
+-------------------+       +---------+---------+       +-------------------+
                                      |
                          +-----------+-----------+
                          |           |           |
                    +-----+---+ +----+----+ +----+------+
                    | Isolation| | Random  | |   Deep    |
                    | Forest   | | Forest  | | Learning  |
                    | (40%)    | | (30%)   | |  (30%)    |
                    +----------+ +---------+ +-----------+
                          |           |           |
                          +-----------+-----------+
                                      |
                              +-------+-------+
                              |   Ensemble    |
                              |   Score       |
                              +-------+-------+
                                      |
                          +-----------+-----------+
                          |                       |
                    +-----+------+    +-----------+---+
                    | Hyperledger|    | Score de      |
                    | Fabric     |    | Confianca     |
                    | (Auditoria)|    | (Decisao)     |
                    +------------+    +---------------+
```

## Stack Tecnologica

### Backend
| Tecnologia | Versao | Funcao |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.11 | Framework backend |
| Spring Security | 6.x | Autenticacao e autorizacao |
| JWT (jjwt) | 0.11.5 | Tokens de autenticacao |
| PostgreSQL | 16 | Banco de dados relacional |
| WEKA | 3.8.6 | Isolation Forest |
| Smile | 3.0.2 | Random Forest |
| DeepLearning4j | 1.0.0-M2.1 | Redes neurais |
| Web3j | 4.10.3 | Integracao Ethereum |
| Hyperledger Fabric | - | Blockchain empresarial |
| TOTP (samstevens) | 1.7.1 | Autenticacao multifator |
| Swagger/OpenAPI | 2.8.8 | Documentacao da API |

### Frontend
| Tecnologia | Versao | Funcao |
|---|---|---|
| Angular | 17.2.0 | Framework frontend |
| TypeScript | 5.2.2 | Linguagem |
| Bootstrap | 5.3.0 | Estilizacao |
| Chart.js | 4.4.1 | Graficos e visualizacoes |
| Keycloak | 24.0.0 | OAuth2 (opcional) |
| RxJS | 7.8.0 | Programacao reativa |

### DevOps
| Tecnologia | Funcao |
|---|---|
| Docker | Containerizacao (multi-stage builds) |
| Docker Compose | Orquestracao de servicos |
| GitHub Actions | CI/CD pipeline |
| GitHub Container Registry | Registro de imagens |
| Nginx | Proxy reverso do frontend |

## Funcionalidades

### Autenticacao Inteligente
- Login com analise de contexto em tempo real (IP, dispositivo, localizacao, horario)
- Registro de usuarios com atribuicao automatica de perfis
- Autenticacao multifator (MFA) com TOTP via Google Authenticator
- Integracao com Keycloak para OAuth2/OpenID Connect (opcional)
- Bloqueio automatico de contas suspeitas

### Modelos de Inteligencia Artificial

#### Isolation Forest (Peso: 40%)
Algoritmo de deteccao de anomalias baseado em arvores de isolamento. Identifica comportamentos atipicos analisando o quao facilmente um ponto de dados pode ser isolado dos demais. Implementado com 100 arvores e tamanho de amostra de 256.

#### Random Forest (Peso: 30%)
Classificador ensemble composto por 50 arvores de decisao que analisa padroes comportamentais para classificar tentativas de login como legitimas ou suspeitas. Utiliza 60% das features em cada arvore para maior diversidade.

#### Deep Learning (Peso: 30%)
Rede neural com arquitetura de 4 camadas (14 -> 32 -> 16 -> 8 -> 1 neuronios) que aprende padroes complexos de comportamento. Utiliza funcao de ativacao ReLU nas camadas ocultas e Sigmoid na saida para classificacao binaria.

#### Ensemble (Score Final)
Combina os tres modelos com pesos configurados para gerar uma pontuacao final de risco entre 0.0 e 1.0.

### Score de Confianca
Score dinamico calculado com base em fatores ponderados:
- **Taxa de sucesso historica** (40%): Historico de logins bem-sucedidos
- **Analise de IA** (30%): Score combinado dos modelos de ML
- **Comportamento recente** (20%): Atividades nas ultimas sessoes
- **Fatores externos** (10%): Geolocalizacao, dispositivo, horario

**Decisoes baseadas no score:**
| Score | Decisao |
|---|---|
| > 0.7 | Acesso permitido |
| 0.5 - 0.7 | MFA obrigatorio |
| 0.2 - 0.5 | MFA + alerta |
| < 0.2 | Acesso bloqueado |

### Blockchain e Auditoria
- Registro imutavel de eventos de autenticacao no Hyperledger Fabric
- Hash SHA-3 para integridade das transacoes
- Consulta de historico de auditoria por usuario, data ou tipo de evento
- Suporte a blockchain nativa (Ethereum) e empresarial (Hyperledger)

### Geolocalizacao
- Deteccao de localizacao por IP com multiplos provedores (IP-API, OpenCage, Nominatim)
- Calculo de distancia Haversine entre localizacoes
- Deteccao de viagem impossivel (logins distantes em curto intervalo)
- Mecanismo de fallback para alta disponibilidade

### Gerenciamento de Sessoes
- Rastreamento de sessoes ativas em banco de dados
- Limite de sessoes concorrentes (padrao: 3)
- Fingerprint de dispositivo e navegador
- Limpeza automatica de sessoes inativas a cada 5 minutos
- Notificacoes para novas sessoes detectadas

### Simulacao de Ataques
Dashboard para testes de seguranca com cenarios simulados:
- Forca bruta
- Credential stuffing
- Viagem impossivel
- Sequestro de dispositivo

### Visualizacoes e Dashboards
- Dashboard principal com metricas em tempo real
- Comparacao de modelos de IA com graficos radar
- Mapa de calor geografico de acessos
- Timeline de eventos de seguranca
- Graficos de distribuicao de risco

## Estrutura do Projeto

```
sistema-autonomo/
|-- api-auditoria/                    # Backend Spring Boot
|   |-- src/main/java/br/com/auth/
|   |   |-- config/                   # Configuracoes (Security, JWT, Swagger, Blockchain)
|   |   |-- controller/               # 10 controllers REST
|   |   |-- dominio/
|   |   |   |-- entidades/            # Entidades JPA
|   |   |   |-- interfaces/           # Contratos de servicos
|   |   |-- infraestrutura/
|   |   |   |-- repositorios/         # Repositorios Spring Data
|   |   |-- service/                  # Logica de negocios e IA
|   |   |-- dto/                      # Objetos de transferencia
|   |   |-- exception/                # Tratamento de erros
|   |-- src/main/resources/
|   |   |-- application.yml           # Configuracoes (dev, prod, keycloak, elk)
|   |-- Dockerfile                    # Build multi-stage
|   |-- pom.xml                       # Dependencias Maven
|
|-- front-auditoria/                  # Frontend Angular
|   |-- src/app/
|   |   |-- components/               # 13+ componentes Angular
|   |   |-- services/                 # Servicos de comunicacao
|   |   |-- guards/                   # Guards de autenticacao
|   |   |-- interceptors/             # Interceptadores HTTP
|   |-- nginx.conf                    # Configuracao do proxy reverso
|   |-- Dockerfile.frontend           # Build multi-stage
|
|-- .github/workflows/deploy.yml      # Pipeline CI/CD
|-- docker-compose.yml                # Desenvolvimento local
|-- docker-compose.prod.yml           # Producao
|-- .env.production                   # Variaveis de ambiente
```

## Como Executar

### Pre-requisitos
- Java 21+
- Node.js 18+
- Docker e Docker Compose
- PostgreSQL 16 (ou via Docker)

### Desenvolvimento Local

1. **Clone o repositorio**
```bash
git clone https://github.com/jgbcientista/sistema-autonomo.git
cd sistema-autonomo
```

2. **Inicie com Docker Compose**
```bash
docker compose up -d
```

Servicos disponiveis:
- Frontend: http://localhost:4200
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

3. **Ou execute separadamente**

Backend:
```bash
cd api-auditoria
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Frontend:
```bash
cd front-auditoria
npm install
ng serve
```

### Producao

1. **Configure as variaveis de ambiente**

Edite o arquivo `.env.production`:
```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<host>:5432/<database>?currentSchema=login_inteligente
DB_USERNAME=<usuario>
DB_PASSWORD=<senha>
DB_SCHEMA=login_inteligente
SERVER_PORT=8080
```

2. **Build e deploy com Docker**
```bash
# Build das imagens
docker build -t api:latest -f api-auditoria/Dockerfile api-auditoria/
docker build -t frontend:latest -f front-auditoria/Dockerfile.frontend front-auditoria/

# Inicie os containers
docker compose -f docker-compose.prod.yml up -d
```

3. **Crie o schema no PostgreSQL**
```sql
CREATE SCHEMA IF NOT EXISTS login_inteligente;
```

## CI/CD Pipeline

O pipeline GitHub Actions executa automaticamente a cada push na branch `main`:

1. **Build API** - Compila e testa o backend com Maven/JDK 21
2. **Build Frontend** - Compila o Angular em modo producao
3. **Docker Build** - Constroi e publica imagens no GitHub Container Registry
4. **Deploy** - Conecta via SSH na VPS, faz pull das imagens e reinicia os containers

## Endpoints Principais

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/v1/autenticacao/registrar` | Registrar novo usuario |
| POST | `/api/v1/autenticacao/entrar` | Login com analise contextual |
| POST | `/api/v1/mfa/configurar` | Configurar MFA (TOTP) |
| POST | `/api/v1/mfa/validar` | Validar codigo MFA |
| GET | `/api/v1/analise-comportamental/metricas` | Metricas dos modelos de IA |
| POST | `/api/v1/analise-comportamental/analisar/{id}` | Analise comportamental por usuario |
| GET | `/api/v1/score-confianca/{id}` | Score de confianca do usuario |
| GET | `/api/v1/geolocalizacao/localizar/{ip}` | Geolocalizacao por IP |
| GET | `/api/v1/blockchain/auditoria/eventos` | Eventos de auditoria no blockchain |
| GET | `/api/v1/sessoes/ativas` | Sessoes ativas do usuario |

Documentacao completa da API disponivel em `/swagger-ui.html`.

## Perfis de Configuracao

| Perfil | Descricao |
|---|---|
| `dev` | Desenvolvimento local, logs detalhados, Redis desabilitado |
| `prod` | Producao com PostgreSQL, Redis, blockchain real |
| `keycloak` | Integracao com Keycloak OAuth2 Resource Server |
| `elk` | Logging centralizado com ELK Stack |

## Licenca

Este projeto foi desenvolvido como parte de pesquisa academica em sistemas de autenticacao inteligente com aplicacao de tecnicas de Machine Learning e tecnologia blockchain.
