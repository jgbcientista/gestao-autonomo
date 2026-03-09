import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './relatorios.component.html',
  styleUrls: ['./relatorios.component.scss']
})
export class RelatoriosComponent implements OnInit {

  activeTab = 'trust-score';
  usuarioAtual: any = null;
  loading: { [key: string]: boolean } = {};
  results: { [key: string]: any } = {};
  errors: { [key: string]: string | null } = {};

  // Trust Score
  tsEmail = '';
  tsNivel = 'ALTO';
  tsAjusteEmail = '';
  tsAjusteValor = 0;
  tsAjusteMotivo = '';
  tsDecisaoEmail = '';

  // IA Python
  aiHora = 14;
  aiDiaSemana = 2;
  aiTentativasRecentes = 0;
  aiUsaVpn = false;
  aiUsaTor = false;
  aiPaisRisco = false;
  aiNovoDispositivo = false;
  aiTempoSessao = 300;
  aiScoreReputacao = 80;
  aiDistanciaKm = 10;

  // IA Comportamental
  iaUserId = 1;
  iaClassUserId = 1;
  iaHistUserId = 1;
  iaHistLimite = 10;
  iaScoreRequest: any = { hora: 14, tentativas: 0, novoDispositivo: false };

  // Blockchain
  bcUserId = 1;
  bcHash = '';
  bcVerifyHash = '';
  bcLimiteRisco = 0.7;
  bcReportUserId = 1;

  // Geolocalização
  geoIp = '';
  geoCodigoPais = '';
  geoLat1 = 0; geoLon1 = 0;
  geoLat2 = 0; geoLon2 = 0;

  // Métricas
  metricasUserId = 1;
  metricasPadraoUserId = 1;
  metricasBcUserId = 1;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuarioAtual = this.authService.getCurrentUser();
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

  private call(key: string, observable: any): void {
    this.loading[key] = true;
    this.errors[key] = null;
    this.results[key] = null;
    observable.subscribe({
      next: (res: any) => {
        this.results[key] = res;
        this.loading[key] = false;
      },
      error: (err: any) => {
        this.errors[key] = err.error?.message || err.message || JSON.stringify(err.error) || 'Erro desconhecido';
        this.loading[key] = false;
      }
    });
  }

  // === TRUST SCORE ===
  getTrustScore(): void {
    this.call('ts-score', this.apiService.getTrustScore(this.tsEmail));
  }

  getTrustScoreStats(): void {
    this.call('ts-stats', this.apiService.getTrustScoreStatistics());
  }

  simulateDecision(): void {
    this.call('ts-decisao', this.apiService.simulateAuthDecision(this.tsDecisaoEmail));
  }

  getUsersByLevel(): void {
    this.call('ts-nivel', this.apiService.getUsersByTrustLevel(this.tsNivel));
  }

  adjustTrustScore(): void {
    this.call('ts-ajuste', this.apiService.adjustTrustScore(this.tsAjusteEmail, this.tsAjusteValor, this.tsAjusteMotivo));
  }

  // === IA PYTHON ===
  getAiHealth(): void {
    this.call('ai-health', this.apiService.getAiHealth());
  }

  predictRisk(): void {
    const request = {
      id_usuario: 1,
      endereco_ip: '127.0.0.1',
      agente_navegador: navigator.userAgent,
      hora_do_dia: this.aiHora,
      dia_da_semana: this.aiDiaSemana,
      tentativas_login_ultima_hora: this.aiTentativasRecentes,
      novo_dispositivo: this.aiNovoDispositivo,
      nova_localizacao: this.aiPaisRisco,
      usa_vpn: this.aiUsaVpn,
      usa_tor: this.aiUsaTor,
      duracao_media_sessao: this.aiTempoSessao,
      paginas_por_sessao_media: 5.0,
      horas_desde_ultimo_login: 24.0
    };
    this.call('ai-predict', this.apiService.predictRisk(request));
  }

  // === IA COMPORTAMENTAL ===
  analyzeUser(): void {
    const context = {
      enderecoIp: '127.0.0.1',
      userAgent: navigator.userAgent,
      localizacaoGeografica: null,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      idiomaBrowser: navigator.language,
      resolucaoTela: `${window.screen.width}x${window.screen.height}`,
      tentativasLogin: 0
    };
    this.call('ia-analisar', this.apiService.analyzeUserBehavior(this.iaUserId, context));
  }

  classifyAccess(): void {
    const context = {
      enderecoIp: '127.0.0.1',
      userAgent: navigator.userAgent,
      localizacaoGeografica: null,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      idiomaBrowser: navigator.language,
      resolucaoTela: `${window.screen.width}x${window.screen.height}`,
      tentativasLogin: 0
    };
    this.call('ia-classificar', this.apiService.classifyAccess(this.iaClassUserId, context));
  }

  getAnalysisHistory(): void {
    this.call('ia-historico', this.apiService.getUserAnalysisHistory(this.iaHistUserId, this.iaHistLimite));
  }

  getAnomalyStats(): void {
    this.call('ia-estatisticas', this.apiService.getAnomalyStatistics());
  }

  calculateAnomalyScore(): void {
    this.call('ia-score', this.apiService.calculateAnomalyScore(this.iaScoreRequest));
  }

  // === BLOCKCHAIN ===
  getBlockchainUserTx(): void {
    this.call('bc-user', this.apiService.getBlockchainUserTransactions(this.bcUserId));
  }

  getBlockchainByHash(): void {
    this.call('bc-hash', this.apiService.getBlockchainTransactionByHash(this.bcHash));
  }

  verifyBlockchainIntegrity(): void {
    this.call('bc-verify', this.apiService.verifyBlockchainIntegrity(this.bcVerifyHash));
  }

  getBlockchainHighRisk(): void {
    this.call('bc-risk', this.apiService.getBlockchainHighRiskTransactions(this.bcLimiteRisco));
  }

  getBlockchainStats(): void {
    this.call('bc-stats', this.apiService.getBlockchainStatistics());
  }

  getBlockchainReport(): void {
    this.call('bc-report', this.apiService.getBlockchainUserReport(this.bcReportUserId));
  }

  // === GEOLOCALIZAÇÃO ===
  geolocateIp(): void {
    this.call('geo-ip', this.apiService.geolocateIp(this.geoIp));
  }

  checkCountryRisk(): void {
    this.call('geo-pais', this.apiService.checkCountryRisk(this.geoCodigoPais));
  }

  calculateDistance(): void {
    this.call('geo-dist', this.apiService.calculateGeoDistance(this.geoLat1, this.geoLon1, this.geoLat2, this.geoLon2));
  }

  // === MÉTRICAS ===
  getSecurityMetrics(): void {
    this.call('met-global', this.apiService.getSecurityMetrics());
  }

  getRiskAssessment(): void {
    this.call('met-risco', this.apiService.getUserRiskAssessment(this.metricasUserId));
  }

  getBehaviorPattern(): void {
    this.call('met-padrao', this.apiService.getUserBehaviorPattern(this.metricasPadraoUserId));
  }

  getMetricasBlockchain(): void {
    this.call('met-bc', this.apiService.getUserBlockchainTransactions(this.metricasBcUserId));
  }

  // === USUÁRIOS ===
  loadUsuarios(): void {
    this.call('usuarios', this.apiService.getUsersReport());
  }

  // === UTILS ===
  formatJson(data: any): string {
    return JSON.stringify(data, null, 2);
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
  }
}
