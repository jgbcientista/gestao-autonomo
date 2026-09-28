# -*- coding: utf-8 -*-
# Gera a Figura 5 do documento de qualificacao a partir de artefatos-modelos/metrics.json
# e experimentos-revisao.json (nada digitado a mao).
import json, os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

HERE = os.path.dirname(os.path.abspath(__file__))
ART = os.path.join(HERE, "artefatos-modelos")
M = json.load(open(os.path.join(ART, "metrics.json")))
E = json.load(open(os.path.join(ART, "experimentos-revisao.json")))
cv = E["E4_cv_aninhada"]["f1"]

def br(v, casas=1):
    return ("%.*f" % (casas, v)).replace(".", ",")

AZUL, VERDE, ROXO, LARANJA, TITULO = "#1352B3", "#1F7A5A", "#5B3FA0", "#BF5720", "#1B2A4A"
plt.rcParams.update({"font.size": 11})
fig, (a, b) = plt.subplots(1, 2, figsize=(11.25, 4.885), dpi=200)

nomes = ["Acurácia", "Precisão", "Recall", "F1-score", "AUC"]
vals = [M["acc"], M["prec"], M["rec"], M["f1"], M["auc"]]
cores = [AZUL, VERDE, VERDE, VERDE, ROXO]
bars = a.bar(nomes, [v * 100 for v in vals], color=cores, width=0.6)
for r, v in zip(bars, vals):
    a.text(r.get_x() + r.get_width() / 2, v * 100 + 1.5, br(v * 100) + "%", ha="center", fontweight="bold")
a.set_ylim(0, 108); a.set_ylabel("%")
a.axhline(100, ls=":", lw=0.8, color="#999")
a.set_title("Desempenho do ensemble (conjunto de teste, 30%)", fontweight="bold", color=TITULO)
a.set_xlabel("FPR = %s%%  ·  CV 5 folds só no treino: F1 %s%% ± %s%%" %
             (br(M["fpr"] * 100), br(cv["media"] * 100), br(cv["dp"] * 100)),
             fontsize=9, color="#333", labelpad=8)

lat = [M["lat_if"], M["lat_rf"], M["lat_dl"], M["lat_ens"]]
bars = b.bar(["Isolation\nForest", "Random\nForest", "Rede\nneural", "Ensemble\n(sequencial)"], lat,
             color=[AZUL, AZUL, AZUL, LARANJA], width=0.6)
for r, v in zip(bars, lat):
    b.text(r.get_x() + r.get_width() / 2, v + 0.8, br(v, 2 if v < 1 else 1) + " ms", ha="center", fontweight="bold")
b.set_ylim(0, max(lat) * 1.25); b.set_ylabel("ms / requisição")
b.set_title("Latência de inferência offline (scikit-learn)", fontweight="bold", color=TITULO)
b.text(0.98, 0.93, "mediana de 300 chamadas, 1 amostra por vez;\nvalor dependente da máquina", transform=b.transAxes,
       ha="right", va="top", fontsize=9, style="italic", color="#555")

for ax in (a, b):
    ax.spines[["top", "right"]].set_visible(False)
fig.tight_layout()
out = os.path.join(HERE, "figura5-resultados.png")
fig.savefig(out)
print("ok", out)
