# -*- coding: utf-8 -*-
# Experimentos da revisao pos-SBESC 2026 (#33972), respondendo aos revisores.
# Uso:  python experimentos-revisao.py        (~ alguns minutos)
# Saida: ./artefatos-modelos/experimentos-revisao.json + tabelas no terminal.
#
#  E1  decisao em tres faixas (0,3 / 0,7): tabela 2x3, taxa de atrito, passagem silenciosa
#  E2  ensemble x cada modelo isolado (mesmo pipeline, limiar proprio por max F1 no treino)
#  E3  sensibilidade aos pesos do ensemble (grade de passo 0,1)
#  E4  validacao cruzada aninhada so no treino (limiar escolhido dentro de cada fold)
#  E5  sensibilidade a proporcao de ataques (1%, 2%, 5%, 10%, 20%)
#  E6  gerador corrigido (ruido so em variaveis continuas, sem valores fisicamente impossiveis)
#  E7  evasao adversarial: atacante que conhece as 14 features imita as que controla
import json, os, itertools
import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import IsolationForest, RandomForestClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.model_selection import train_test_split, StratifiedKFold
from sklearn.metrics import (precision_score, recall_score, f1_score,
                             roc_auc_score, average_precision_score, confusion_matrix)

HERE = os.path.dirname(os.path.abspath(__file__))
ART = os.path.join(HERE, "artefatos-modelos")
COLS = ["hora", "dia_semana", "interv_ult_login", "freq_24h", "dist_geodesica",
        "tipo_rede", "asn", "vel_deslocamento", "fp_navegador", "so", "tipo_disp",
        "automacao", "padrao_digitacao", "falhas_recentes"]
W0 = (0.4, 0.3, 0.3)
THS = np.linspace(0.2, 0.8, 61)
LO, HI = 0.3, 0.7  # faixas de decisao do artigo


# ---------------------------------------------------------------- gerador
# Copia fiel de simulacao-resultados-parciais.py, parametrizada por proporcao de
# ataques e pela correcao do ruido (E6). Com fr=0.05 e ruido_v2=False reproduz o
# dataset publicado.
def gerar(fr=0.05, n=50000, seed=42, ruido_v2=False):
    rng = np.random.default_rng(seed)
    ns = int(n * fr); nl = n - ns

    def gen_legit(m):
        hora = np.clip(rng.normal(13, 3.2, m), 0, 23)
        dia = np.clip(rng.integers(0, 5, m) + rng.normal(0, 0.4, m), 0, 6)
        interval = rng.exponential(8, m)
        freq = rng.poisson(5, m).astype(float)
        dist = np.abs(rng.normal(0, 4, m))
        rede = rng.choice([0, 1, 2], m, p=[0.6, 0.35, 0.05]).astype(float)
        asn = (rng.random(m) > 0.03).astype(float)
        vel = np.abs(rng.normal(8, 8, m))
        fp = np.clip(rng.normal(0.92, 0.06, m), 0, 1)
        so = (rng.random(m) > 0.03).astype(float)
        disp = (rng.random(m) > 0.05).astype(float)
        auto = (rng.random(m) < 0.02).astype(float)
        dig = np.clip(rng.normal(0.89, 0.07, m), 0, 1)
        falhas = rng.poisson(0.3, m).astype(float)
        return np.column_stack([hora, dia, interval, freq, dist, rede, asn, vel, fp, so, disp, auto, dig, falhas])

    def corrupt(X, kmin=1, kmax=4):
        for i in range(X.shape[0]):
            k = rng.integers(kmin, kmax)
            for j in rng.choice(14, k, replace=False):
                if   j == 0: X[i, 0] = rng.choice([rng.uniform(2, 6), rng.uniform(21, 23.9)])
                elif j == 1: X[i, 1] = rng.choice([5, 6])
                elif j == 2: X[i, 2] = rng.exponential(0.4)
                elif j == 3: X[i, 3] = rng.poisson(18) + 5
                elif j == 4: X[i, 4] = rng.uniform(300, 4000)
                elif j == 5: X[i, 5] = rng.choice([2, 3])
                elif j == 6: X[i, 6] = 0
                elif j == 7: X[i, 7] = rng.uniform(200, 1200)
                elif j == 8: X[i, 8] = rng.uniform(0.35, 0.72)
                elif j == 9: X[i, 9] = 0
                elif j == 10: X[i, 10] = 0
                elif j == 11: X[i, 11] = 1
                elif j == 12: X[i, 12] = rng.uniform(0.35, 0.72)
                elif j == 13: X[i, 13] = rng.poisson(6) + 3
        return X

    Xl = gen_legit(nl)
    amask = rng.random(nl) < 0.05
    Xl[amask] = corrupt(Xl[amask], 1, 2)
    Xs = corrupt(gen_legit(ns), 2, 5)
    X = np.vstack([Xl, Xs]); y = np.concatenate([np.zeros(nl), np.ones(ns)]).astype(int)
    ruido = rng.normal(0, 0.30, X.shape) * X.std(0)
    if ruido_v2:
        # sem ruido em categoricas/contagens; continuas nao negativas truncadas em 0
        CONT = [0, 1, 2, 4, 7, 8, 12]
        mask = np.zeros(14, bool); mask[CONT] = True
        X = X + ruido * mask
        X[:, [2, 4, 7]] = np.clip(X[:, [2, 4, 7]], 0, None)
        X[:, 0] = np.clip(X[:, 0], 0, 23.99); X[:, 1] = np.clip(X[:, 1], 0, 6)
        X[:, [8, 12]] = np.clip(X[:, [8, 12]], 0, 1)
    else:
        X = X + ruido
    idx = rng.permutation(n)
    return X[idx], y[idx]


