# Trabalhos Relacionados e Estado da Arte

## Autenticação Adaptativa com Inteligência Artificial, Biometria Comportamental e Blockchain

---

## 1. Introdução

Este documento apresenta uma revisão dos principais trabalhos encontrados na literatura acadêmica e na indústria que se relacionam com a proposta do **Sistema Autônomo de Autenticação Inteligente**. O sistema proposto integra aprendizado de máquina (Isolation Forest, Random Forest e Deep Learning), biometria comportamental (dinâmica de digitação), blockchain (Hyperledger Fabric e Ethereum) para trilha de auditoria imutável, score de confiança dinâmico, autenticação multifator adaptativa e explicabilidade de IA (SHAP). A análise está organizada por eixos temáticos, com cada trabalho descrito em um ou dois parágrafos, seguido de uma comparação direta com a proposta deste projeto.

---

## 2. Quadro Comparativo Geral

| # | Nome do Projeto / Trabalho | Proposta | Ano | Objetivo | Experimento | Métricas |
|---|---------------------------|----------|-----|----------|-------------|----------|
| 1 | Adaptive Biometric Authentication Systems (ScienceDirect) | Revisão sistemática de sistemas biométricos adaptativos | 2023 | Mapear o estado da arte em autenticação biométrica adaptativa | Revisão de literatura com análise de 80+ trabalhos | Taxas de FAR, FRR, EER |
| 2 | AI-Driven Adaptive Authentication for Multi-Modal Biometrics (ESRG Journal) | Framework com ML para ajuste dinâmico de parâmetros de autenticação | 2024 | Reduzir FAR e FRR em autenticação multimodal | Testes com dados biométricos multimodais | FAR reduzido em 27%, FRR reduzido em 35% |
| 3 | Continuous Smartphone Authentication via Multimodal Biometrics (MDPI) | Autenticação contínua com ensemble de CNN, LSTM e GRU | 2026 | Autenticação contínua em dispositivos móveis | Pipeline de deep learning heterogêneo com dados de sensores | Acurácia, EER, F1-Score |
| 4 | Keystroke Dynamics for Intelligent Biometric Authentication (Springer) | Autenticação por dinâmica de digitação com KNN, RF e LGBM | 2025 | Autenticação baseada em padrões de digitação | Testes com amostras de texto fixo | EER de 2.67% a 6.61% |
| 5 | Blockchain-Based Audit Trail Mechanism (MDPI Algorithms) | Mecanismo de trilha de auditoria baseado em blockchain | 2021 | Garantir integridade e transparência de registros de auditoria | Implementação com smart contracts | Throughput, latência, integridade |
| 6 | Harpocrates (Virginia Tech / Amazon EC2) | Logs de auditoria imutáveis com preservação de privacidade | 2022 | Auditoria imutável com privacidade em Hyperledger Fabric | Deploy em Amazon EC2 com Hyperledger Fabric | Latência, throughput, overhead de privacidade |
| 7 | IAM com Hyperledger Fabric e OAuth 2.0 (ScienceDirect) | Gestão de identidade e acesso com blockchain e OAuth 2.0 | 2023 | Melhorar segurança e escalabilidade no setor de saúde | Protótipo com Hyperledger Fabric | Escalabilidade, segurança, latência |
| 8 | Risk-Based Authentication usando ML (IEEE) | Sistema de autenticação baseado em risco com ML | 2018 | Classificar tentativas de login por nível de risco | Modelos de ML para scoring de risco | Acurácia, precisão, recall |
| 9 | Continuous Behavioral Biometrics Trust Engine (IJETCSIT) | Motor de confiança com biometria comportamental para autenticação passwordless | 2024 | Autenticação contínua com score de confiança dinâmico | Avaliação em dispositivos móveis e wearables | TAR > 90%, FAR < 5%, EER ~6.5% |
| 10 | AI-Based Continuous Authentication in Zero Trust (ResearchGate) | Autenticação contínua com IA em ambientes Zero Trust | 2025 | Decisões de confiança adaptativas com Reinforcement Learning | Framework com RL para enforcement de políticas | Redução de falsos positivos, detecção de ameaças internas |
| 11 | XAI para Detecção de Intrusão (Frontiers in AI) | Revisão de XAI integrada a IDS | 2025 | Tornar modelos de detecção de intrusão interpretáveis | Análise de SHAP/LIME em modelos de segurança | Interpretabilidade, fidelidade das explicações |
| 12 | Adaptive Security Model com Behavioral Authentication (Al-Qadisiyah) | Modelo adaptativo com detecção de deriva comportamental | 2024 | Proteção de dados com autenticação comportamental contínua | LSTM + Autoencoder com detecção de drift por KL-divergence | Acurácia de detecção de anomalias |
| 13 | IBM Trusteer (Indústria) | Prevenção de fraudes com biometria comportamental e IA | 2016–presente | Detectar fraudes e autenticar usuários em jornadas digitais | Produção em centenas de instituições financeiras | Taxas de detecção de fraude, falsos positivos |
| 14 | BioCatch (Indústria) | Biometria comportamental cognitiva e física para prevenção de fraudes | 2011–presente | Diferenciar usuários legítimos de fraudadores | Produção em bancos e fintechs globais | Redução de fraude, experiência do usuário |

