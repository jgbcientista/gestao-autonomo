# Sistema Autonomo de Autenticacao Inteligente

Sistema de autenticacao adaptativa que utiliza modelos de Inteligencia Artificial para analise comportamental, blockchain para auditoria imutavel e score de confianca dinamico para tomada de decisao em tempo real.

## Visao Geral

O sistema combina tecnicas de Machine Learning, analise contextual e registro distribuido para criar uma camada de autenticacao inteligente que vai alem do modelo tradicional usuario/senha. A cada tentativa de login, o sistema analisa 14 features contextuais, calcula um score de confianca com 3 modelos ensemble e decide automaticamente se deve permitir o acesso, exigir autenticacao adicional (MFA) ou bloquear a tentativa.

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

### 1. Login e Registro

**Tela de Login:**
- Card com efeito glassmorphism sobre fundo com formas geometricas flutuantes animadas
- Logotipo circular com icone de escudo (shield-lock) no topo
- Campos: Email (obrigatorio, validacao de formato) e Senha (obrigatorio, toggle de visibilidade)
- Captura de dinamica de digitacao durante a entrada da senha (eventos onPasswordFocus, onPasswordKeyDown, onPasswordKeyUp)
- Spinner de carregamento durante autenticacao
- Mensagens de erro/sucesso em alerta
- Divisor visual com "ou" e link para registro
- Apos login bem-sucedido, exibe tela de desafio MFA: input para codigo TOTP de 6 digitos do Google Authenticator

**Tela de Registro:**
- Campos: Nome (2-100 caracteres), Email (validacao de formato), Senha (minimo 6 caracteres), Confirmar Senha (validacao de correspondencia)
- Indicador de forca da senha em tempo real: barra de progresso com percentual (0-100%) e label (fraca/media/forte) com cores graduais
- Captura de dinamica de digitacao durante o preenchimento
- Apos registro, exibe configuracao MFA: QR Code para escanear com Google Authenticator, chave secreta manual em fonte monoespacada, guia passo-a-passo (4 etapas), input de verificacao do codigo de 6 digitos

---

### 2. Dashboard Principal

**Menu de Navegacao Lateral:**
- Dashboard (home)
- Monitoramento: Score de Confianca, Geo Heatmap
- Inteligencia Artificial: Analise IA, Comparacao IA, XAI, Deriva Comportamental
- Seguranca: Blockchain, Simulacao Ataques, Keystroke, Gestao Acesso (somente admin)
- Gerar Dados (ferramenta de geracao)

**Secao Hero:**
- Badge de status com indicador visual
- Saudacao personalizada com o primeiro nome do usuario
- Subtitulo descritivo do painel
- 3 badges informativos: "100% Seguro", "24/7 Monitoramento", "IA Powered"
- Indicador de seguranca animado com aneis pulsantes concentricos

**Cards de Status (4 cards):**
1. **Sistema Online** (verde): icone check-circle, status "Operacional"
2. **Sessao Ativa** (azul): icone person-check, status "Autenticado"
3. **IA Ativa** (amarelo): icone cpu-fill, status "Monitorando"
4. **Score Confianca** (roxo): icone graph-up, status "Excelente"

**Grid de Funcionalidades (11 cards clicaveis):**
1. Score de Confianca — icone bullseye, "Score Excelente"
2. Analise IA — icone CPU, "IA Ativa"
3. Blockchain — icone chain, "Cadeia Integra"
4. Simulacao de Ataques — icone bug, "Ambiente de Teste"
5. Mapa Geografico — icone globe, "Monitoramento Ativo"
6. Comparacao de Modelos — icone bar-chart, "3 Modelos Ativos"
7. Gerador de Dados — icone database, "Pronto para Gerar"
8. Keystroke Dynamics — icone keyboard, "Biometria Ativa"
9. Explicabilidade IA — icone lightbulb, "XAI Ativo"
10. Deriva Comportamental — icone activity, "Monitoramento Ativo"
11. Gestao de Acesso — icone person-check, "Controle Ativo" (visivel apenas para admin)

**Metricas do Sistema:**
- Uso de CPU (percentual)
- Uso de Memoria (percentual, MB usado/maximo)
- Carga IA (percentual)
- Uptime (formatado)
- Total de usuarios
- Total de transacoes blockchain
- Total de analises de IA

