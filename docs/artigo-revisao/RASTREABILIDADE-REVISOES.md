# Rastreabilidade: avaliações SBESC 2026 (#33972) → versão 2

Artigo rejeitado em 24/09/2026. A versão submetida continua intacta em `docs/artigo/`.
A versão revisada está em `docs/artigo-revisao/artigo-v2-{pt,en}.tex` (+ PDFs).

Status: ✅ feito na v2 (itens 1 e 2) · 🔧 depende de mudança no código (item 3)

Experimentos do item 2: `docs/experimentos-revisao.py` → `docs/artefatos-modelos/experimentos-revisao.json` (~6,5 min). Usa o dataset publicado e a mesma divisão treino/teste; a linha de base reproduz F1 0,802.

## Revisor 1 (confiança alta, Strong Reject)

| # | Crítica | O que foi feito | Status |
|---|---------|-----------------|--------|
| 1.1 | Blockchain não existe no artefato (deps do Fabric comentadas, `HyperledgerFabricService` simulado, registro em PostgreSQL) | Conferido no código: procede. Camada de auditoria reescrita como **projeto** (resumo, contribuições, Seç. III "Estado do protótipo", Seç. V, VII, conclusão); Tabelas I e II passam de ✓ para "Proj."; removida toda alegação de imutabilidade/validação | ✅ |
| 1.2 | Frases da Seç. VII sem suporte (testes de imutabilidade, validação criptográfica, "poucas centenas de ms com registro", Keycloak validado, ELK/Grafana) | Removidas; a latência agora é só a *offline*, discriminada (4,0 + 29,2 + 0,05 ms, sequencial), e o texto declara o que não foi medido | ✅ |
| 1.3 | Caminho Full Paper: Fabric real com ≥2 orgs, latência de commit, vazão, teste de adulteração | Descrito como próximo passo (Seç. V e conclusão) | 🔧 |
| 1.4 | Bandas 0,3/0,7 nunca avaliadas; limiar único 0,54 cai na faixa de MFA | Texto explicita isso (Seç. IV) e que as bandas são parâmetros de projeto, não calibradas. Modelo de ameaças agora diz que a faixa de MFA pressupõe que o adversário não tem o 2º fator. **Tabela V** (2x3): legítimos 97,3 / 2,6 / 0,1%; ataques 7,3 / 30,1 / 62,5%; taxa de atrito 2,7%, passagem silenciosa 7,3%. ⚠️ O revisor obteve 11,3% liberados e 47,6% bloqueados. O nosso pipeline, com o dataset publicado, dá 7,3% e 62,5%. A diferença provavelmente vem da versão do scikit-learn (ele usou a 1.8); mencionar numa resposta | ✅ |
| 1.5 | Eq. (1) e bandas ≠ `ServicoScoreConfianca` (0,4 H + 0,3 IA + 0,2 R + 0,1 X) | Divergência declarada na Seç. III (os modelos avaliados são os do pipeline Python; o protótipo Java usa outro escore e outro conjunto de atributos) | ✅ texto · 🔧 alinhar o código |
| 1.6 | Rótulos "revisados por especialistas" | Corrigido: o rótulo é o ramo do gerador | ✅ |
| 1.7 | Distribuições "observadas em produção" | Corrigido: constantes escolhidas pelos autores; 95/5 é hipótese de trabalho | ✅ |
| 1.8 | Hora "circular" é gaussiana truncada | Corrigido (Seç. IV e VI) | ✅ |
| 1.9 | MLP não tem dropout | Corrigido (Seç. IV e Tabela de hiperparâmetros) | ✅ |
| 1.10 | CV no dataset inteiro, sem seleção de hiperparâmetro, reusando o limiar | CV refeita só no treino, com normalizador, modelos e limiar reajustados em cada fold: F1 80,3% ± 1,3%, AUC 0,990 ± 0,002 (substitui a CV antiga no texto e na Tabela IV) | ✅ |
| 1.11 | Perturbação cobre 2–4 features, não 5 | Corrigido | ✅ |
| 1.12 | Inferência é sequencial, não paralela | Corrigido | ✅ |
| 1.13 | Anomalias brandas dos legítimos usam as mesmas faixas dos ataques | Declarado | ✅ |
| 1.14 | Ruído em categóricas gera ASN = 0,94 e velocidade negativa | Declarado. Variante do gerador sem ruído em categóricas e contagens: F1 0,809, AUC 0,992, passagem silenciosa 6,9%; os valores implausíveis não inflavam as métricas | ✅ |
| 1.15 | Publicar hiperparâmetros | Nova Tabela II (hiperparâmetros) | ✅ |
| 1.16 | Ref. [12] (TypeFormer) está na Neural Computing and Applications 36:18531–18545, 2024 | Corrigida, com a lista completa de autores | ✅ |
| 1.17 | (positivo) Ensemble ganha ~3 pts de F1 sobre o melhor modelo isolado | Reproduzido (**Tabela VI**): IF 0,767 · RF 0,775 · MLP 0,774 · ensemble 0,802; FPR de 1,15% para 0,84%; PR-AUC de 0,86 para 0,90 | ✅ |

