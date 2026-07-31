# SBESC 2026 — Artigo, PDFs e submissão

> ✅ **SUBMETIDO em 31/07/2026 — artigo nº `#33972`** no JEMS3.
> Trilha **SBESC 2026 / Full paper**, idioma **inglês**, arquivo `artigo-sbesc2026-en.pdf`.
> Tópicos: Cybersecurity · Embedded Artificial Intelligence, Internet of Intelligent Things ·
> Distributed Systems. Autores: João Guedes de Brito (contato) e Cleber Jorge Lira de Santana,
> ambos IFBA. Status no sistema: *Active*, com o PDF anexado.
> Acompanhar em: https://jems3.sbc.org.br/sbesc2026
>
> **Próximas datas:** notificação **02/09/2026** · câmera-ready **04/10/2026** ·
> evento **24–27/11/2026**, Salvador/BA.
> Prazo de submissão era 02/08/2026 (firm), após duas prorrogações (10/07 → 19/07 → 02/08).
> Histórico dos campos usados na submissão: **`FOLHA-SUBMISSAO-JEMS3.md`**.

**Arquivos do artigo (mesmo conteúdo, 3 formatos por idioma):**

| Idioma | PDF (submissão + leitura) | Word (editar/comentar) | LaTeX (fonte) |
|--------|---------------------------|------------------------|---------------|
| 🇧🇷 Português | `artigo-sbesc2026-pt.pdf` | `artigo-sbesc2026-pt.docx` | `artigo-sbesc2026-pt.tex` |
| 🇬🇧 Inglês | `artigo-sbesc2026-en.pdf` | `artigo-sbesc2026-en.docx` | `artigo-sbesc2026-en.tex` |

- **Os dois PDFs já estão no formato de submissão:** IEEE two-column (IEEEtran), **6 páginas
  exatas** cada, compilados a partir dos `.tex` com o MiKTeX local. Não é rascunho de coluna
  única — isso valeu para uma versão antiga deste arquivo.
- **O `.pdf` é a entrega canônica.** O `.docx` é uma versão editável "de trabalho", fiel ao
  texto mas não pixel-perfect no layout IEEE (a figura TikZ vira uma caixa simples e a
  paginação do Word difere) — sirva-o ao professor para comentários, nunca ao JEMS3.
- **PT → SBC OpenLib · EN → IEEE Xplore.** Na submissão **escolha apenas UM idioma**
  (o SBESC aceita um idioma por artigo). **Decisão tomada: inglês**, pelo IEEE Xplore.

---

## 1. Recompilar o PDF (só se editar o `.tex`)

**Local (MiKTeX já instalado nesta máquina):**
```
& "$env:LOCALAPPDATA\Programs\MiKTeX\miktex\bin\x64\pdflatex.exe" artigo-sbesc2026-en.tex
& "$env:LOCALAPPDATA\Programs\MiKTeX\miktex\bin\x64\pdflatex.exe" artigo-sbesc2026-en.tex
```
Rode **2x** para resolver referências e citações. Confira no fim do `.log`:
`Output written on artigo-sbesc2026-en.pdf (6 pages...)`.

**Alternativa (Overleaf):** https://www.overleaf.com → *New Project → Upload Project* → suba o
`.tex` → compilador em **pdfLaTeX** → *Recompile* → *Download PDF*.

> O artigo **não usa imagens externas** — a figura da arquitetura é desenhada em TikZ dentro do
> próprio `.tex`, então o arquivo único basta para compilar.

**Regenerar os `.docx`** (a partir do `.tex`, que é a fonte única): script `gen-docx.js`.

---

## 2. Estado atual — o que já está resolvido

- [x] **6 páginas completas** nos dois idiomas (limite do SBESC para full paper).
- [x] **Feedback do Prof. Cleber aplicado** (4 rodadas): introdução reforçada, +5 trabalhos
      correlatos reais, tabelas de comparação ampliadas, modelo de ameaças, justificativa dos
      eixos da Tabela II, limitações e ameaças à validade, trabalhos futuros expandidos.
- [x] **Dataset materializado e público.** `docs/artefatos-modelos/` está commitado e no ar em
      `github.com/jgbcientista/gestao-autonomo` (repositório **público**) — o link do rodapé do
      artigo resolve. Contém `dataset_sintetico.csv` (50k registros, 95/5%, 14 features),
      os 3 modelos `.joblib`, o scaler, `ensemble_params.json` e `metrics.json`.
- [x] **Métricas alinhadas ao `metrics.json` reproduzível:** 98,1% acurácia · 83,0% precisão ·
      77,6% recall · **80,2% F1** · AUC 0,99 · **FPR 0,8%** · CV 98,2%±0,1% · latência ~33 ms.
      *(Números antigos como "80,6% F1 / 18,6 ms" ficaram obsoletos — não use.)*
- [x] **E-mails dos autores confirmados:** `joaoguedesdebrito@gmail.com` e
      `cleber.santana@ifba.edu.br`; orientador entra como **coautor**.
- [x] **Honestidade dos resultados:** o texto declara explicitamente que os dados são sintéticos
      e os resultados preliminares — mantido de propósito, por integridade científica.
- [x] **Sem numeração de página** (padrão IEEE de conferência).

---

## 3. Submissão no JEMS3 — como foi feita

Campos usados (título, abstract em texto puro, keywords, autores, tópicos) e o passo a passo
completo: **`FOLHA-SUBMISSAO-JEMS3.md`**.

Resumo do caminho: https://jems3.sbc.org.br/sbesc2026 → login SBC → *Event tracks: Full paper*
→ metadados → cadastro dos 2 autores → tópicos → upload de `artigo-sbesc2026-en.pdf` na seção
*Files*. Registro concluído em 31/07/2026 como **#33972**, status *Active* com o PDF anexado.

> Na tela da submissão, **"Edit"** altera metadados com segurança; **"Withdraw" retira o artigo
> do evento** — não usar. O PDF pode ser substituído até 02/08/2026 23:59 (BRT).

---

## 4. Datas do SBESC 2026
- Submissão de artigos: **02/08/2026 (prazo firme)**
- Notificação: 02/09/2026
- Câmera-ready: 04/10/2026
- Evento: 24–27/11/2026, Salvador/BA