**Toast de Notificacao:**
- Canto inferior direito, icone azul de informacao, cabecalho "Sistema", mensagem sobre autenticacao inteligente ativa

---

### 3. Score de Confianca

**Selecao de Usuario:**
- Dropdown com todos os usuarios registrados, exibindo nome, email e score atual
- Auto-seleciona o usuario logado

**Gauge SVG do Score:**
- Medidor semicircular de 0.0 a 1.0
- Gradiente de cores: Vermelho (0.0) → Laranja → Azul → Verde (1.0)
- Agulha animada apontando para o score atual
- Valor numerico central (ex: "0.85")
- Label de nivel de confianca (ex: "Muito Alto")

**Cards de Detalhes:**

1. **Card Score Atual:**
   - Visualizacao do gauge SVG
   - Score base e fator de ajuste (positivo/negativo)
   - Badge "Em Observacao" quando aplicavel

2. **Card Decisao Automatica:**
   - Icone com fundo colorido baseado na decisao
   - Label da decisao: Permitir / Exige MFA / Bloqueado
   - 4 flags de decisao com icones: Confiavel (check/x), Requer MFA (lock/unlock), Bloqueio (shield), Observacao (eye)

3. **Card Estatisticas:**
   - Logins bem-sucedidos (icone check, verde)
   - Logins suspeitos (icone warning, laranja)
   - Tentativas bloqueadas (icone x, vermelho)
   - MFA exigido (icone key, azul)

**Analise Contextual:**
- Card de largura total com icone de lampada
- Narrativa detalhada explicando os componentes do score: nivel, historico de login, analise IA, fatores historicos, ajustes manuais e decisao final

**Tabela de Historico de Localizacoes:**
- Colunas: #, Data/Hora, Endereco IP, Localizacao, Dispositivo, Risco, Status
- Paginacao (8 linhas por pagina)
- Badges de risco: Baixo (verde), Medio (amarelo), Alto (vermelho)
- Badges de status: Sucesso (verde), Falha (vermelho)

**Ajuste Manual de Score:**
- Input de valor de ajuste (-1.0 a 1.0)
- Input de motivo
- Botao "Aplicar"

**Explicacao do Calculo (4 fatores em grid):**
1. **Historico de Sucesso (40%)** — badge azul, taxa de logins bem-sucedidos
2. **Analise de IA (30%)** — badge roxo, ensemble de modelos
3. **Comportamento Recente (20%)** — badge verde, consistencia nos ultimos 7 dias
4. **Fatores Externos (10%)** — badge ambar, tempo desde ultimo login, geolocalizacao

**Formula:**
```
Score = (Historico x 0.4) + (IA x 0.3) + (Recente x 0.2) + (Externos x 0.1) + Fator de Ajuste
```

**Limiares de Decisao (3 caixas):**
- Verde: >= 0.70 → Confiavel → Acesso Permitido
- Ambar: 0.50 - 0.69 → Suspeito → Exige MFA
- Vermelho: < 0.20 → Nao Confiavel → Acesso Bloqueado

**KPIs Gerais do Sistema (4 cards):**
- Score Medio Geral (azul)
- Usuarios Confiaveis (verde)
- Score Baixo (ambar)
- Em Observacao (roxo)

---

### 4. Analise de IA

**Selecao e Controles:**
- Dropdown de usuario
- Botao "Nova Analise" (com spinner de carregamento)
- Botao toggle "Iniciar Monitor" / "Pausar Monitor"

**Card de Classificacao:**
- Icone grande de acordo com classificacao (check/warning/alert)
- Label: Esperado / Suspeito / Anomalo / Altamente Suspeito
- Nivel de risco com timestamp
- Fundo colorido baseado na classificacao

**Card de Analise Contextual:**
- Icone de lampada com narrativa detalhada explicando os resultados da analise

**Tabela de Historico de Localizacoes:**
- Mesma estrutura da tela de Score de Confianca com paginacao

**Card Perfil Comportamental:**
- Score Global de Anomalia com barra de progresso
- Percentual de confianca da analise com barra de progresso
- Grid de scores comportamentais: Horario, Localizacao, Dispositivo, Frequencia, Navegacao
- Tags dos modelos utilizados