## Revisor 2 (confiança baixa, Borderline; aceitaria short/pôster)

| # | Crítica | O que foi feito | Status |
|---|---------|-----------------|--------|
| 2.1 | Dados sintéticos e rotulagem circular | Circularidade agora explícita na metodologia (rótulo = ramo do gerador), além das Limitações | ✅ texto |
| 2.2 | Testar outras proporções (não só 95/5) | **Tabela VII**: a AUC fica estável (0,99), mas com 1% de ataques o F1 cai para 0,63 e a passagem silenciosa sobe para 24,0% | ✅ |
| 2.3 | Testar outros pesos do escore | 66 combinações: F1 de 0,767 a 0,820; 0,4/0,3/0,3 fica em 19.º lugar; as melhores dão 0,6–0,8 à IF e quase nada à rede neural. Relatado como análise de sensibilidade (escolher os pesos pelo teste seria ajustar ao teste) | ✅ · pendente: escolher os pesos numa partição de validação |
| 2.4 | Desempenho do blockchain sob carga | Texto declara que não foi medido | 🔧 depende da rede Fabric real |
| 2.5 | Robustez contra evasão por atacante que conhece as 14 features | **Tabela VIII**, com o atacante substituindo as features que controla por valores legítimos: imitar horário e ritmo derruba o recall de 77,6% para 50,7%; com proxy residencial, para 18,0%; com navegador anti-detecção, para 2,9%. Entrou como 4ª limitação e no resumo | ✅ · 🔧 critérios difíceis de forjar e defesas contra evasão |

## Outras correções feitas por auditoria própria do código (não apontadas pelos revisores)

- Removidas alegações de: microsserviço REST de IA em produção com fallback, retreinamento periódico, versionamento/rollback de modelos, ambiente Docker/Kubernetes do experimento. Nada disso existe no repositório.
- Afirmações de robustez sem medição ("aumenta a robustez a zero-day", "degrada de forma graciosa", "detectou cenários de credential stuffing e sequestro de sessão") trocadas por afirmações sustentadas pelos números.

## Achados no código para o item 3 (não entram no artigo)

- `ServicoDeepLearning`: o backpropagation só atualiza a camada de saída e usa a entrada bruta em vez das ativações ocultas.
- `determinarDecisao()` (PERMITIR/MFA/BLOQUEAR) nunca é chamada; nenhum login é bloqueado por risco.
- `BloqueioAutomaticoService` é `@Scheduled`, mas a aplicação não tem `@EnableScheduling`.
- `BlockchainService` sobrescreve o hash encadeado (`hlf_`+UUID ou `hashCode`), então a verificação de integridade não funciona. O timestamp usado no hash não é armazenado. Há condição de corrida no encadeamento (`@Async`).
- `/api/v1/mfa/validar` emite JWT só com e-mail + código TOTP, sem vínculo com a verificação de senha anterior.
- Tentativas de login com falha não são contadas nem auditadas.
