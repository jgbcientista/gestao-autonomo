// === MODELOS PARA SCORE DE CONFIANÇA ===
export interface TrustScore {
  usuario: string;
  scoreAtual: number;
  nivelConfianca: 'MUITO_BAIXO' | 'BAIXO' | 'MEDIO' | 'ALTO' | 'MUITO_ALTO';
  scoreBase: number;
  fatorAjuste: number;
  emObservacao: boolean;
  ultimaAtualizacao: Date;
  motivoAlteracao?: string;
  estatisticas: TrustScoreStatistics;
  decisoes: TrustScoreDecisions;
}

export interface TrustScoreStatistics {
  totalLoginsSucesso: number;
  totalLoginsSuspeitos: number;
  totalBloqueios: number;  
  totalMfaExigido: number;
}

export interface TrustScoreDecisions {
  confiavel: boolean;
  requerMfa: boolean;
  deveBloquear: boolean;
}

// === MODELOS PARA ANÁLISE COMPORTAMENTAL IA ===
export interface BehaviorAnalysis {
  id: number;
  usuarioId: number;
  classificacao: 'ESPERADO' | 'ANOMALO' | 'SUSPEITO' | 'CRITICO';
  scoreAnomaliaGlobal: number;
  confiabilidade: number;
  timestamp: Date;
  contexto: AnalysisContext;
}

export interface AnalysisContext {
  enderecoIp: string;
  userAgent: string;
  localizacaoGeografica: string;
  timezone: string;
  idiomaBrowser: string;
  resolucaoTela: string;
  tentativasLogin: number;
}

// === NOVOS MODELOS PARA INTEGRAÇÃO COM API DE IA ===
export interface DadosContextoRequest {
  enderecoIp: string;
  userAgent: string;
  localizacaoGeografica: string;
  timezone: string;
  idiomaBrowser: string;
  resolucaoTela: string;
  tentativasLogin: number;
}

export interface ClassificacaoResponse {
  classificacao: 'ESPERADO' | 'SUSPEITO' | 'ANOMALO' | 'ALTAMENTE_SUSPEITO';
  descricao: string;
  nivelRisco: 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO';
}

export interface FeedbackRequest {
  acessoLegitimo: boolean;
  comentario?: string;
}

export interface ScoreResponse {
  isolationForest: number;
  randomForest: number;
  deepLearning: number;
  ensemble: number;
  detalhes: { [key: string]: number };
}

export interface CalcularScoreRequest {
  horaAcesso: number;
  diaSemana: number;
  frequenciaAcessoSemanal: number;
  ipJaUtilizado: boolean;
  dispositivoJaUtilizado: boolean;
  localizacaoJaUtilizada: boolean;
  distanciaLocalizacaoHabitualKm: number;
  diferencaHorarioHabitualHoras: number;
  tempoDesdeUltimoAcessoHoras: number;
  mediaSessoesDiarias: number;
  desvioPadraoHorarios: number;
  totalIpsDistintos: number;
  totalDispositivosDistintos: number;
  padroesNavegacaoScore: number;
}

export interface PerfilComportamentalIA {
  id: number;
  usuarioId: number;
  classificacao: 'ESPERADO' | 'SUSPEITO' | 'ANOMALO' | 'ALTAMENTE_SUSPEITO';
  scoreAnomaliaGlobal: number;
  confiabilidade: number;
  scoresComportamentais: { [key: string]: number };
  dadosContexto: DadosContextoRequest;
  timestamp: Date;
  modelosUtilizados: string[];
}

export interface EstatisticasAnomalias {
  totalAnalises: number;
  totalAnomalias: number;
  percentualAnomalias: number;
  taxaAcuracia: number;
  tempoMedioAnalise: number;
  distribuicaoClassificacoes: { [key: string]: number };
  precisaoModelo: number;
  ultimoTreinamento: Date;
  modelosAtivos: string[];
}