**Card Scores dos Algoritmos (4 itens):**
1. **Isolation Forest** — Deteccao de outliers, circulo de score colorido
2. **Random Forest** — Classificacao supervisionada, circulo de score colorido
3. **Deep Learning** — Redes neurais profundas, circulo de score colorido
4. **Ensemble Score** — Combinacao dos algoritmos, circulo de score maior e destacado

**Visualizacao do Fluxo de Decisao:**
```
1. Login → 2. Coleta → 3. Analise IA → 4. Decisao → 5. Blockchain
```
Cada etapa com icone e descricao breve

**Limiares de Decisao (3 cards):**
- 0.0 - 0.3: PERMITIDO (verde)
- 0.3 - 0.7: SUSPEITO (amarelo)
- 0.7 - 1.0: NEGADO (vermelho)

**14 Features Analisadas (secao expansivel):**
1. Hora do Acesso
2. Dia da Semana
3. Frequencia Semanal
4. IP Conhecido?
5. Dispositivo Conhecido?
6. Localizacao Conhecida?
7. Distancia da Localizacao Habitual
8. Diferenca do Horario Habitual
9. Tempo Desde Ultimo Acesso
10. Media de Sessoes Diarias
11. Desvio Padrao dos Horarios
12. Total de IPs Distintos
13. Total de Dispositivos Distintos
14. Score de Navegacao

**Pesos dos Modelos:**
- Isolation Forest (40%) — barra gradiente
- Random Forest (30%) — barra gradiente
- Deep Learning (30%) — barra gradiente

**Card Estatisticas Gerais:**
- Total de Analises
- Anomalias Detectadas
- Taxa de Acuracia (%)
- Tempo Medio (ms)

**Card Controles de IA:**
- Botao de Treinamento dos Modelos
- Painel de informacao de treinamento com 3 modelos
- Timestamp do ultimo treinamento
- Toggle de monitoramento em tempo real

**Timeline de Historico de Analises:**
- Itens numerados na timeline
- Cada item exibe: ID da analise, timestamp, score, confianca (%)
- Marcadores coloridos (danger/warning/success)
- Paginacao

---

### 5. Comparacao de Modelos de IA

**Selecao de Usuario:**
- Dropdown para selecionar usuario

