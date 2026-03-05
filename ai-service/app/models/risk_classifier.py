import os
import logging
import numpy as np
import joblib
from sklearn.ensemble import RandomForestClassifier, VotingClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler

logger = logging.getLogger(__name__)


class RiskClassifier:
    """Ensemble classifier: RandomForest + DecisionTree + LogisticRegression."""

    def __init__(self, model_dir: str = "models_saved"):
        self.model_dir = model_dir
        self.model = None
        self.scaler = None
        self._loaded = False

    @property
    def is_loaded(self) -> bool:
        return self._loaded

    def _feature_names(self) -> list[str]:
        return [
            "hour_of_day",
            "day_of_week",
            "login_attempts_last_hour",
            "is_new_device",
            "is_new_location",
            "is_vpn",
            "is_tor",
            "session_duration_avg",
            "pages_per_session_avg",
            "time_since_last_login_hours",
        ]

    def load(self) -> bool:
        model_path = os.path.join(self.model_dir, "ensemble_model.joblib")
        scaler_path = os.path.join(self.model_dir, "scaler.joblib")
        if os.path.exists(model_path) and os.path.exists(scaler_path):
            self.model = joblib.load(model_path)
            self.scaler = joblib.load(scaler_path)
            self._loaded = True
            logger.info("Modelos carregados de %s", self.model_dir)
            return True
        logger.warning("Modelos nao encontrados em %s", self.model_dir)
        return False

    def train(self, X: np.ndarray, y: np.ndarray) -> float:
        self.scaler = StandardScaler()
        X_scaled = self.scaler.fit_transform(X)

        rf = RandomForestClassifier(n_estimators=100, random_state=42, max_depth=10)
        dt = DecisionTreeClassifier(random_state=42, max_depth=8)
        lr = LogisticRegression(random_state=42, max_iter=500)

        self.model = VotingClassifier(
            estimators=[("rf", rf), ("dt", dt), ("lr", lr)],
            voting="soft",
        )
        self.model.fit(X_scaled, y)

        accuracy = self.model.score(X_scaled, y)
        self._loaded = True

        os.makedirs(self.model_dir, exist_ok=True)
        joblib.dump(self.model, os.path.join(self.model_dir, "ensemble_model.joblib"))
        joblib.dump(self.scaler, os.path.join(self.model_dir, "scaler.joblib"))
        logger.info("Modelo treinado e salvo. Accuracy: %.4f", accuracy)
        return accuracy

    def predict(self, features: dict) -> tuple[float, float]:
        """Returns (risk_score, confidence)."""
        if not self._loaded:
            raise RuntimeError("Modelo nao carregado. Execute /train primeiro.")

        arr = np.array(
            [[features.get(f, 0) for f in self._feature_names()]]
        )
        arr_scaled = self.scaler.transform(arr)

        probas = self.model.predict_proba(arr_scaled)[0]
        # Class 1 = high risk
        risk_score = float(probas[1]) if len(probas) > 1 else float(probas[0])
        confidence = float(max(probas))
        return risk_score, confidence