---

## 3. Pesquisa no INPI (Instituto Nacional da Propriedade Industrial)

Até a data desta pesquisa (abril de 2026), a busca no banco de dados do INPI por patentes relacionadas aos termos "autenticação adaptativa", "autenticação inteligente", "login inteligente", "biometria comportamental com machine learning" e "blockchain para auditoria de autenticação" não retornou registros de patentes que combinem todos esses elementos em uma única solução. Foram identificados os seguintes pontos relevantes:

- **Patentes de blockchain no INPI**: Existem 47 pedidos de patentes relacionados a soluções técnicas que utilizam blockchain, mas nenhum especificamente voltado para trilhas de auditoria de autenticação com IA comportamental.
- **Biometria comportamental**: Não foram encontradas patentes registradas no INPI que tratem especificamente de dinâmica de digitação combinada com ensemble de modelos de ML para autenticação adaptativa.
- **Diretrizes de IA no INPI (2025)**: O INPI abriu consulta pública em agosto de 2025 sobre as Diretrizes de Exame de Pedidos de Patente relacionados à Inteligência Artificial, categorizando invenções em: (i) modelos e técnicas de IA, (ii) invenções baseadas em IA, e (iii) invenções assistidas por IA. Isso indica um amadurecimento regulatório que pode viabilizar futuras proteções para sistemas como o proposto neste trabalho.

**Relação com a proposta**: A ausência de patentes no INPI que integrem autenticação adaptativa com ML, biometria comportamental, blockchain e explicabilidade de IA evidencia o caráter inovador da proposta. O sistema proposto se posiciona em uma lacuna de propriedade intelectual no cenário brasileiro, combinando elementos que, individualmente, já possuem algum registro, mas que em conjunto representam uma contribuição original.

---

## 4. Trabalhos Acadêmicos

### 4.1 Adaptive Biometric Authentication Systems — Revisão Sistemática (ScienceDirect, 2023)

Este trabalho apresenta uma revisão sistemática abrangente sobre o design e avaliação de sistemas de autenticação biométrica adaptativos. Os autores analisam mais de 80 trabalhos da literatura, mapeando como esses sistemas ajustam dinamicamente seus processos de amostragem e reconhecimento em resposta a mudanças no ambiente operacional. A revisão identifica desafios críticos como ameaças à privacidade, spoofing e problemas de acessibilidade, além de categorizar as abordagens existentes por tipo de biometria (física e comportamental) e técnica de adaptação utilizada.

**Comparação com a proposta**: Enquanto a revisão mapeia o estado da arte de forma ampla, o sistema proposto neste trabalho implementa concretamente uma solução adaptativa que combina múltiplas camadas de adaptação: o ensemble de ML (Isolation Forest + Random Forest + Deep Learning) ajusta automaticamente o score de confiança, a autenticação multifator é ativada condicionalmente com base no risco calculado, e a detecção de deriva comportamental permite que o sistema se adapte a mudanças no padrão do usuário ao longo do tempo. Diferentemente dos trabalhos mapeados na revisão, a proposta adiciona blockchain como camada de auditoria imutável e explicabilidade de IA, elementos não cobertos na revisão.

---

### 4.2 AI-Driven Adaptive Authentication for Multi-Modal Biometrics (ESRG Journal, 2024)