export interface AIStatistics {
  totalAnalises: number;
  totalAnomalias: number;
  percentualAnomalias: number;
  ultimoTreinamento: Date;
  precisaoModelo: number;
}

export interface AnomalyScore {
  isolationForest: number;
  randomForest: number;
  deepLearning: number;
  ensemble: number;
  detalhes: { [key: string]: number };
}

// === MODELOS PARA ANALYTICS ===
export interface SecurityMetrics {
  totalUsuarios: number;
  pontuacaoRiscoMedia: number;
  usuariosAltoRisco: number;
  transacoesAltoRisco: number;
  transacoesNaoVerificadas: number;
  integridadeBlockchain: 'BOA' | 'PRECISA_ATENCAO' | 'CRITICA';
}

export interface UserRiskAssessment {
  idUsuario: number;
  emailUsuario: string;
  pontuacaoRiscoAtual: number;
  nivelRisco: 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  totalLogins: number;
  atividadesSuspeitas: number;
  transacoesNegadas: number;
  totalTransacoes: number;
  ultimoLoginData?: Date;
  contaBloqueada: boolean;
}

export interface BehaviorPattern {
  id: number;
  usuario: any;
  horariosAcessoHabituais: string[];
  localizacoesFrequentes: string[];
  dispositivosReconhecidos: string[];
  padraoNavegacao: string;
  pontuacaoRiscoGlobal: number;
  statusPerfilRisco: 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  numeroTotalLogins: number;
  numeroLoginsSuspeitos: number;
}

// === MODELOS PARA BLOCKCHAIN ===
export interface BlockchainTransaction {
  id: number;
  usuarioId: number;
  tipoOperacao: string;
  hashTransacao: string;
  statusConfirmacao: 'PENDENTE' | 'CONFIRMADA' | 'FALHADA';
  numeroBloco?: number;
  confirmacoes: number;
  scoreRisco: number;
  decisao: 'APROVADA' | 'NEGADA' | 'PENDENTE';
  timestamp: Date;
  detalhesOperacao: string;
}

export interface TransactionVerification {
  hashTransacao: string;
  verificado: boolean;
  statusConfirmacao: string;
  confirmacoes: number;
  numeroBloco: number;
}

// === MODELOS PARA GEOLOCALIZAÇÃO ===
export interface LocationAnalysis {
  localizacaoAtual: string;
  coordenadas: {
    latitude: number;
    longitude: number;
  };
  distanciaUltimaLocalizacao: number;
  localizacaoSuspeita: boolean;
  risco: 'BAIXO' | 'MEDIO' | 'ALTO';
  motivoSuspeita?: string;
}

export interface LocationHistory {
  id: number;
  usuarioId: number;
  localizacao: string;
  coordenadas: {
    latitude: number;
    longitude: number;
  };
  timestamp: Date;
  scoreConfianca: number;
}

// === MODELOS PARA SISTEMA ===
export interface SystemHealth {
  status: 'ONLINE' | 'OFFLINE' | 'DEGRADADO';
  servicos: SystemService[];
  timestamp: Date;
  uptime: number;
  versao: string;
}

export interface SystemService {
  nome: string;
  status: 'ATIVO' | 'INATIVO' | 'ERRO';
  ultimaVerificacao: Date;
  detalhes?: string;
}

// === MODELOS PARA DASHBOARD ===
export interface DashboardData {
  sistemasOnline: boolean;
  sessaoAtiva: boolean;
  iaAtiva: boolean;
  scoreConfianca: TrustScore;
  metricas: SecurityMetrics;
  alertas: Alert[];
  atividades: RecentActivity[];
}

export interface Alert {
  id: number;
  tipo: 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS';
  titulo: string;
  mensagem: string;
  timestamp: Date;
  lida: boolean;
}

export interface RecentActivity {
  id: number;
  tipo: 'LOGIN' | 'LOGOUT' | 'ANOMALIA' | 'BLOQUEIO' | 'MFA';
  descricao: string;
  timestamp: Date;
  risco: 'BAIXO' | 'MEDIO' | 'ALTO';
} 