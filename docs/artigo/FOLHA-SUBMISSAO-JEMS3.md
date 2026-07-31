# Folha de submissão — SBESC 2026 (JEMS3)

> **Prazo firme: 2 de agosto de 2026 (BRT).** Sistema: https://jems3.sbc.org.br/events/628
> **Arquivo a enviar: `artigo-sbesc2026-en.pdf`** (IEEE two-column, 6 páginas exatas).
> Idioma escolhido: **inglês** → publicação pela IEEE CPS e indexação no **IEEE Xplore**.

Tudo abaixo é texto puro, pronto para copiar e colar nos campos do JEMS3.

---

## 1. Track / Tipo de submissão

- **Evento:** SBESC 2026 — XVI Symposium on Computing Systems Engineering
- **Trilha:** Main Track (trilha principal) — **Full Paper**
- **Tópicos que mais se encaixam** (marque os que o formulário permitir):
  - Cyber-Physical Systems and **Cybersecurity**
  - **Embedded Systems and Artificial Intelligence**
  - Internet of Things and Ubiquitous Computing
  - Distributed and Multicore Systems

## 2. Title

```
Autonomous Contextual Authentication for Web Applications Combining an AI Ensemble and Permissioned Blockchain Auditing
```

## 3. Abstract (texto puro, 1 parágrafo)

```
Traditional authentication mechanisms based on static passwords or fixed multi-factor authentication (MFA) remain widely deployed, yet they ignore the behavioral and environmental context of each access attempt and are increasingly exploited by credential stuffing, phishing, and account-takeover attacks. This paper presents an autonomous contextual authentication system for web applications that combines a machine-learning ensemble with permissioned-blockchain auditing. Each login is described by fourteen contextual and behavioral features, which are scored by an ensemble of Isolation Forest, Random Forest, and a multilayer neural network; the resulting risk score drives a three-band adaptive decision (grant, step-up to MFA, or block). Every decision is recorded immutably on a Hyperledger Fabric ledger, yielding a verifiable, non-repudiable audit trail whose necessity over signed logs and append-only databases is explicitly justified. In a controlled evaluation on a synthetic dataset of about 50,000 login records (95% legitimate, 5% suspicious), the ensemble reached 98.1% accuracy, 80.2% F1-score, and a 0.99 ROC-AUC on a held-out test set, with a 0.8% false-positive rate and an inference latency of roughly 33 ms per request. These results are preliminary and rely on synthetic data; they characterize the technical feasibility of the approach rather than a field study, and are to be confirmed with real data.
```

## 4. Keywords

```
contextual authentication; risk-based authentication; machine learning; ensemble; anomaly detection; blockchain; audit trail; information security
```

Se o campo aceitar apenas 5, use: `contextual authentication; risk-based authentication; machine learning ensemble; blockchain audit trail; information security`

## 5. Autores (nesta ordem)

**Autor 1 — autor de contato / apresentador**
- Nome: `João Guedes de Brito`
- E-mail: `joaoguedesdebrito@gmail.com`
- Instituição: `Instituto Federal da Bahia (IFBA)`
- Programa: `Programa de Pós-Graduação em Engenharia de Sistemas e Produtos (PPGESP)`
- Cidade/Estado/País: `Salvador, Bahia, Brasil`

**Autor 2 — orientador**
- Nome: `Cleber Jorge Lira de Santana`
- E-mail: `cleber.santana@ifba.edu.br`
- Instituição: `Instituto Federal da Bahia (IFBA)`
- Programa: `Programa de Pós-Graduação em Engenharia de Sistemas e Produtos (PPGESP)`
- Cidade/Estado/País: `Salvador, Bahia, Brasil`

> No JEMS3 marque **João** como *corresponding author* e como quem vai **apresentar** o trabalho.

## 6. Passo a passo no JEMS3

1. Acesse https://jems3.sbc.org.br/events/628 e faça login com a conta SBC (crie uma se ainda não tiver — leva 2 min e o e-mail de confirmação pode demorar; **não deixe para o dia 2**).
2. `Submit paper` → selecione a trilha **SBESC 2026 — Main Track (Full Paper)**.
3. Cole **Title**, **Abstract** e **Keywords** das seções 2–4 acima.
4. Cadastre os **dois autores** (seção 5), na ordem indicada. O JEMS3 busca o coautor pelo e-mail: se `cleber.santana@ifba.edu.br` já tiver conta SBC, ele aparece na busca; se não, cadastre manualmente.
5. Marque os **tópicos** da seção 1.
6. **Upload do PDF:** `docs/artigo/artigo-sbesc2026-en.pdf`.
7. **Finalize/submeta** — no JEMS3 não basta salvar o rascunho; confirme que o status ficou **"submitted"** e que chegou o e-mail de confirmação.
8. Guarde o **número do paper** que o sistema gerar.

## 7. Checklist final antes de clicar em submeter

- [x] **6 páginas** (limite do SBESC para full paper) — confirmado no PDF compilado.
- [x] **IEEE two-column** (IEEEtran, template oficial de conferência).
- [x] **Sem numeração de página** — padrão IEEE de conferência.
- [x] **Link do dataset resolve:** repositório público e `docs/artefatos-modelos/` no ar (dataset, 3 modelos, scaler, `metrics.json`).
- [x] **Números do artigo batem com o `metrics.json` reproduzível** (98,1% acurácia · 83,0% precisão · 77,6% recall · 80,2% F1 · AUC 0,99 · FPR 0,8%).
- [x] **Limitações declaradas** — o texto diz explicitamente que os dados são sintéticos e os resultados preliminares.
- [ ] E-mails dos dois autores conferidos no PDF final.
- [ ] Status **"submitted"** no JEMS3 + e-mail de confirmação recebido.

## 8. Depois da submissão

- **Notificação:** 02/09/2026
- **Câmera-ready:** 04/10/2026 (haverá formulário de copyright do IEEE, por ser artigo em inglês)
- **Evento:** 24–27/11/2026, Salvador/BA — exige registro/inscrição de pelo menos um autor para publicação nos anais.