Este trabalho propõe um framework que emprega algoritmos de machine learning para ajuste dinâmico de parâmetros de autenticação com base em dados contextuais e comportamentais do usuário. Os resultados experimentais demonstraram uma redução de 27% na taxa de falsa aceitação (FAR) e de 35% na taxa de falsa rejeição (FRR) em comparação com métodos estáticos tradicionais de autenticação. O framework utiliza dados biométricos multimodais para criar perfis adaptativos que evoluem com o comportamento do usuário.

**Comparação com a proposta**: A proposta deste trabalho compartilha o princípio de ajuste dinâmico baseado em contexto e comportamento, porém vai além ao implementar um ensemble de três algoritmos distintos (Isolation Forest com peso de 40%, Random Forest com 30% e Deep Learning com 30%) que produz um score unificado de risco entre 0.0 e 1.0. Adicionalmente, o sistema proposto incorpora regras de decisão escalonadas (score > 0.7 permite acesso; 0.5–0.7 exige MFA; 0.2–0.5 exige MFA + alerta; < 0.2 bloqueia acesso), oferecendo uma granularidade de resposta não presente no trabalho comparado. A integração com blockchain para registro imutável de cada decisão de autenticação também é um diferencial exclusivo.

---

### 4.3 Continuous Smartphone Authentication via Multimodal Biometrics and Optimized Ensemble Learning (MDPI, 2026)

Publicado na revista Mathematics da MDPI, este trabalho introduz um framework robusto de autenticação contínua que utiliza biometria comportamental multimodal. O pipeline emprega Redes Neurais Convolucionais (CNNs) para dados de sensores, redes Long Short-Term Memory (LSTM) para padrões de deslizamento curvilíneo, e Gated Recurrent Units (GRUs) para modelagem temporal. O ensemble heterogêneo de deep learning combina as saídas desses modelos para uma decisão de autenticação contínua, demonstrando que a fusão de múltiplas modalidades comportamentais supera abordagens unimodais.

**Comparação com a proposta**: Ambos os trabalhos utilizam ensembles de modelos para autenticação, porém com focos distintos. O trabalho da MDPI concentra-se em dispositivos móveis com dados de sensores (acelerômetro, giroscópio, touchscreen), enquanto a proposta deste projeto é voltada para sistemas web, utilizando dinâmica de digitação, geolocalização e análise contextual. O sistema proposto também se diferencia por oferecer explicabilidade via SHAP das decisões de cada modelo, score de confiança persistente e rastreável por blockchain, e a capacidade de detectar deriva comportamental ao longo do tempo — aspectos não abordados no trabalho comparado.

---

### 4.4 Keystroke Dynamics for Intelligent Biometric Authentication with Machine Learning (Springer, 2025)

Este estudo aplica três modelos de ML — K-Nearest Neighbors (KNN), Random Forest (RF) e Light Gradient Boosting Machine (LGBM) — para autenticação biométrica baseada em dinâmica de digitação. Os testes foram realizados com amostras de texto fixo para autenticação única (one-shot), alcançando taxas de Equal Error Rate (EER) entre 2.67% e 6.61% em datasets abertos de digitação livre. O trabalho demonstra que a combinação de features temporais (dwell time, flight time) com modelos de ML tradicionais é eficaz para autenticação por padrões de teclado.

**Comparação com a proposta**: O sistema proposto neste trabalho também utiliza dinâmica de digitação como uma das fontes de dados comportamentais, implementando a extração de features como tempo de pressionamento e intervalo entre teclas. Entretanto, a proposta vai além ao integrar a dinâmica de digitação como uma das 14 features de entrada para o ensemble de ML (que inclui Deep Learning com arquitetura 14→32→16→8→1), em vez de tratá-la isoladamente. Além disso, o sistema proposto utiliza a digitação em conjunto com análise de geolocalização, padrão de sessão e contexto de acesso, proporcionando uma visão holística do comportamento do usuário que os modelos unimodais de keystroke dynamics não oferecem.

---

### 4.5 A Blockchain-Based Audit Trail Mechanism: Design and Implementation (MDPI Algorithms, 2021)

Publicado na revista Algorithms da MDPI, este trabalho apresenta o design e a implementação de um mecanismo de trilha de auditoria baseado em blockchain. O sistema utiliza smart contracts para registrar e validar eventos de auditoria, garantindo imutabilidade, transparência e integridade dos registros. A arquitetura proposta separa a camada de aplicação da camada de blockchain, permitindo que sistemas existentes integrem a trilha de auditoria sem modificações significativas em sua infraestrutura.

