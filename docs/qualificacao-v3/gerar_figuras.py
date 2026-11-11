# -*- coding: utf-8 -*-
"""Gera as figuras da qualificacao v3 (sem titulo embutido: a legenda fica no texto, padrao ABNT).
   python -I gerar_figuras.py
"""
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "figuras")
os.makedirs(OUT, exist_ok=True)
plt.rcParams.update({"font.family": "DejaVu Sans", "font.size": 11})

AZUL, VERDE, LARANJA, ROXO, CINZA = "#1452b0", "#2e7d32", "#c0561f", "#5b3fa0", "#555555"
FUNDO = {AZUL: "#dbe7f8", VERDE: "#dcefdc", LARANJA: "#fbe3d6", ROXO: "#e8e1f5", CINZA: "#ececec"}


def caixa(ax, x, y, w, h, texto, cor, fs=10.5, bold=False, ls="-"):
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.02,rounding_size=0.08",
                                fc=FUNDO.get(cor, "#ffffff"), ec=cor, lw=1.8, ls=ls))
    ax.text(x + w / 2, y + h / 2, texto, ha="center", va="center", fontsize=fs,
            fontweight="bold" if bold else "normal", color="#1b1b1b", wrap=True)


def seta(ax, x1, y1, x2, y2, cor="#444444", ls="-", lw=1.6):
    ax.add_patch(FancyArrowPatch((x1, y1), (x2, y2), arrowstyle="-|>", mutation_scale=14,
                                 color=cor, lw=lw, ls=ls))


def salvar(fig, nome):
    fig.savefig(os.path.join(OUT, nome), dpi=200, bbox_inches="tight", facecolor="white")
    plt.close(fig)


# Figura 1 - Etapas da DSR (Peffers et al., 2007) mapeadas nos capitulos
def fig_dsr():
    fig, ax = plt.subplots(figsize=(11.5, 2.9))
    ax.set_xlim(0, 11.5); ax.set_ylim(0.9, 3.5); ax.axis("off")
    etapas = [("1. Identificação\ndo problema", "Cap. 1"),
              ("2. Objetivos\nda solução", "Cap. 1"),
              ("3. Projeto e\ndesenvolvimento", "Cap. 2, 3 e 4"),
              ("4. Demons-\ntração", "Cap. 4"),
              ("5. Avaliação", "Cap. 5"),
              ("6. Comunicação", "Cap. 6 e artigos")]
    w, gap = 1.62, 0.3
    for i, (t, c) in enumerate(etapas):
        x = 0.15 + i * (w + gap)
        caixa(ax, x, 1.45, w, 1.0, t, AZUL, fs=9.8)
        ax.text(x + w / 2, 1.15, c, ha="center", va="center", fontsize=9.5, color=CINZA, style="italic")
        if i < len(etapas) - 1:
            seta(ax, x + w + 0.02, 1.95, x + w + gap - 0.02, 1.95)
    # iteracao avaliacao -> projeto
    x3 = 0.15 + 2 * (w + gap) + w / 2
    x5 = 0.15 + 4 * (w + gap) + w / 2
    ax.add_patch(FancyArrowPatch((x5, 2.47), (x3, 2.47), connectionstyle="arc3,rad=0.35",
                                 arrowstyle="-|>", mutation_scale=14, color=LARANJA, lw=1.6, ls="--"))
    ax.text((x3 + x5) / 2, 3.32, "iteração: os resultados da avaliação realimentam o projeto",
            ha="center", fontsize=9.5, color=LARANJA, style="italic")
    salvar(fig, "fig1-dsr.png")


