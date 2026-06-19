# Changelog da Revisão — Qualificação (doc-qualificacao-ifba-REVISADO.docx)

Resposta ponto a ponto ao parecer do Prof. Cleber (`feature-professor.md`) e aos comentários nas imagens `*-ajuste.jpg`.
Arquivo original preservado: `doc-qualificacao-ifba.docx`. Entregáveis: `doc-qualificacao-ifba-REVISADO.docx` (+ `.pdf`, 46 págs).

## 1. Unificação do objeto de estudo (Ajuste Prioritário #1)
- **Cap. 4 (Desenvolvimento)** realinhado de *pipeline DevSecOps de liberação de deploy* → **desenvolvimento do sistema de autenticação contextual**: 4.1 arquitetura em 5 camadas (Aplicação → Identidade → Inteligência → Decisão → Registro), 4.3 "Fluxo de Autenticação Contextual", 4.4 IA na decisão de acesso, 4.6 "Critérios de Decisão de Acesso", 4.9 fluxo operacional de login.
- **Cap. 2 (Fundamentação)** reestruturado: 2.1 Autenticação e Controle de Acesso · 2.2 Autenticação Contextual e Adaptativa (RBA) · 2.3 IA para Detecção de Anomalias · 2.4 Blockchain.
- **Objetivos específicos (1.3)** reescritos para autenticação (eram SAST/DAST/SIEM).
- Lista de Siglas limpa (removidos SAST/DAST/CI-CD; incluídos RBA/XAI); menções residuais a "DevSecOps" removidas.

## 2. Inconsistências editoriais (Ajuste Prioritário #2)
- **Abstract**: palavras-chave "temperature control / Peltier effect" → keywords corretas do tema.
- **Introdução 1.1**: limitações com exemplos + referências (credential stuffing, phishing; NIST, Verizon); definição de "natureza dinâmica e contextual"; "Qual cenário?" explicitado; tecnologias retiradas da introdução; "tendências atuais" → justificativa com referências; frase incompleta sobre blockchain completada.
- **SUMÁRIO**: convertido em campo automático (TOC), restrito ao corpo (sem capa/pré-textuais).
- **Lista de Figuras**: números de página corrigidos.

## 3. Justificativa do blockchain (Ajuste Prioritário #4)
- §2.4 reescrita com justificativa robusta **vs. logs assinados / bancos append-only**, com reanálise crítica: concede que as alternativas garantem integridade/não repúdio, isola o diferencial real (não depender de um custodiante único + verificabilidade independente), assume o custo e delimita o escopo de necessidade.

## 4. Desenho experimental e resultados (Ajustes Prioritários #3 e #5)
- **Cap. 5** expandido: fonte e rotulagem dos dados, ética/LGPD, divisão 70/30 + validação cruzada estratificada (k=5), métricas e reprodutibilidade.
- **Tabela 1** — comparação com baselines (Senha+2FA, RBA-IP, keystroke, proposta).
- **Simulação executada** (`simulacao-resultados-parciais.py`, seed fixa): 50.000 registros sintéticos, 14 features, 95/5; ensemble Isolation Forest (40%) + Random Forest (30%) + rede neural (30%). Resultados reais no conjunto de teste (30%): **acurácia 98,1%, precisão 80,7%, recall 80,4%, F1 80,6%, AUC 0,99, FPR 1,0%**; 5-fold estável (acc 98,1%±0,1%, F1 80,5%±0,7%); latência do ensemble **~18,6 ms/requisição**. **Figura 5** gerada a partir desses resultados.
- Overclaims temperados: Discussão 5.4 ("confirmam a hipótese" → "não permitem confirmar de forma conclusiva", com caveat de dados sintéticos), Resumo ("inovadora"→"adaptativa"), "garante"→"busca preservar", "demonstram"→"indicam", "100%"→"todas as transações dos testes", "sempre/garantindo", "superando"→"com potencial de superar".

## 5. Coerência entre capítulos
- Natureza dos dados: **sintéticos** (Cap. 3 dizia "dados reais, 90 dias").
- Algoritmos: **ensemble IF + RF + Deep Learning** (Cap. 5 dizia "Random Forest e Gradient Boosting").
- Blockchain: **Hyperledger Fabric** em todo o texto (Cap. 3 usava Besu/IBFT).
- Validação cruzada: **k=5** (Cap. 5 dizia k=10).
- "85%" sem fonte → "espera-se que a maior parte…".

## 6. Padrão ABNT
- Fonte **Arial 12** unificada (havia Times New Roman, Georgia e Arial MT misturados).
- Margens 3 cm (esq./sup.) e 2 cm (dir./inf.); **entrelinha 1,5** no corpo (referências em espaço simples); texto justificado.

## 7. Referências
- Adicionadas: Killourhy & Maxion (2009), Liu/Ting/Zhou (2008), Rose et al./Zero Trust (2020), Stylios et al. (2021), Wiefling et al. (2019, 2023), Gama et al. (2014), NIST SP 800-63B (2017), Verizon DBIR (2024). Lista ordenada alfabeticamente.

## Pendente (depende do autor)
- Substituir os **dados sintéticos por dados reais** quando disponíveis e revalidar os números — a estrutura, a Tabela 1 e a simulação reprodutível já estão prontas para recebê-los.