# ---------------------------------------------------------------- pipeline
class Pipeline:
    """Mesmo pipeline do artigo: scaler + IF + RF + MLP, ajustados no treino."""

    def __init__(self, fr, mlp_iter=220):
        self.fr, self.mlp_iter = fr, mlp_iter

    def fit(self, X, y):
        self.sc = StandardScaler().fit(X); Xs = self.sc.transform(X)
        self.iso = IsolationForest(n_estimators=100, contamination=self.fr, random_state=42).fit(Xs)
        s = -self.iso.score_samples(Xs); self.lo, self.hi = s.min(), s.max()
        self.rf = RandomForestClassifier(n_estimators=120, n_jobs=-1, random_state=42,
                                         class_weight="balanced").fit(Xs, y)
        self.mlp = MLPClassifier(hidden_layer_sizes=(64, 32), activation="relu",
                                 max_iter=self.mlp_iter, random_state=42).fit(Xs, y)
        return self

    def componentes(self, X):
        Xs = self.sc.transform(X)
        s_if = np.clip((-self.iso.score_samples(Xs) - self.lo) / (self.hi - self.lo), 0, 1)
        return s_if, self.rf.predict_proba(Xs)[:, 1], self.mlp.predict_proba(Xs)[:, 1]


def combina(c, w=W0):
    return w[0] * c[0] + w[1] * c[1] + w[2] * c[2]


def limiar(y, s):
    return float(max(THS, key=lambda t: f1_score(y, (s > t).astype(int), zero_division=0)))


def metricas(y, s, t):
    p = (s > t).astype(int)
    tn, fp, fn, tp = confusion_matrix(y, p, labels=[0, 1]).ravel()
    return dict(thr=round(t, 2), prec=precision_score(y, p, zero_division=0),
                rec=recall_score(y, p), f1=f1_score(y, p), fpr=fp / (fp + tn),
                auc=roc_auc_score(y, s), pr_auc=average_precision_score(y, s))


def faixas(y, s):
    """Tabela 2x3 das faixas + taxas derivadas."""
    out = {}
    for rot, nome in [(0, "legitimo"), (1, "suspeito")]:
        v = s[y == rot]
        out[nome] = dict(permitir=float(np.mean(v < LO)),
                         mfa=float(np.mean((v >= LO) & (v < HI))),
                         bloquear=float(np.mean(v >= HI)))
    out["taxa_atrito"] = 1 - out["legitimo"]["permitir"]          # legitimos desafiados ou bloqueados
    out["passagem_silenciosa"] = out["suspeito"]["permitir"]      # ataques liberados sem desafio
    return out


def split(X, y):
    return train_test_split(X, y, test_size=0.30, stratify=y, random_state=42)