# Figura 2 - Arquitetura em camadas, com o estado de cada uma no prototipo
def fig_arquitetura():
    fig, ax = plt.subplots(figsize=(11, 6.6))
    ax.set_xlim(0, 11); ax.set_ylim(0, 6.6); ax.axis("off")
    estados = {"impl": (VERDE, "implementada"), "parc": (LARANJA, "parcial"), "proj": (ROXO, "projetada")}
    camadas = [
        ("C1 — Captura de contexto", "Que contexto acompanha a tentativa?",
         "requisição de login → vetor com 14 critérios", "parc"),
        ("C2 — Identidade", "Quem alega se autenticar?",
         "credencial → identidade verificada (Keycloak, OAuth 2.0 / OIDC, JWT)", "parc"),
        ("C3 — Avaliação de risco", "Quão provável é que seja legítimo?",
         "14 critérios → escore S ∈ [0, 1] (ensemble IF + RF + MLP)", "parc"),
        ("C4 — Decisão adaptativa", "O que fazer com a tentativa?",
         "escore → permitir / exigir MFA / bloquear (faixas 0,3 e 0,7)", "parc"),
        ("C5 — Auditoria verificável", "Como provar a terceiros o que foi decidido?",
         "decisão + hash dos critérios + escore → registro em blockchain permissionada", "proj"),
    ]
    h, gap, x0, w = 0.92, 0.22, 0.2, 8.5
    for i, (nome, perg, io, est) in enumerate(camadas):
        y = 6.6 - 0.35 - (i + 1) * h - i * gap
        cor = estados[est][0]
        ax.add_patch(FancyBboxPatch((x0, y), w, h, boxstyle="round,pad=0.02,rounding_size=0.08",
                                    fc=FUNDO[AZUL], ec=AZUL, lw=1.8))
        ax.text(x0 + 0.2, y + h * 0.70, nome, fontsize=11.5, fontweight="bold", color="#0d2f66", va="center")
        ax.text(x0 + 4.35, y + h * 0.70, perg, fontsize=10, style="italic", color="#333333", va="center")
        ax.text(x0 + 0.2, y + h * 0.28, io, fontsize=9.8, color="#222222", va="center")
        ax.add_patch(FancyBboxPatch((x0 + w + 0.25, y + 0.22), 1.75, h - 0.44,
                                    boxstyle="round,pad=0.02,rounding_size=0.12", fc=FUNDO[cor], ec=cor, lw=1.6))
        ax.text(x0 + w + 0.25 + 0.875, y + h / 2, estados[est][1], ha="center", va="center",
                fontsize=10, color=cor, fontweight="bold")
        if i < len(camadas) - 1:
            seta(ax, x0 + w / 2, y - 0.01, x0 + w / 2, y - gap + 0.01)
    ax.text(x0 + w + 1.125, 6.42, "estado no protótipo", ha="center", fontsize=9.5, color=CINZA)
    salvar(fig, "fig2-arquitetura.png")


# Figura 3 - Diagrama de sequencia do fluxo de autenticacao (arquitetura-alvo)
def fig_sequencia():
    fig, ax = plt.subplots(figsize=(10, 7.6))
    ax.set_xlim(0, 10); ax.set_ylim(0, 7.6); ax.axis("off")
    atores = ["Usuário", "Front-end", "Back-end\n(C1, C4)", "Keycloak\n(C2)", "Serviço de\nrisco (C3)", "Rede Fabric\n(C5)"]
    cores = [AZUL, AZUL, VERDE, VERDE, LARANJA, ROXO]
    xs = [0.8 + i * 1.68 for i in range(len(atores))]
    for x, a, c in zip(xs, atores, cores):
        caixa(ax, x - 0.72, 6.75, 1.44, 0.72, a, c, fs=9.5, bold=True, ls="--" if "Fabric" in a else "-")
        ax.plot([x, x], [0.25, 6.75], ls=(0, (4, 3)), color="#9a9a9a", lw=1)
    msgs = [(0, 1, "credenciais", False), (1, 2, "login + dados de contexto", False),
            (2, 3, "valida credencial (OIDC)", False), (3, 2, "token / identidade", True),
            (2, 2, "extrai 14 critérios", False), (2, 4, "vetor de critérios", False),
            (4, 4, "ensemble → escore S", False), (4, 2, "escore S", True),
            (2, 2, "aplica faixas de decisão", False), (2, 5, "registra hash, S, decisão, versão", False),
            (5, 2, "confirmação da transação", True), (2, 1, "permitir / MFA / bloquear", True),
            (1, 0, "resultado do acesso", True)]
    y = 6.4
    for a, b, t, ret in msgs:
        if a == b:
            ax.add_patch(FancyArrowPatch((xs[a], y + 0.08), (xs[a] + 0.02, y - 0.22), connectionstyle="arc3,rad=-1.4",
                                         arrowstyle="-|>", mutation_scale=11, color="#333333", lw=1.3))
            ax.text(xs[a] + 0.32, y - 0.07, t, fontsize=9, color="#333333", va="center")
        else:
            seta(ax, xs[a], y, xs[b], y, cor="#222222", ls="--" if ret else "-", lw=1.4)
            ax.text((xs[a] + xs[b]) / 2, y + 0.1, t, ha="center", fontsize=9, color="#1b1b1b")
        y -= 0.47
    salvar(fig, "fig3-sequencia.png")


