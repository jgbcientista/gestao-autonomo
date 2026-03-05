"""
Gera dados sinteticos de autenticacao e treina o modelo de risco.
Pode ser executado diretamente: python -m train.train_model
"""
import os
import sys
import numpy as np

# Adicionar raiz ao path para imports
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from app.models.risk_classifier import RiskClassifier
from app.config import settings


def generate_data(n_samples: int = 5000) -> tuple[np.ndarray, np.ndarray]:
    """Gera dados sinteticos de contexto de autenticacao."""
    rng = np.random.RandomState(42)

    hour_of_day = rng.randint(0, 24, n_samples)
    day_of_week = rng.randint(0, 7, n_samples)
    login_attempts = rng.poisson(1, n_samples)
    is_new_device = rng.binomial(1, 0.15, n_samples)
    is_new_location = rng.binomial(1, 0.1, n_samples)
    is_vpn = rng.binomial(1, 0.2, n_samples)
    is_tor = rng.binomial(1, 0.03, n_samples)
    session_duration = rng.exponential(300, n_samples)
    pages_per_session = rng.poisson(5, n_samples).astype(float)
    time_since_last_login = rng.exponential(48, n_samples)

    X = np.column_stack([
        hour_of_day, day_of_week, login_attempts,
        is_new_device, is_new_location, is_vpn, is_tor,
        session_duration, pages_per_session, time_since_last_login,
    ])

    # Gerar labels de risco baseado em regras
    risk = np.zeros(n_samples)
    risk += is_tor * 0.4
    risk += is_new_device * 0.15
    risk += is_new_location * 0.15
    risk += is_vpn * 0.05
    risk += (login_attempts > 3).astype(float) * 0.2
    risk += ((hour_of_day < 6) | (hour_of_day > 22)).astype(float) * 0.1
    risk += (time_since_last_login > 720).astype(float) * 0.1
    noise = rng.normal(0, 0.05, n_samples)
    risk = np.clip(risk + noise, 0, 1)

    # Binarizar: >0.5 = alto risco
    y = (risk > 0.4).astype(int)

    return X, y


def main():
    print("Gerando dados sinteticos...")
    X, y = generate_data()
    print(f"  Amostras: {len(y)}, Alto risco: {y.sum()}, Baixo risco: {(1-y).sum()}")

    clf = RiskClassifier(model_dir=settings.model_path)
    accuracy = clf.train(X, y)
    print(f"  Accuracy: {accuracy:.4f}")
    print(f"  Modelos salvos em: {settings.model_path}")


if __name__ == "__main__":
    main()