def pct(x): return "%5.1f%%" % (100 * x)


R = {}

# ---------------------------------------------------------------- dados publicados
d = np.loadtxt(os.path.join(ART, "dataset_sintetico.csv"), delimiter=",", skiprows=1)
X, y = d[:, :14], d[:, 14].astype(int)
Xtr, Xte, ytr, yte = split(X, y)
P = Pipeline(0.05).fit(Xtr, ytr)
ctr, cte = P.componentes(Xtr), P.componentes(Xte)
s_tr, s_te = combina(ctr), combina(cte)
t0 = limiar(ytr, s_tr)
R["base"] = metricas(yte, s_te, t0)
print("== Base (deve reproduzir o artigo)  F1=%.3f AUC=%.3f thr=%.2f" % (R["base"]["f1"], R["base"]["auc"], t0))

# E1 ----------------------------------------------------------------
R["E1_faixas"] = faixas(yte, s_te)
f = R["E1_faixas"]
print("\n== E1 Faixas (teste)          permitir    MFA   bloquear")
for k in ["legitimo", "suspeito"]:
    print("   %-10s               %s  %s  %s" % (k, pct(f[k]["permitir"]), pct(f[k]["mfa"]), pct(f[k]["bloquear"])))
print("   taxa de atrito = %s | passagem silenciosa = %s" % (pct(f["taxa_atrito"]), pct(f["passagem_silenciosa"])))

# E2 ----------------------------------------------------------------
R["E2_modelos"] = {}
print("\n== E2 Modelos isolados x ensemble (teste; limiar = max F1 no treino)")
print("   %-12s thr   prec   rec    F1     FPR    AUC    PR-AUC" % "")
for nome, i in [("IF", 0), ("RF", 1), ("MLP", 2), ("Ensemble", None)]:
    a, b = (s_tr, s_te) if i is None else (ctr[i], cte[i])
    m = metricas(yte, b, limiar(ytr, a)); R["E2_modelos"][nome] = m
    print("   %-12s %.2f  %.3f  %.3f  %.3f  %.4f  %.3f  %.3f" % (nome, m["thr"], m["prec"], m["rec"], m["f1"], m["fpr"], m["auc"], m["pr_auc"]))

# E3 ----------------------------------------------------------------
# Analise de sensibilidade (nao selecao): cada combinacao usa limiar escolhido no treino.
grade = []
for a in range(11):
    for b in range(11 - a):
        w = (a / 10, b / 10, (10 - a - b) / 10)
        m = metricas(yte, combina(cte, w), limiar(ytr, combina(ctr, w)))
        grade.append(dict(w=w, f1=m["f1"], auc=m["auc"], pr_auc=m["pr_auc"]))
grade.sort(key=lambda g: -g["f1"])
pos = [i for i, g in enumerate(grade) if g["w"] == W0][0] + 1
R["E3_pesos"] = dict(n=len(grade), posicao_0433=pos, top5=grade[:5], pior=grade[-1],
                     f1_min=grade[-1]["f1"], f1_max=grade[0]["f1"],
                     f1_mediana=float(np.median([g["f1"] for g in grade])))
print("\n== E3 Pesos (%d combinacoes)  F1 min/mediana/max = %.3f / %.3f / %.3f" %
      (len(grade), grade[-1]["f1"], R["E3_pesos"]["f1_mediana"], grade[0]["f1"]))
print("   (0.4,0.3,0.3) fica em %d.o lugar; top-5:" % pos)
for g in grade[:5]:
    print("   w=%s  F1=%.3f AUC=%.3f" % (g["w"], g["f1"], g["auc"]))
print("   pior: w=%s F1=%.3f" % (grade[-1]["w"], grade[-1]["f1"]))

# E4 ----------------------------------------------------------------
skf = StratifiedKFold(5, shuffle=True, random_state=42); cv = []
for tri, vai in skf.split(Xtr, ytr):
    Pk = Pipeline(0.05).fit(Xtr[tri], ytr[tri])
    tk = limiar(ytr[tri], combina(Pk.componentes(Xtr[tri])))
    cv.append(metricas(ytr[vai], combina(Pk.componentes(Xtr[vai])), tk))