# Figura 4 - Processo de treinamento e avaliacao efetivamente executado
def fig_treinamento():
    fig, ax = plt.subplots(figsize=(11, 5.4))
    ax.set_xlim(0, 11); ax.set_ylim(0, 5.4); ax.axis("off")
    w, h = 1.95, 0.95
    caixa(ax, 0.1, 3.9, w, h, "Gerador sintético\n50.000 registros\n(95% / 5%)", CINZA, fs=9.5)
    caixa(ax, 2.45, 3.9, w, h, "Divisão estratificada\n70% treino\n30% teste", AZUL, fs=9.5)
    caixa(ax, 4.8, 3.9, w, h, "Padronização\n(z-score ajustado\nsó no treino)", AZUL, fs=9.5)
    seta(ax, 0.1 + w, 4.375, 2.45, 4.375); seta(ax, 2.45 + w, 4.375, 4.8, 4.375)
    # tres modelos
    ys = [4.15, 2.95, 1.75]
    nomes = ["Isolation Forest\n(não supervisionado)", "Random Forest\n(supervisionado)", "Rede neural MLP\n(supervisionado)"]
    for yy, n in zip(ys, nomes):
        caixa(ax, 7.15, yy - 0.05, 1.85, 0.8, n, VERDE, fs=9)
        seta(ax, 4.8 + w, 4.375, 7.15, yy + 0.35)
    caixa(ax, 9.25, 2.8, 1.65, 1.1, "Combinação\nponderada\n(Eq. 1)", LARANJA, fs=9.5)
    for yy in ys:
        seta(ax, 9.0, yy + 0.35, 9.25, 3.35)
    caixa(ax, 9.25, 1.0, 1.65, 1.1, "Limiar τ\n(máx. F1\nno treino)", LARANJA, fs=9.5)
    seta(ax, 10.075, 2.8, 10.075, 2.1)
    caixa(ax, 6.35, 0.15, 2.4, 0.9, "Avaliação final no\nteste (30%, inédito)", ROXO, fs=9.5)
    seta(ax, 9.25, 1.3, 8.75, 0.75)
    caixa(ax, 2.45, 1.6, 3.3, 1.2, "Validação cruzada estratificada\n(5 folds, só no treino): modelos,\npadronização e limiar reajustados\nem cada fold", ROXO, fs=9)
    seta(ax, 3.425, 3.9, 3.6, 2.8, ls="--")
    ax.text(0.15, 0.5, "Sementes aleatórias fixas;\nhiperparâmetros definidos a priori\n(Tabela 6).", fontsize=9,
            color=CINZA, style="italic")
    salvar(fig, "fig4-treinamento.png")


# Figura 5 - Estrutura do registro de auditoria (projeto da camada C5)
def fig_registro():
    fig, ax = plt.subplots(figsize=(12, 3.9))
    ax.set_xlim(0, 12); ax.set_ylim(0, 3.9); ax.axis("off")
    regs = [("Registro n−1", "a91f…", "07c2…", "u_4e1d…", "0,12", "permitir"),
            ("Registro n", "07c2…", "5bd0…", "u_9a03…", "0,81", "bloquear"),
            ("Registro n+1", "5bd0…", "e3a7…", "u_4e1d…", "0,44", "exigir MFA")]
    w = 3.6
    for i, (t, ant, hsh, uid, s, d) in enumerate(regs):
        x = 0.1 + i * (w + 0.45)
        ax.add_patch(FancyBboxPatch((x, 0.2), w, 3.4, boxstyle="round,pad=0.02,rounding_size=0.08",
                                    fc="white", ec=ROXO, lw=1.8))
        ax.add_patch(FancyBboxPatch((x + 0.1, 2.95), w - 0.2, 0.5, boxstyle="round,pad=0.01,rounding_size=0.05",
                                    fc=ROXO, ec=ROXO))
        ax.text(x + w / 2, 3.2, t, ha="center", va="center", color="white", fontweight="bold")
        linhas = [("hash_anterior", ant), ("hash_registro", hsh), ("id_pseudônimo", uid),
                  ("hash_critérios", "SHA-256(…)"), ("escore", s), ("decisão", d),
                  ("versão_modelo", "v1.0"), ("instante", "2026-11-11T09:14Z")]
        for k, (a, b) in enumerate(linhas):
            yy = 2.62 - k * 0.3
            ax.text(x + 0.2, yy, a + ":", fontsize=8.8, family="DejaVu Sans Mono", color="#333333")
            ax.text(x + 1.75, yy, b, fontsize=8.8, family="DejaVu Sans Mono",
                    color=ROXO if a.startswith("hash_") else "#111111")
        if i < 2:
            seta(ax, x + w + 0.02, 2.5, x + w + 0.43, 2.5, cor=ROXO)
    salvar(fig, "fig5-registro-auditoria.png")


if __name__ == "__main__":
    fig_dsr(); fig_arquitetura(); fig_sequencia(); fig_treinamento(); fig_registro()
    print("ok:", sorted(os.listdir(OUT)))