**Comparação com a proposta**: O sistema proposto adota princípios similares de auditoria imutável por blockchain, porém com foco específico em eventos de autenticação e segurança. Enquanto o trabalho da MDPI propõe uma solução genérica de audit trail, o sistema proposto registra especificamente: hash da transação, tipo de evento (LOGIN_SUCCESS, LOGIN_DENIED, MFA_REQUIRED), ID do usuário (hasheado com SHA-3), endereço IP, score de confiança, decisão do sistema e timestamp. Além disso, o sistema suporta duas redes blockchain (Hyperledger Fabric para ambientes permissionados e Ethereum para interoperabilidade), oferecendo flexibilidade não presente no trabalho comparado. A integração bidirecional entre o score de confiança gerado pelo ensemble de ML e o registro em blockchain cria uma cadeia de evidências completa e verificável.

---

### 4.6 Harpocrates: Privacy-Preserving and Immutable Audit Log (Virginia Tech, 2022)

Harpocrates é um esquema de log de auditoria imutável com preservação de privacidade, implementado e avaliado sobre Hyperledger Fabric em ambiente Amazon EC2. O sistema foi projetado para operações sensíveis de dados, garantindo que registros de auditoria não possam ser adulterados enquanto preserva a privacidade dos dados auditados. A contribuição principal está na combinação de técnicas criptográficas com a imutabilidade nativa da blockchain para criar logs que são simultaneamente verificáveis e privados.

**Comparação com a proposta**: Ambos os trabalhos utilizam Hyperledger Fabric como plataforma de blockchain, porém com objetivos complementares. Harpocrates foca exclusivamente na privacidade e imutabilidade de logs genéricos, enquanto o sistema proposto utiliza a blockchain especificamente para auditoria de decisões de autenticação inteligente, incluindo o score de confiança calculado pelo ensemble de IA, a decisão tomada (permitir, exigir MFA ou bloquear) e o contexto comportamental que levou à decisão. O sistema proposto também implementa hashing SHA-3 para dados de usuário e opera em modo de simulação para desenvolvimento, com transição transparente para redes reais em produção — uma característica de engenharia não abordada no Harpocrates.

---

### 4.7 Enhancing IAM using Hyperledger Fabric and OAuth 2.0 (ScienceDirect, 2023)

Este trabalho propõe um sistema de gestão de identidade e acesso (IAM) que emprega Hyperledger Fabric e OAuth 2.0 para melhorar segurança e escalabilidade, com foco no setor de saúde. O sistema combina a autenticação baseada em tokens do OAuth 2.0 com a imutabilidade e descentralização do Hyperledger Fabric para criar uma plataforma robusta de controle de acesso. Os autores demonstram que a abordagem blockchain melhora a transparência das operações de acesso e reduz riscos de adulteração de logs.

**Comparação com a proposta**: Enquanto este trabalho combina blockchain com OAuth 2.0 (autenticação estática baseada em tokens), o sistema proposto substitui a abordagem estática por autenticação dinâmica baseada em score de confiança gerado por ensemble de IA. O sistema proposto também utiliza JWT com Spring Security em vez de OAuth 2.0, mas oferece integração opcional com Keycloak para cenários que exigem OAuth2/OpenID Connect. A principal diferença está na inteligência da decisão de acesso: enquanto o trabalho comparado toma decisões binárias (permitir/negar), o sistema proposto opera com quatro níveis de resposta baseados no score de confiança, adaptando-se ao risco em tempo real.

---

### 4.8 Design of a Risk-Based Authentication System using ML (IEEE, 2018)

Este trabalho pioneiro propõe o uso de técnicas de machine learning para construir um sistema de autenticação baseado em risco. O sistema classifica tentativas de login em diferentes níveis de risco com base em features contextuais como localização, dispositivo e horário de acesso. Os modelos de ML são treinados para identificar padrões normais de acesso e sinalizar desvios que indiquem ameaças potenciais, representando uma das primeiras propostas formais de integração entre ML e autenticação adaptativa.

**Comparação com a proposta**: O trabalho de 2018 estabeleceu bases conceituais que o sistema proposto expande significativamente. Enquanto o trabalho original utiliza features contextuais básicas e modelos de ML individuais, o sistema proposto implementa um ensemble ponderado de três algoritmos especializados (Isolation Forest para detecção de anomalias, Random Forest para classificação de padrões e Deep Learning para reconhecimento de padrões complexos), incorpora biometria comportamental via dinâmica de digitação, utiliza 14 features de entrada, e adiciona camadas de blockchain, explicabilidade e MFA adaptativo ausentes no trabalho original.

