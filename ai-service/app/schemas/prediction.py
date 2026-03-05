from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class PredictionRequest(BaseModel):
    id_usuario: int
    endereco_ip: str = "0.0.0.0"
    agente_navegador: str = "unknown"
    hora_do_dia: int = Field(ge=0, le=23, default=12)
    dia_da_semana: int = Field(ge=0, le=6, default=0)
    tentativas_login_ultima_hora: int = Field(ge=0, default=0)
    novo_dispositivo: bool = False
    nova_localizacao: bool = False
    usa_vpn: bool = False
    usa_tor: bool = False
    duracao_media_sessao: float = Field(ge=0, default=300.0)
    paginas_por_sessao_media: float = Field(ge=0, default=5.0)
    horas_desde_ultimo_login: float = Field(ge=0, default=24.0)


class PredictionResponse(BaseModel):
    id_usuario: int
    score_risco: float = Field(ge=0.0, le=1.0)
    nivel_risco: str
    decisao: str
    confianca: float = Field(ge=0.0, le=1.0)
    fatores: list[str] = []
    timestamp: str = Field(default_factory=lambda: datetime.now().isoformat())


class TrainResponse(BaseModel):
    status: str
    mensagem: str
    acuracia: Optional[float] = None
    amostras_utilizadas: Optional[int] = None


class HealthResponse(BaseModel):
    status: str
    modelo_carregado: bool
    versao: str = "1.0.0"