**Card Explicacao dos Modelos (3 itens numerados):**
1. Isolation Forest (Azul #1351B4) — peso 40%
2. Random Forest (Verde #168821) — peso 30%
3. Deep Learning (Marrom #B8860B) — peso 30%
- Caixa com formula do ensemble
- Tabela de classificacao (Score → Classificacao)

**Cards de Metricas (4 cards por modelo):**
Cada card exibe:
- Nome e peso do modelo
- Descricao do algoritmo
- 3 barras de metricas: Precisao, Recall, F1-Score
- Contagem total de predicoes

**Grafico Radar SVG + Concordancia:**
- Grafico radar SVG com 5 eixos: Precisao, Recall, F1-Score, Consistencia, Velocidade
- 3 poligonos coloridos (um por modelo) sobrepostos
- Legenda abaixo do grafico
- Explicacao de como interpretar o radar

**Gauge de Concordancia:**
- Gauge circular mostrando percentual de concordancia entre modelos
- Estatisticas abaixo: Total de Analises, Discordancias, Concordancias
- Caixas explicativas sobre calculo de concordancia
- Justificativa de por que o ensemble e superior

**Tabela de Historico de Comparacao:**
- Colunas: #, Data/Hora, Isolation Forest, Random Forest, Deep Learning, Ensemble, Classificacao, Acordo?
- Destaque visual em linhas com discordancia entre modelos
- Valores de score com labels de classificacao
- Paginacao

---

### 6. Perfil de Dinamica de Digitacao (Keystroke)

**Selecao de Usuario:**
- Dropdown com score do usuario

**4 Cards de KPIs:**
1. **Tempo Medio Hold** — icone stopwatch, ex: "89 ms", tempo medio que cada tecla fica pressionada
2. **Tempo Medio Flight** — icone arrow, ex: "45 ms", tempo medio entre soltar uma tecla e pressionar a proxima
3. **Amostras** — icone collection, ex: "127", total de capturas registradas para baseline
4. **Score Similaridade** — icone bullseye, ex: "0.892", colorido por nivel (success/warning/danger), label "Alta Similaridade"

**Card Gauge de Similaridade:**
- Barra horizontal de gauge de 0.0 a 1.0
- Zonas coloridas: Baixa (vermelho), Moderada (amarelo), Alta (verde)
- Marcador na posicao do score atual
- Botao "Atualizar Baseline"

**Metrica de Desvio Padrao:**
- Barra de progresso mostrando desvio
- Label e valor numerico

**Grafico de Tempos de Hold (barras):**
- Tempos de hold por tecla
- Barras coloridas por nivel de risco (baixo/medio/alto)
- Exibe: nome da tecla, barra, valor em milissegundos

**Tabela de Capturas Recentes:**
- Colunas: #, Data/Hora, Eventos, Similaridade
- Badge de similaridade com codigo de cor
- Destaque em linhas com baixa similaridade

---

### 7. Blockchain e Auditoria

**Navegacao por Abas:**
- Visao Geral
- Transacoes (transacoes do usuario)
- Alto Risco (transacoes de alto risco)
- Integridade (verificacao de integridade)

**Aba Visao Geral — Estatisticas:**
- Total de Transacoes
- Transacoes Confirmadas
- Transacoes Pendentes
- Integridade da Cadeia (valida/invalida)
- Descricao narrativa do blockchain: contagem, status, integridade, garantia SHA-256

**Aba Transacoes do Usuario:**
- Dropdown de selecao de usuario
- Tabela: #, Hash (truncado), Status, Risco, Decisao, Data/Hora
- Badges de status: CONFIRMADO (verde), PENDENTE (amarelo), FALHADO (vermelho)
- Badges de risco: BAIXO, MEDIO, ALTO (coloridos)
- Badges de decisao: PERMITIDO, NEGADO, REQUER_MFA
- Linhas expansiveis para visualizacao detalhada da transacao
- Paginacao
- Relatorio do usuario: total, taxa de sucesso, integridade, ultima transacao

**Aba Alto Risco:**
- Slider de limiar de risco (padrao 0.7)
- Tabela com mesmas colunas, filtrada para alto risco
- Paginacao

**Busca de Transacao:**
- Input de busca por hash
- Botao "Buscar"
- Exibicao dos detalhes completos da transacao encontrada

**Aba Integridade:**
- Campo de input de hash
- Botao "Verificar"
- Resultado: status de integridade (integro: verdadeiro/falso), mensagem

---

### 8. Mapa Geografico de Acessos (Geo Heatmap)

**Selecao de Usuario:**
- Dropdown para selecionar usuario

**Mapa Mundi SVG:**
- Projecao Mercator, 1000x500px
- Pontos de acesso plotados por coordenadas geograficas

**Pontos de Acesso:**
- Acessos normais: pontos verdes
- Acessos suspeitos: pontos vermelhos
- Risco medio: pontos laranja
- Tamanho do ponto varia pela quantidade de acessos (raio de 3-12px)
- Tooltip interativo ao passar o mouse: nome da cidade, contagem de acessos, nivel de risco, ultimo acesso

**Linhas de Conexao:**
- Linhas azuis: conexoes normais entre localizacoes
- Linhas vermelhas tracejadas: viagens impossiveis
- Opacidade variavel (0.3 normal, 0.8 viagem impossivel)

**Estatisticas:**
- Total de acessos
- Acessos normais
- Acessos suspeitos
- Viagens impossiveis

**Detalhes de Viagem Impossivel:**
- Distancia (km)
- Intervalo de tempo (minutos)
- Velocidade necessaria (km/h)
- Se a viagem e fisicamente impossivel

---

### 9. Simulacao de Ataques

**4 Cenarios de Ataque (cards):**

1. **Forca Bruta** (vermelho #E52207):
   - Icone shield-exclamation
   - "10 tentativas rapidas de login do mesmo IP"
   - Sistema detecta padrao e bloqueia automaticamente

2. **Credential Stuffing** (marrom #B8860B):
   - Icone globe2
   - "8 IPs internacionais diferentes em curto intervalo"

3. **Viagem Impossivel** (azul #1351B4):
   - Icone airplane-fill
   - "Sao Paulo → Tokyo em 5 minutos"
   - Distancia impossivel de percorrer

4. **Sequestro de Dispositivo** (roxo #7c3aed):
   - Icone laptop
   - "Dispositivo e localizacao totalmente novos"

**Exibicao do Fluxo de Simulacao:**
- Numero do passo atual / total de passos
- Descricao do passo corrente
- Progressao animada passo-a-passo (intervalos de 800ms)

**Visualizacao dos Estagios (card por estagio):**
- Numero do passo
- Descricao do que aconteceu
- Trust score (barra gauge colorida)
- Score de anomalia da IA
- Decisao: PERMITIR / EXIGIR_MFA / BLOQUEAR
- IP, Localizacao, Dispositivo
- Hash do blockchain
- Distancia/Velocidade (para cenarios de viagem)
- Timestamp

**Gauge de Score (barra horizontal):**
- Colorido: Verde (>= 0.7), Laranja (0.5-0.7), Vermelho (< 0.2)
- Largura proporcional ao valor do score

**Resumo Final:**
- Nome do cenario executado
- Duracao total (ms)
- Botao "Reset Simulacao"

---

### 10. Gerador de Dados

- Ferramenta para gerar registros sinteticos de login
- Os dados passam pelo fluxo completo: Auditoria + IA + Blockchain + Trust Score
- Barra de progresso da geracao
- Endpoint de progresso em tempo real

---

### 11. Explicabilidade da IA (XAI)

- Explicacao detalhada das decisoes tomadas pela IA
- Contribuicao de cada feature para a decisao final
- Visualizacao de pesos e fatores determinantes
- Justificativa transparente para auditorias

---

### 12. Deriva Comportamental

- Monitoramento de mudancas graduais ou abruptas no comportamento do usuario
- Deteccao de desvios do padrao historico
- Alertas quando o perfil comportamental muda significativamente
- Historico de evolucao do comportamento ao longo do tempo

---

### 13. Gestao de Acesso (Admin)

- Listagem de solicitacoes de acesso pendentes
- Aprovacao/rejeicao de usuarios
- Listagem completa de usuarios do sistema
- Bloqueio/desbloqueio de contas
- Ajustes manuais de score de confianca
- Reset de estado de simulacoes
- Monitoramento de metricas do sistema
- Verificacao de integridade do blockchain

## Modelos de Inteligencia Artificial

### Isolation Forest (Peso: 40%)
Algoritmo de deteccao de anomalias baseado em arvores de isolamento. Identifica comportamentos atipicos analisando o quao facilmente um ponto de dados pode ser isolado dos demais. Implementado com 100 arvores e tamanho de amostra de 256.

### Random Forest (Peso: 30%)
Classificador ensemble composto por 50 arvores de decisao que analisa padroes comportamentais para classificar tentativas de login como legitimas ou suspeitas. Utiliza 60% das features em cada arvore para maior diversidade.

### Deep Learning (Peso: 30%)
Rede neural com arquitetura de 4 camadas (14 → 32 → 16 → 8 → 1 neuronios) que aprende padroes complexos de comportamento. Utiliza funcao de ativacao ReLU nas camadas ocultas e Sigmoid na saida para classificacao binaria.

### Ensemble (Score Final)
```
Ensemble = (IsolationForest x 0.4) + (RandomForest x 0.3) + (DeepLearning x 0.3)
```

### 14 Features Analisadas por Login
1. Hora do acesso
2. Dia da semana
3. Frequencia semanal de acesso
4. IP conhecido?
5. Dispositivo conhecido?
6. Localizacao conhecida?
7. Distancia da localizacao habitual (km)
8. Diferenca do horario habitual (horas)
9. Tempo desde ultimo acesso (horas)
10. Media de sessoes diarias
11. Desvio padrao dos horarios
12. Total de IPs distintos
13. Total de dispositivos distintos
14. Score de navegacao

## Score de Confianca

Score dinamico calculado com base em fatores ponderados:
- **Taxa de sucesso historica** (40%): Historico de logins bem-sucedidos
- **Analise de IA** (30%): Score combinado dos modelos de ML
- **Comportamento recente** (20%): Atividades nas ultimas sessoes
- **Fatores externos** (10%): Geolocalizacao, dispositivo, horario

**Decisoes baseadas no score:**
| Score | Decisao |
|---|---|
| >= 0.70 | Acesso permitido |
| 0.50 - 0.69 | MFA obrigatorio |
| 0.20 - 0.49 | MFA + alerta |
| < 0.20 | Acesso bloqueado |

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

## Endpoints Principais

| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/v1/autenticacao/registrar` | Registrar novo usuario |
| POST | `/api/v1/autenticacao/entrar` | Login com analise contextual |
| POST | `/api/v1/mfa/configurar` | Configurar MFA (TOTP) |
| POST | `/api/v1/mfa/validar` | Validar codigo MFA |
| POST | `/api/v1/ia/analisar/{id}` | Analise comportamental por usuario |
| POST | `/api/v1/ia/classificar/{id}` | Classificar acesso como esperado/anomalo |
| POST | `/api/v1/ia/treinar` | Retreinar modelos de ML |
| GET | `/api/v1/ia/estatisticas` | Estatisticas gerais da IA |
| GET | `/api/v1/ia/historico/{id}` | Historico de analises por usuario |
| GET | `/api/v1/score-confianca/{id}` | Score de confianca do usuario |
| POST | `/api/v1/score-confianca/{id}/ajustar` | Ajuste manual do score |
| GET | `/api/v1/geolocalizacao/localizar/{ip}` | Geolocalizacao por IP |
| GET | `/api/v1/geolocalizacao/mapa-acessos/{id}` | Heatmap de acessos |
| GET | `/api/v1/geolocalizacao/viagem-impossivel` | Deteccao de viagem impossivel |
| GET | `/blockchain/auditoria/transacoes` | Transacoes do blockchain |
| GET | `/blockchain/auditoria/usuario/{id}` | Transacoes por usuario |
| POST | `/blockchain/auditoria/verificar-integridade` | Verificar integridade |
| GET | `/blockchain/auditoria/alto-risco` | Transacoes de alto risco |
| GET | `/api/v1/dinamica-digitacao/perfil/{id}` | Perfil de digitacao |
| POST | `/api/v1/dinamica-digitacao/capturar` | Capturar dados de digitacao |
| POST | `/api/v1/simulacao-ataque/forca-bruta` | Simular forca bruta |
| POST | `/api/v1/simulacao-ataque/credential-stuffing` | Simular credential stuffing |
| POST | `/api/v1/simulacao-ataque/viagem-impossivel` | Simular viagem impossivel |
| POST | `/api/v1/simulacao-ataque/sequestro-dispositivo` | Simular sequestro |
| GET | `/api/v1/gestao-acesso/usuarios` | Listar usuarios (admin) |
| POST | `/api/v1/gestao-acesso/aprovar/{id}` | Aprovar acesso (admin) |
| POST | `/api/v1/gestao-acesso/bloquear/{id}` | Bloquear conta (admin) |
| GET | `/api/v1/health` | Status de saude do sistema |
| GET | `/api/v1/health/metricas` | Metricas (CPU, memoria, IA) |

Documentacao completa da API disponivel em `/swagger-ui.html`.

## Como Executar

### Pre-requisitos
- Java 21+
- Node.js 18+
- Docker e Docker Compose
- PostgreSQL 16 (ou via Docker)

### Desenvolvimento Local

```bash
git clone <repo-url>
cd sistema-autonomo
docker compose up -d
```

| Servico | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

Ou execute separadamente:

```bash
# Backend
cd api-auditoria && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd front-auditoria && npm install && ng serve
```

### Producao

```bash
docker build -t api:latest -f api-auditoria/Dockerfile api-auditoria/
docker build -t frontend:latest -f front-auditoria/Dockerfile.frontend front-auditoria/
docker compose -f docker-compose.prod.yml up -d
```

## Perfis de Configuracao

| Perfil | Descricao |
|---|---|
| `dev` | Desenvolvimento local, logs detalhados, Redis desabilitado |
| `prod` | Producao com PostgreSQL, Redis, blockchain real |
| `keycloak` | Integracao com Keycloak OAuth2 Resource Server |
| `elk` | Logging centralizado com ELK Stack |

## CI/CD Pipeline

O pipeline GitHub Actions executa automaticamente a cada push na branch `main`:

1. **Build API** — Compila e testa o backend com Maven/JDK 21
2. **Build Frontend** — Compila o Angular em modo producao
3. **Docker Build** — Constroi e publica imagens no GitHub Container Registry
4. **Deploy** — Conecta via SSH na VPS, faz pull das imagens e reinicia os containers

## Licenca

Projeto privado - todos os direitos reservados.