R["E4_cv_aninhada"] = {k: dict(media=float(np.mean([c[k] for c in cv])), dp=float(np.std([c[k] for c in cv])))
                       for k in ["prec", "rec", "f1", "fpr", "auc"]}
R["E4_cv_aninhada"]["limiares"] = [c["thr"] for c in cv]
e4 = R["E4_cv_aninhada"]
print("\n== E4 CV 5 folds so no treino (limiar por fold)  F1 = %.3f +- %.3f | AUC = %.3f +- %.3f | limiares %s" %
      (e4["f1"]["media"], e4["f1"]["dp"], e4["auc"]["media"], e4["auc"]["dp"], e4["limiares"]))

# E5 / E6 -----------------------------------------------------------
def cenario(fr, ruido_v2=False):
    Xa, ya = gerar(fr=fr, ruido_v2=ruido_v2)
    a_tr, a_te, b_tr, b_te = split(Xa, ya)
    Pa = Pipeline(fr).fit(a_tr, b_tr)
    st, se = combina(Pa.componentes(a_tr)), combina(Pa.componentes(a_te))
    return dict(metricas(b_te, se, limiar(b_tr, st)), faixas=faixas(b_te, se))

print("\n== E5 Proporcao de ataques      F1     AUC    PR-AUC  atrito  pass.silenc.")
R["E5_proporcao"] = {}
for fr in [0.01, 0.02, 0.05, 0.10, 0.20]:
    m = cenario(fr); R["E5_proporcao"][str(fr)] = m
    print("   %4.0f%%                       %.3f  %.3f  %.3f   %s  %s" %
          (fr * 100, m["f1"], m["auc"], m["pr_auc"], pct(m["faixas"]["taxa_atrito"]), pct(m["faixas"]["passagem_silenciosa"])))

m = cenario(0.05, ruido_v2=True); R["E6_gerador_corrigido"] = m
print("\n== E6 Gerador corrigido (5%%)  F1=%.3f AUC=%.3f PR-AUC=%.3f FPR=%.4f | atrito %s | pass.silenc. %s" %
      (m["f1"], m["auc"], m["pr_auc"], m["fpr"], pct(m["faixas"]["taxa_atrito"]), pct(m["faixas"]["passagem_silenciosa"])))

# E7 ----------------------------------------------------------------
# O atacante conhece as 14 features e substitui as que controla por valores
# sorteados da distribuicao LEGITIMA (linhas legitimas do treino). Niveis:
NIVEIS = {
    "N0 sem evasao": [],
    "N1 basico (horario, ritmo)": ["hora", "dia_semana", "interv_ult_login", "freq_24h", "falhas_recentes"],
    "N2 + proxy residencial": ["tipo_rede", "asn", "dist_geodesica", "vel_deslocamento"],
    "N3 + navegador anti-deteccao": ["fp_navegador", "so", "tipo_disp", "automacao"],
    "N4 + imita digitacao (todas)": ["padrao_digitacao"],
}
rng = np.random.default_rng(7)
legit_tr = Xtr[ytr == 0]
Xatk = Xte[yte == 1].copy(); leg_te = s_te[yte == 0]
ctrl = []; R["E7_evasao"] = {}
print("\n== E7 Evasao adversarial (ataques do teste; limiar binario %.2f e faixas)" % t0)
print("   %-30s recall  permitir  MFA    bloquear" % "")
for nome, novas in NIVEIS.items():
    ctrl += novas
    Xe = Xatk.copy()
    for c in ctrl:
        j = COLS.index(c); Xe[:, j] = legit_tr[rng.integers(0, len(legit_tr), len(Xe)), j]
    se = combina(P.componentes(Xe))
    fx = faixas(np.r_[np.zeros(len(leg_te)), np.ones(len(se))].astype(int), np.r_[leg_te, se])["suspeito"]
    R["E7_evasao"][nome] = dict(features=list(ctrl), recall=float(np.mean(se > t0)), **fx)
    print("   %-30s %s  %s   %s %s" % (nome, pct(np.mean(se > t0)), pct(fx["permitir"]), pct(fx["mfa"]), pct(fx["bloquear"])))

json.dump(R, open(os.path.join(ART, "experimentos-revisao.json"), "w"), indent=2, default=float)
print("\nSalvo em artefatos-modelos/experimentos-revisao.json")
