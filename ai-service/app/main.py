import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .config import settings
from .schemas.prediction import PredictionRequest, PredictionResponse, TrainResponse, HealthResponse
from .services.prediction_service import predict, get_classifier

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title=settings.app_name, version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup():
    clf = get_classifier()
    if not clf.load():
        logger.warning("Nenhum modelo pre-treinado encontrado. Execute POST /train.")


@app.get("/health", response_model=HealthResponse)
def health():
    clf = get_classifier()
    return HealthResponse(
        status="UP",
        modelo_carregado=clf.is_loaded,
    )


@app.post("/predict", response_model=PredictionResponse)
def predict_risk(request: PredictionRequest):
    return predict(request)


@app.post("/train", response_model=TrainResponse)
def retrain_model():
    import importlib
    train_mod = importlib.import_module("train.train_model")

    X, y = train_mod.generate_data()
    clf = get_classifier()
    accuracy = clf.train(X, y)
    return TrainResponse(
        status="OK",
        mensagem="Modelo treinado com sucesso",
        acuracia=round(accuracy, 4),
        amostras_utilizadas=len(y),
    )
