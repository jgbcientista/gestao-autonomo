import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;
  private aiUrl = (environment as any).aiApiUrl || 'http://localhost:5000';
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    if (this.isBrowser) {
      const token = localStorage.getItem('auth_token');
      if (token) {
        console.log('🔑 Token encontrado:', token.substring(0, 10) + '...');
        headers = headers.set('Authorization', `Bearer ${token}`);
      } else {
        console.warn('⚠️ Token não encontrado no localStorage');
      }
    }
    
    return headers;
  }

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.baseUrl}/api/v1/autenticacao/entrar`, 
      credentials, 
      { headers: this.getHeaders() }
    );
  }

  register(userDetails: RegisterRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.baseUrl}/api/v1/autenticacao/registrar`, 
      userDetails, 
      { headers: this.getHeaders() }
    );
  }

  validateToken(token: string): Observable<boolean> {
    return this.http.post<boolean>(
      `${this.baseUrl}/api/v1/autenticacao/validar-token`, 
      null,
      { 
        headers: this.getHeaders(),
        params: { token }
      }
    );
  }

  refreshToken(expiredToken: string): Observable<string> {
    return this.http.post<string>(
      `${this.baseUrl}/api/v1/autenticacao/renovar-token`, 
      null,
      { 
        headers: this.getHeaders(),
        params: { tokenExpirado: expiredToken }
      }
    );
  }

  logout(token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
    return this.http.post(
      `${this.baseUrl}/api/v1/autenticacao/logout`,
      null,
      { headers }
    );
  }

  getStatus(): Observable<string> {
    return this.http.get(
      `${this.baseUrl}/api/v1/autenticacao/status`, 
      { 
        headers: this.getHeaders(),
        responseType: 'text'
      }
    );
  }

  // === SCORE DE CONFIANÇA ===
  getUsuariosCadastrados(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/api/trust-score/usuarios`,
      { headers: this.getHeaders() }
    );
  }

  getTrustScore(email: string): Observable<any> {
    console.log('📊 Buscando Trust Score para:', email);
    const headers = this.getHeaders();
    console.log('🔒 Headers:', headers.keys());
    
    return this.http.get<any>(
      `${this.baseUrl}/api/trust-score/usuario/${email}`,
      { headers }
    );
  }

  adjustTrustScore(email: string, ajuste: number, motivo: string): Observable<any> {
    console.log('⚖️ Ajustando Trust Score:', { email, ajuste, motivo });
    const headers = this.getHeaders();

    return this.http.post<any>(
      `${this.baseUrl}/api/trust-score/ajustar/${email}`,
      null,
      {
        headers,
        params: { ajuste: ajuste.toString(), motivo }
      }
    );
  }

  getTrustScoreStatistics(): Observable<any> {
    console.log('📈 Buscando estatísticas do Trust Score');
    const headers = this.getHeaders();
    
    return this.http.get<any>(
      `${this.baseUrl}/api/trust-score/estatisticas`,
      { headers }
    );
  }

  getUsersByTrustLevel(nivel: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/trust-score/nivel/${nivel}`,
      { headers: this.getHeaders() }
    );
  }

  simulateAuthDecision(email: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/trust-score/decisao/${email}`,
      { headers: this.getHeaders() }
    );
  }

  // === ANÁLISE COMPORTAMENTAL IA ===
  analyzeUserBehavior(userId: number, context: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/ia/analisar/${userId}`,
      context,
      { headers: this.getHeaders() }
    );
  }

  classifyAccess(userId: number, context: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/ia/classificar/${userId}`,
      context,
      { headers: this.getHeaders() }
    );
  }

  trainAIModels(): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/ia/treinar-modelos`,
      {},
      { headers: this.getHeaders() }
    );
  }

  sendAIFeedback(perfilId: number, feedback: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/ia/feedback/${perfilId}`,
      feedback,
      { headers: this.getHeaders() }
    );
  }

  getUserAnalysisHistory(userId: number, limite: number = 10): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/ia/historico/${userId}`,
      { 
        headers: this.getHeaders(),
        params: { limite: limite.toString() }
      }
    );
  }

  getAnomalyStatistics(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/ia/estatisticas`,
      { headers: this.getHeaders() }
    );
  }

  calculateAnomalyScore(request: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/ia/calcular-score`,
      request,
      { headers: this.getHeaders() }
    );
  }

  // === IA PYTHON (FastAPI) ===
  getAiHealth(): Observable<any> {
    return this.http.get<any>(`${this.aiUrl}/health`);
  }

  predictRisk(request: any): Observable<any> {
    return this.http.post<any>(`${this.aiUrl}/predict`, request);
  }

  // === BLOCKCHAIN AUDITORIA ===
  getBlockchainAllTransactions(limite: number = 100): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/transacoes`,
      { headers: this.getHeaders(), params: { limite: limite.toString() } }
    );
  }

  getBlockchainUserTransactions(userId: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/usuario/${userId}`,
      { headers: this.getHeaders() }
    );
  }

  getBlockchainTransactionByHash(hash: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/transacao/${hash}`,
      { headers: this.getHeaders() }
    );
  }

  verifyBlockchainIntegrity(hash: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/integridade/${hash}`,
      { headers: this.getHeaders() }
    );
  }

  getBlockchainHighRiskTransactions(limiteRisco: number = 0.7): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/alto-risco`,
      {
        headers: this.getHeaders(),
        params: { limiteRisco: limiteRisco.toString() }
      }
    );
  }

  getBlockchainUserHighRiskTransactions(userId: number, limiteRisco: number = 0.7): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/usuario/${userId}/suspeitas`,
      {
        headers: this.getHeaders(),
        params: { limiteRisco: limiteRisco.toString() }
      }
    );
  }

  getBlockchainStatistics(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/estatisticas`,
      { headers: this.getHeaders() }
    );
  }

  getBlockchainUserReport(userId: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/blockchain/auditoria/relatorio/usuario/${userId}`,
      { headers: this.getHeaders() }
    );
  }

  // === GEOLOCALIZAÇÃO (EXTRAS) ===
  geolocateIp(ip: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/geolocalizacao/ip/${ip}`,
      { headers: this.getHeaders() }
    );
  }

  checkCountryRisk(codigoPais: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/geolocalizacao/pais/${codigoPais}/risco`,
      { headers: this.getHeaders() }
    );
  }

  calculateGeoDistance(lat1: number, lon1: number, lat2: number, lon2: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/geolocalizacao/distancia`,
      {
        headers: this.getHeaders(),
        params: {
          lat1: lat1.toString(), lon1: lon1.toString(),
          lat2: lat2.toString(), lon2: lon2.toString()
        }
      }
    );
  }

  // === ANALYTICS E RELATÓRIOS ===
  analyzeContext(request: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/relatorios/analise-contexto`,
      request,
      { headers: this.getHeaders() }
    );
  }

  getUserBehaviorPattern(userId: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/usuario/${userId}/padrao-comportamento`,
      { headers: this.getHeaders() }
    );
  }

  getUserBlockchainTransactions(userId: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/usuario/${userId}/transacoes-blockchain`,
      { headers: this.getHeaders() }
    );
  }

  getSecurityMetrics(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/metricas-seguranca`,
      { headers: this.getHeaders() }
    );
  }

  getUserRiskAssessment(userId: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/usuario/${userId}/avaliacao-risco`,
      { headers: this.getHeaders() }
    );
  }

  getUsersReport(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/usuarios`,
      { headers: this.getHeaders() }
    );
  }

  // === BLOCKCHAIN ===
  getTransactionByHash(hash: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/blockchain/transacao/${hash}`,
      { headers: this.getHeaders() }
    );
  }

  getHighRiskTransactions(limiteRisco: number = 0.7): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/blockchain/transacoes-alto-risco`,
      { 
        headers: this.getHeaders(),
        params: { limiteRisco: limiteRisco.toString() }
      }
    );
  }

  getUnverifiedTransactions(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/blockchain/transacoes-nao-verificadas`,
      { headers: this.getHeaders() }
    );
  }

  verifyTransaction(hash: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/relatorios/blockchain/transacao/${hash}/verificar`,
      { headers: this.getHeaders() }
    );
  }

  // === GEOLOCALIZAÇÃO ===
  analyzeLocation(request: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/geolocalizacao/analisar`,
      request,
      { headers: this.getHeaders() }
    );
  }

  getUserLocationHistory(userId: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/geolocalizacao/usuario/${userId}/historico`,
      { headers: this.getHeaders() }
    );
  }

  // === HEALTH CHECK E MONITORAMENTO ===
  getHealthStatus(): Observable<any> {
    console.log('🏥 Buscando status de saúde do sistema');
    const headers = this.getHeaders();
    console.log('🔒 Headers:', headers.keys());
    
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/health/status`,
      { headers }
    );
  }

  getSystemHealth(): Observable<any> {
    console.log('🔍 Buscando métricas do sistema');
    const headers = this.getHeaders();
    console.log('🔒 Headers:', headers.keys());
    
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/health/system`,
      { headers }
    );
  }

  // === CONFIGURAÇÕES DO USUÁRIO ===
  getUserSettings(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/configuracoes/usuario`,
      { headers: this.getHeaders() }
    );
  }

  updateUserSettings(settings: any): Observable<any> {
    return this.http.put<any>(
      `${this.baseUrl}/api/v1/configuracoes/usuario`,
      settings,
      { headers: this.getHeaders() }
    );
  }

  // === POLÍTICA DE SEGURANÇA (ADMIN) ===
  getSecurityPolicy(): Observable<any> {
    console.log('🔒 Buscando política de segurança');
    const headers = this.getHeaders();
    
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/configuracoes/seguranca`,
      { headers }
    );
  }

  updateSecurityPolicy(policy: any): Observable<any> {
    console.log('🔒 Atualizando política de segurança:', policy);
    const headers = this.getHeaders();
    
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/configuracoes/seguranca`,
      policy,
      { headers }
    );
  }

  // === CONFIGURAÇÃO DO SISTEMA (ADMIN) ===
  getSystemConfiguration(): Observable<any> {
    console.log('⚙️ Buscando configurações do sistema');
    const headers = this.getHeaders();
    
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/configuracoes/sistema`,
      { headers }
    );
  }

  updateSystemConfiguration(config: any): Observable<any> {
    console.log('⚙️ Atualizando configurações:', config);
    const headers = this.getHeaders();
    
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/configuracoes/sistema`,
      config,
      { headers }
    );
  }

  blockSession(sessionId: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/sessoes/${sessionId}/bloquear`,
      {},
      { headers: this.getHeaders() }
    );
  }

  endSession(sessionId: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/sessoes/${sessionId}/encerrar`,
      {},
      { headers: this.getHeaders() }
    );
  }

  getSessionStats(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/sessoes/estatisticas`,
      { headers: this.getHeaders() }
    );
  }

  // === MFA (Autenticação Multi-Fator) ===
  configurarMfa(email: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/mfa/configurar`,
      { email },
      { headers: this.getHeaders() }
    );
  }

  verificarConfiguracaoMfa(email: string, codigo: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/mfa/verificar-configuracao`,
      { email, codigo },
      { headers: this.getHeaders() }
    );
  }

  validarMfa(email: string, codigo: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/mfa/validar`,
      { email, codigo },
      { headers: this.getHeaders() }
    );
  }

  getMfaStatus(email: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/mfa/status/${email}`,
      { headers: this.getHeaders() }
    );
  }

  // === FUNCIONALIDADES ESPECÍFICAS ===
  regenerateApiKey(): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/configuracoes/regenerar-chave-api`,
      {},
      { headers: this.getHeaders() }
    );
  }

  testNotifications(): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/v1/configuracoes/testar-notificacoes`,
      {},
      { headers: this.getHeaders() }
    );
  }
}