---

### 4.9 Continuous Behavioral Biometrics Trust Engine (IJETCSIT, 2024)

Este trabalho apresenta um motor de confiança (Trust Engine) baseado em biometria comportamental contínua para autenticação passwordless em dispositivos móveis e wearables. O sistema converte indicadores comportamentais em tempo real em um score de confiança dinâmico, alcançando True Acceptance Rate (TAR) superior a 90%, False Acceptance Rate (FAR) inferior a 5% e Equal Error Rate (EER) de aproximadamente 6.5%. Todos os dados brutos são mantidos localmente no dispositivo, preservando a privacidade do usuário.

**Comparação com a proposta**: Ambos os sistemas implementam o conceito de score de confiança dinâmico baseado em comportamento, porém com abordagens arquiteturais distintas. O Trust Engine opera exclusivamente no dispositivo (edge computing) com foco em privacidade local, enquanto o sistema proposto implementa o score no servidor com persistência em banco de dados PostgreSQL e registro em blockchain para auditabilidade. O sistema proposto também oferece explicabilidade das decisões via SHAP, permitindo que administradores e auditores compreendam quais features contribuíram para cada score — um requisito de transparência e compliance não atendido pelo Trust Engine.

---

### 4.10 AI-Based Continuous Authentication in Zero Trust Environments (ResearchGate, 2025)

Este trabalho explora a integração de Reinforcement Learning para enforcement automatizado de políticas em redes Zero Trust, introduzindo um framework dinâmico capaz de aprender decisões ótimas de acesso e resposta a partir de feedback contínuo da rede. Os autores demonstram que modelos baseados em IA superam mecanismos estáticos ao alcançar decisões de confiança adaptativas, detectar ameaças internas e reduzir falsos positivos em redes corporativas.

**Comparação com a proposta**: O framework Zero Trust utiliza Reinforcement Learning para decisões de acesso, enquanto o sistema proposto utiliza um ensemble supervisionado/não-supervisionado (Isolation Forest + Random Forest + Deep Learning). Ambos buscam decisões adaptativas, mas o sistema proposto oferece explicabilidade imediata de cada decisão (via SHAP), registro imutável em blockchain, e resposta escalonada (quatro níveis de ação), enquanto o framework Zero Trust foca em enforcement binário de políticas. O sistema proposto pode ser considerado complementar a uma arquitetura Zero Trust, adicionando camadas de biometria comportamental e auditoria que o framework comparado não implementa.

---

### 4.11 XAI para Sistemas de Detecção de Intrusão — Revisão Sistemática (Frontiers in AI, 2025)

Esta revisão sistemática analisa a integração de Inteligência Artificial Explicável (XAI) em Sistemas de Detecção de Intrusão (IDS), mapeando como técnicas como SHAP e LIME são aplicadas para tornar modelos de segurança mais transparentes e interpretáveis. Os autores argumentam que a explicabilidade é essencial para que profissionais de segurança possam confiar, compreender e otimizar modelos de detecção, destacando que modelos black-box limitam a adoção em ambientes críticos de segurança.

**Comparação com a proposta**: O sistema proposto implementa concretamente a explicabilidade de IA no contexto de autenticação, utilizando técnicas inspiradas em SHAP para decompor a contribuição de cada uma das 14 features no score de confiança final. Enquanto a revisão da Frontiers foca em IDS, o sistema proposto aplica explicabilidade especificamente a decisões de autenticação, permitindo que administradores visualizem quais fatores (geolocalização anômala, padrão de digitação diferente, horário incomum) levaram a uma decisão de bloqueio ou exigência de MFA. Essa transparência é registrada em blockchain, criando uma trilha de evidências explicável e imutável — uma integração não identificada nos trabalhos revisados.

---

### 4.12 Adaptive Security Model com Detecção de Deriva Comportamental (Al-Qadisiyah, 2024)

Este trabalho propõe um modelo de segurança adaptativo que utiliza um modelo híbrido combinando LSTM para predição de sequências e Autoencoders para reconstrução, com um módulo de aprendizado adaptativo que inclui detecção de drift baseada em divergência de Kullback-Leibler. O sistema diferencia entre deriva gradual (mudanças naturais no comportamento do usuário) e deriva abrupta (indicativa de comprometimento de conta), aplicando medidas de segurança proporcionais ao tipo e magnitude da mudança detectada.

