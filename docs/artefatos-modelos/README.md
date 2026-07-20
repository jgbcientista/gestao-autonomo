# Artefatos dos modelos treinados — SBESC 2026

Esta pasta recebe os **artefatos do ensemble de IA** descritos no artigo
(Seção "Motor de Decisão por IA"). Eles **não são versionados vazios**: são
gerados de forma **determinística** (semente fixa = 42) por um único comando.

## Como gerar

Em uma máquina com Python 3.10+ (a máquina de desenvolvimento atual não tem
Python instalado):

```bash
pip install -r ../requirements.txt
python ../simulacao-resultados-parciais.py
```

## O que é gerado aqui

| Arquivo | Conteúdo |
|---------|----------|
| `isolation_forest.joblib` | Isolation Forest treinado (peso 0,4 no ensemble) |
| `random_forest.joblib` | Random Forest treinado (peso 0,3) |
| `mlp_neural_net.joblib` | Rede neural multicamada (64→32, ReLU; peso 0,3) |
| `scaler.joblib` | `StandardScaler` ajustado no conjunto de treino |
| `ensemble_params.json` | Normalização do IF (lo/hi), pesos e limiar de decisão |
| `dataset_sintetico.csv` | ~50.000 registros rotulados (14 critérios + rótulo) |
| `metrics.json` | Acurácia, precisão, recall, F1, AUC, FPR, 5-fold e latências |

## Reprodutibilidade

O script fixa a semente em 42 e particiona 70/30 de forma estratificada,
reproduzindo os números do artigo (acurácia 98,1%, precisão 83,0%, recall
77,6%, F1 80,2%, AUC 0,99, FPR 0,8%; validação cruzada 5-fold: acurácia
98,2%±0,1% e F1 80,5%±0,8%; latência do ensemble ≈33 ms/req., dependente da
máquina). Pequenas variações podem ocorrer conforme a versão do scikit-learn.

> Observação de integridade científica: os dados são **sintéticos** e os
> resultados são **preliminares** — caracterizam viabilidade técnica, não um
> estudo de campo.
