import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8081';
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
        headers = headers.set('Authorization', `Bearer ${token}`);
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

  getStatus(): Observable<string> {
    return this.http.get<string>(
      `${this.baseUrl}/api/v1/autenticacao/status`, 
      { headers: this.getHeaders() }
    );
  }

  // === SCORE DE CONFIANÇA ===
  getTrustScore(email: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/trust-score/usuario/${email}`,
      { headers: this.getHeaders() }
    );
  }

  adjustTrustScore(email: string, ajuste: number, motivo: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/api/trust-score/ajustar/${email}`,
      null,
      { 
        headers: this.getHeaders(),
        params: { ajuste: ajuste.toString(), motivo }
      }
    );
  }

  getTrustScoreStatistics(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/trust-score/estatisticas`,
      { headers: this.getHeaders() }
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

  // === HEALTH CHECK ===
  getHealthStatus(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/health/status`,
      { headers: this.getHeaders() }
    );
  }

  getSystemHealth(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/health/system`,
      { headers: this.getHeaders() }
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
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/configuracoes/politica-seguranca`,
      { headers: this.getHeaders() }
    );
  }

  updateSecurityPolicy(policy: any): Observable<any> {
    return this.http.put<any>(
      `${this.baseUrl}/api/v1/configuracoes/politica-seguranca`,
      policy,
      { headers: this.getHeaders() }
    );
  }

  // === CONFIGURAÇÃO DO SISTEMA (ADMIN) ===
  getSystemConfiguration(): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/api/v1/configuracoes/sistema`,
      { headers: this.getHeaders() }
    );
  }

  updateSystemConfiguration(config: any): Observable<any> {
    return this.http.put<any>(
      `${this.baseUrl}/api/v1/configuracoes/sistema`,
      config,
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
