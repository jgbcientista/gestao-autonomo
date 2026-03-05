from pydantic_settings import BaseSettings
import os


class Settings(BaseSettings):
    app_name: str = "AI Risk Analysis Service"
    model_path: str = os.path.join(os.path.dirname(os.path.dirname(__file__)), "models_saved")
    risk_threshold_low: float = 0.3
    risk_threshold_medium: float = 0.6
    risk_threshold_high: float = 0.8

    class Config:
        env_prefix = "AI_"


settings = Settings()