**Comparação com a proposta**: O sistema proposto também implementa detecção de deriva comportamental como módulo dedicado (`ServicoDerivaComportamental`), monitorando mudanças nos padrões de acesso dos usuários ao longo do tempo. Enquanto o trabalho comparado utiliza KL-divergence com LSTM+Autoencoder, o sistema proposto integra a detecção de deriva diretamente com o ensemble de ML e o score de confiança, permitindo que mudanças comportamentais afetem automaticamente o nível de autenticação exigido. A diferenciação entre deriva gradual e abrupta também está presente na proposta, com o adicional de que cada detecção de drift é registrada em blockchain e pode ser explicada via módulo de explicabilidade de IA.

---

## 5. Soluções da Indústria

### 5.1 IBM Security Trusteer

IBM Trusteer é uma suíte de soluções de prevenção de fraude baseada em nuvem que combina analítica comportamental com IA, inteligência global sobre ameaças e avaliações de risco em tempo real. Desde 2016, a plataforma utiliza biometria comportamental para autenticar usuários e detectar tentativas de account takeover em jornadas digitais completas, do onboarding à transação. O sistema analisa sinais de identidade digital, biometria comportamental e informações do dispositivo para detectar fraudes antes que transações ocorram, sendo utilizado por centenas de instituições financeiras globalmente.

**Comparação com a proposta**: O IBM Trusteer é uma solução proprietária e fechada voltada para o setor financeiro, enquanto o sistema proposto é uma plataforma acadêmica/open-source com arquitetura transparente. A principal diferença está na explicabilidade: o Trusteer opera como black-box para seus usuários finais, enquanto o sistema proposto oferece explicabilidade completa via SHAP de cada decisão de autenticação. Adicionalmente, o sistema proposto integra blockchain para auditoria imutável — uma camada de transparência que soluções como Trusteer não oferecem publicamente. O IBM Trusteer não publica suas métricas de desempenho ou arquitetura de ML, impossibilitando reprodução acadêmica, enquanto o sistema proposto é completamente documentado e reprodutível.

---

### 5.2 BioCatch

Fundada em 2011, a BioCatch é uma empresa de cibersegurança especializada em biometria comportamental que analisa interações humano-dispositivo para proteger usuários e dados. A plataforma utiliza IA para examinar comportamentos digitais físicos e cognitivos dos usuários, gerando insights que diferenciam candidatos genuínos de potenciais criminosos cibernéticos. O sistema identifica anomalias comportamentais que sinalizam ameaças de fontes humanas e automatizadas, incluindo Remote Access Tools, bots, malware e account takeovers manuais. A BioCatch é amplamente adotada em bancos e fintechs globais.

**Comparação com a proposta**: A BioCatch e o sistema proposto compartilham a base conceitual de biometria comportamental para autenticação, porém com diferenças arquiteturais significativas. A BioCatch foca em fraude financeira e opera como SaaS proprietário, enquanto o sistema proposto é voltado para autenticação de sistemas web em geral, com código aberto e arquitetura documentada. O sistema proposto inova ao combinar biometria comportamental com: (a) ensemble explícito de três algoritmos com pesos configuráveis, (b) blockchain dual (Hyperledger Fabric + Ethereum) para auditoria, (c) explicabilidade das decisões via SHAP, e (d) detecção de deriva comportamental com resposta escalonada — elementos que a BioCatch não disponibiliza publicamente em sua arquitetura.

---

## 6. Síntese Comparativa

### Diferenciais exclusivos da proposta em relação ao estado da arte:

| Característica | Proposta | Literatura | Indústria |
|----------------|----------|------------|-----------|
| Ensemble de 3 algoritmos (IF + RF + DL) com pesos configuráveis | **Sim** | Parcial (geralmente 1-2 modelos) | Não divulgado |
| Score de confiança com 4 níveis de decisão | **Sim** | Binário ou ternário | Não divulgado |
| Blockchain dual (Hyperledger Fabric + Ethereum) | **Sim** | Uma rede ou nenhuma | Não utilizado |
| Explicabilidade via SHAP para autenticação | **Sim** | Aplicado em IDS, não em autenticação | Não disponível |
| Detecção de deriva comportamental integrada | **Sim** | Trabalhos isolados | Parcial |
| Dinâmica de digitação + contexto + geolocalização | **Sim** | Geralmente unimodal | Multimodal (proprietário) |
| Trilha de auditoria imutável das decisões de IA | **Sim** | Blockchain genérico | Não disponível |
| Código aberto e reprodutível | **Sim** | Parcial | Não |
| MFA adaptativo condicionado ao score de confiança | **Sim** | Raro | Parcial |

