import logging
from ..config import settings
from ..models.risk_classifier import RiskClassifier
from ..schemas.prediction import PredictionRequest, PredictionResponse

logger = logging.getLogger(__name__)

classifier = RiskClassifier(model_dir=settings.model_path)


def get_classifier() -> RiskClassifier:
    return classifier


def classificar_nivel_risco(score: float) -> str:
    if score >= settings.risk_threshold_high:
        return "CRITICO"
    elif score >= settings.risk_threshold_medium:
        return "ALTO"
    elif score >= settings.risk_threshold_low:
        return "MEDIO"
    return "BAIXO"


def decidir_acao(nivel_risco: str) -> str:
    decisoes = {
        "BAIXO": "PERMITIR",
        "MEDIO": "PERMITIR_COM_MONITORAMENTO",
        "ALTO": "EXIGIR_MFA",
        "CRITICO": "BLOQUEAR",
    }
    return decisoes.get(nivel_risco, "PERMITIR")


def identificar_fatores_risco(request: PredictionRequest) -> list[str]:
    fatores = []
    if request.usa_tor:
        fatores.append("Conexao via Tor detectada")
    if request.usa_vpn:
        fatores.append("Uso de VPN detectado")
    if request.novo_dispositivo:
        fatores.append("Dispositivo desconhecido")
    if request.nova_localizacao:
        fatores.append("Localizacao incomum")
    if request.tentativas_login_ultima_hora > 3:
        fatores.append(f"Multiplas tentativas de login ({request.tentativas_login_ultima_hora})")
    if request.hora_do_dia < 6 or request.hora_do_dia > 22:
        fatores.append("Horario incomum de acesso")
    if request.horas_desde_ultimo_login > 720:
        fatores.append("Inatividade prolongada (>30 dias)")
    return fatores


def predict(request: PredictionRequest) -> PredictionResponse:
    features = {
        "hour_of_day": request.hora_do_dia,
        "day_of_week": request.dia_da_semana,
        "login_attempts_last_hour": request.tentativas_login_ultima_hora,
        "is_new_device": int(request.novo_dispositivo),
        "is_new_location": int(request.nova_localizacao),
        "is_vpn": int(request.usa_vpn),
        "is_tor": int(request.usa_tor),
        "session_duration_avg": request.duracao_media_sessao,
        "pages_per_session_avg": request.paginas_por_sessao_media,
        "time_since_last_login_hours": request.horas_desde_ultimo_login,
    }

    risk_score, confidence = classifier.predict(features)
    nivel_risco = classificar_nivel_risco(risk_score)
    decisao = decidir_acao(nivel_risco)
    fatores = identificar_fatores_risco(request)

    logger.info(
        "Predicao para id_usuario=%d: score=%.4f nivel=%s decisao=%s",
        request.id_usuario, risk_score, nivel_risco, decisao,
    )

    return PredictionResponse(
        id_usuario=request.id_usuario,
        score_risco=round(risk_score, 4),
        nivel_risco=nivel_risco,
        decisao=decisao,
        confianca=round(confidence, 4),
        fatores=fatores,
    )