### Lacunas identificadas na literatura que a proposta preenche:

1. **Integração de explicabilidade com autenticação**: Nenhum dos trabalhos revisados implementa explicabilidade de IA especificamente para decisões de autenticação com registro em blockchain.
2. **Blockchain dual para auditoria de autenticação**: A combinação de Hyperledger Fabric (permissionada) e Ethereum (interoperável) para auditoria de eventos de autenticação não foi encontrada na literatura.
3. **Ensemble ponderado de três paradigmas de ML**: A combinação específica de detecção de anomalias (Isolation Forest), classificação (Random Forest) e aprendizado profundo (Deep Learning) com pesos configuráveis para autenticação é uma contribuição original.
4. **Resposta escalonada em quatro níveis**: A granularidade de resposta (permitir, MFA, MFA+alerta, bloquear) baseada em score contínuo supera as abordagens binárias predominantes.

---

## 7. Referências

1. "The design and evaluation of adaptive biometric authentication systems: Current status, challenges and future direction." *ScienceDirect*, 2023. Disponível em: https://www.sciencedirect.com/science/article/pii/S2405959523000504
2. "AI-Driven Adaptive Authentication for Multi-Modal Biometric Systems." *ESRG Journal*, 2024. Disponível em: https://journal.esrgroups.org/jes/article/download/6643/4609/12253
3. "Continuous Smartphone Authentication via Multimodal Biometrics and Optimized Ensemble Learning." *MDPI Mathematics*, 14(2), 311, 2026. Disponível em: https://www.mdpi.com/2227-7390/14/2/311
4. "Keystroke dynamics for intelligent biometric authentication with machine learning." *Discover Applied Sciences, Springer*, 2025. Disponível em: https://link.springer.com/article/10.1007/s42452-025-07449-5
5. "A Blockchain-Based Audit Trail Mechanism: Design and Implementation." *MDPI Algorithms*, 14(12), 341, 2021. Disponível em: https://www.mdpi.com/1999-4893/14/12/341
6. "Harpocrates: Privacy-Preserving and Immutable Audit Log for Sensitive Data Operations." *arXiv*, 2022. Disponível em: https://arxiv.org/abs/2211.04741
7. "Enhancing identity and access management using Hyperledger Fabric and OAuth 2.0." *ScienceDirect*, 2023. Disponível em: https://www.sciencedirect.com/science/article/pii/S2667345223000470
8. "Design of a risk based authentication system using machine learning techniques." *IEEE*, 2018. Disponível em: https://ieeexplore.ieee.org/document/8397628
9. "Continuous Behavioral Biometrics for Passwordless Authentication: A Trust Engine." *IJETCSIT*, 2024. Disponível em: https://ijetcsit.org/index.php/ijetcsit/article/view/643
10. "AI-Based Continuous Authentication in Zero Trust Environments." *ResearchGate*, 2025. Disponível em: https://www.researchgate.net/publication/396744658
11. "A systematic review on the integration of explainable artificial intelligence in intrusion detection systems." *Frontiers in AI*, 2025. Disponível em: https://www.frontiersin.org/journals/artificial-intelligence/articles/10.3389/frai.2025.1526221/full
12. "Adaptive Security Model for Data Protection Using Behavioral User Authentication." *Journal of Al-Qadisiyah for Computer Science and Mathematics*, 2024. Disponível em: https://jqcsm.qu.edu.iq/index.php/journalcm/article/view/2651
13. IBM Security Trusteer. Disponível em: https://www.ibm.com/security/fraud-protection/trusteer
14. BioCatch — Behavioral Biometrics. Disponível em: https://www.biocatch.com/what-we-do
15. INPI — Consulta Pública sobre Diretrizes de IA. Disponível em: https://www.gov.br/inpi/pt-br/servicos/patentes/consultas-publicas/Minutadasdiretrizes.pdf
