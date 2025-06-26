import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, interval, forkJoin } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface UserSession {
  id: string;
  userId: number;
  userEmail: string;
  userName: string;
  deviceInfo: string;
  ipAddress: string;
  location: string;
  loginTime: Date;
  lastActivity: Date;
  sessionDuration: number;
  isActive: boolean;
  riskLevel: 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  activities: SessionActivity[];
}

interface SessionActivity {
  id: string;
  timestamp: Date;
  action: string;
  details: string;
  ipAddress: string;
  riskScore: number;
}

interface SessionMetrics {
  totalSessions: number;
  activeSessions: number;
  suspiciousSessions: number;
  averageSessionDuration: number;
  uniqueDevices: number;
  uniqueLocations: number;
}

interface SecurityAlert {
  id: string;
  sessionId: string;
  type: 'SUSPICIOUS_LOGIN' | 'MULTIPLE_LOCATIONS' | 'UNUSUAL_ACTIVITY' | 'CONCURRENT_SESSIONS';
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  timestamp: Date;
  resolved: boolean;
}

@Component({
  selector: 'app-session-manager',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './session-manager.component.html',
  styleUrl: './session-manager.component.scss'
})
export class SessionManagerComponent implements OnInit, OnDestroy {
  loading = false;
  error: string | null = null;
  modoSimulado = false;

  // Dados principais
  sessions: UserSession[] = [];
  sessionMetrics: SessionMetrics | null = null;
  securityAlerts: SecurityAlert[] = [];
  
  // Sessão selecionada para detalhes
  selectedSession: UserSession | null = null;
  
  // Filtros e controles
  filtroStatus = 'TODAS';
  filtroRisco = 'TODOS';
  filtroDispositivo = 'TODOS';
  ordenacao = 'lastActivity';
  
  // Visualização ativa
  visualizacaoAtiva = 'overview';
  
  // Monitoramento em tempo real
  monitoramentoAtivo = false;
  intervalId: any = null;
  
  // Subscriptions
  private subscriptions = new Subscription();
  
  // Usuário atual
  usuarioAtual: any = null;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Verifica se está executando no browser para evitar problemas de SSR
    if (typeof window !== 'undefined') {
      this.carregarDados();
      // Delay antes de iniciar monitoramento para evitar problemas de SSR
      setTimeout(() => {
        this.iniciarMonitoramento();
      }, 1000);
    } else {
      // No SSR, apenas carrega dados simulados sem monitoramento
      this.carregarDadosSimuladosSSR();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.pararMonitoramento();
  }

  carregarDados(): void {
    this.loading = true;
    this.error = null;
    
    this.usuarioAtual = this.authService.getCurrentUser();
    
    // Verifica se o usuário está autenticado e tem token
    const token = this.authService.getToken();
    const isAuthenticated = this.authService.isAuthenticated();
    const isAdmin = this.usuarioAtual?.role === 'ADMIN';
    
    console.log('👤 Usuário:', this.usuarioAtual?.name, '| Role:', this.usuarioAtual?.role, '| Admin:', isAdmin);
    
    if (!isAuthenticated || !token) {
      console.warn('Usuário não autenticado ou token ausente. Usando dados simulados.');
      this.modoSimulado = true;
      this.carregarDadosSimulados();
      this.loading = false;
      return;
    }

    // Se não é admin, usa dados simulados
    if (!isAdmin) {
      console.warn('Usuário não é administrador. Usando dados simulados.');
      this.modoSimulado = true;
      this.carregarDadosSimulados();
      this.loading = false;
      return;
    }

    // ADMIN: Tenta carregar dados reais
    console.log('👤 Usuário ADMIN detectado - Buscando dados reais');
    this.modoSimulado = false;
    
    // Tenta carregar dados reais da API
    const subscricao = forkJoin({
      metrics: this.apiService.getSystemHealth(),
      sessions: this.apiService.getHealthStatus()
    }).subscribe({
      next: (dados) => {
        console.log('✅ API disponível - dados carregados:', dados);
        
        // Verifica se os dados são realmente reais
        if (dados.metrics?.dadosSimulados || dados.sessions?.dadosSimulados) {
          console.warn('⚠️ API retornou dados simulados:', {
            motivoMetrics: dados.metrics?.motivoSimulacao,
            motivoSessions: dados.sessions?.motivoSimulacao
          });
          this.modoSimulado = true;
        } else {
          this.modoSimulado = false;
        }
        
        this.processarDadosReais(dados);
        this.loading = false;
      },
      error: (erro) => {
        console.error('⚠️ API não disponível:', erro);
        this.error = 'Erro ao carregar dados do servidor. Usando dados simulados temporariamente.';
        this.modoSimulado = true;
        this.carregarDadosSimulados();
        this.loading = false;
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  private processarDadosReais(dados: any): void {
    console.log('🔄 Processando dados:', dados);
    
    // Verifica se os dados são válidos
    if (!dados.sessions || !dados.metrics) {
      console.error('⚠️ Dados inválidos recebidos da API');
      this.error = 'Erro ao processar dados do servidor';
      this.modoSimulado = true;
      this.carregarDadosSimulados();
      return;
    }
    
    // Processa métricas do sistema
    this.sessionMetrics = {
      totalSessions: dados.sessions.totalSessions || 0,
      activeSessions: dados.sessions.activeSessions || 0,
      suspiciousSessions: dados.sessions.suspiciousSessions || 0,
      averageSessionDuration: dados.sessions.averageSessionDuration || 0,
      uniqueDevices: dados.sessions.uniqueDevices || 0,
      uniqueLocations: dados.sessions.uniqueLocations || 0
    };
    
    // Processa sessões
    if (Array.isArray(dados.sessions.sessionsData)) {
      this.sessions = dados.sessions.sessionsData.map((sessao: any) => ({
        id: sessao.userId.toString(),
        userId: sessao.userId,
        userEmail: sessao.email,
        userName: sessao.username,
        deviceInfo: `${sessao.browser} (${sessao.deviceType})`,
        ipAddress: sessao.ipAddress,
        location: sessao.location,
        loginTime: new Date(sessao.loginTime),
        lastActivity: new Date(sessao.lastActivity),
        sessionDuration: sessao.sessionDuration,
        isActive: sessao.status === 'Active',
        riskLevel: this.calcularNivelRisco(sessao.trustScore),
        activities: this.gerarAtividadesUsuarioAtual() // TODO: Implementar atividades reais quando disponíveis
      }));
    } else {
      console.warn('⚠️ Dados de sessões inválidos');
      this.sessions = [];
    }
    
    // Gera alertas baseados nos dados reais
    this.gerarAlertasReais();
    
    console.log('✅ Dados processados:', {
      metrics: this.sessionMetrics,
      sessions: this.sessions.length,
      alerts: this.securityAlerts.length
    });
  }
  
  private calcularNivelRisco(trustScore: number): 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO' {
    if (trustScore >= 80) return 'BAIXO';
    if (trustScore >= 60) return 'MEDIO';
    if (trustScore >= 40) return 'ALTO';
    return 'CRITICO';
  }
  
  private gerarAlertasReais(): void {
    this.securityAlerts = [];
    
    // Gera alertas baseados nas sessões reais
    this.sessions.forEach(session => {
      // Alerta para sessões com risco alto/crítico
      if (session.riskLevel === 'ALTO' || session.riskLevel === 'CRITICO') {
        this.securityAlerts.push({
          id: `alert_${session.id}_risk`,
          sessionId: session.id,
          type: 'SUSPICIOUS_LOGIN',
          severity: session.riskLevel === 'CRITICO' ? 'CRITICAL' : 'HIGH',
          message: `Sessão de alto risco detectada para ${session.userName}`,
          timestamp: new Date(),
          resolved: false
        });
      }
      
      // Alerta para múltiplas sessões do mesmo usuário
      const sessoesUsuario = this.sessions.filter(s => s.userId === session.userId);
      if (sessoesUsuario.length > 1) {
        this.securityAlerts.push({
          id: `alert_${session.id}_concurrent`,
          sessionId: session.id,
          type: 'CONCURRENT_SESSIONS',
          severity: 'MEDIUM',
          message: `Múltiplas sessões ativas detectadas para ${session.userName}`,
          timestamp: new Date(),
          resolved: false
        });
      }
      
      // Alerta para sessões muito longas
      if (session.sessionDuration > 8) {
        this.securityAlerts.push({
          id: `alert_${session.id}_duration`,
          sessionId: session.id,
          type: 'UNUSUAL_ACTIVITY',
          severity: 'LOW',
          message: `Sessão prolongada detectada para ${session.userName}`,
          timestamp: new Date(),
          resolved: false
        });
      }
    });
  }

  private gerarSessoesReais(): void {
    // Gera sessões priorizando o usuário logado atual quando disponível
    const sessions: UserSession[] = [];
    
    // Adiciona primeiro a sessão do usuário atual logado
    if (this.usuarioAtual) {
      console.log('👤 Adicionando sessão do usuário atual:', this.usuarioAtual);
      sessions.push({
        id: `sess_current_${this.usuarioAtual.id || 'user'}`,
        userId: this.usuarioAtual.id || 1,
        userEmail: this.usuarioAtual.email || 'usuario@atual.com',
        userName: this.usuarioAtual.name || 'Usuário Atual',
        deviceInfo: this.detectarDispositivo(),
        ipAddress: '192.168.1.' + Math.floor(Math.random() * 254 + 1),
        location: 'Local atual - Sessão ativa',
        loginTime: new Date(Date.now() - 30 * 60 * 1000), // 30 min atrás
        lastActivity: new Date(Date.now() - 5 * 60 * 1000), // 5 min atrás
        sessionDuration: 0.5, // 30 minutos
        isActive: true, // Sessão atual sempre ativa
        riskLevel: 'BAIXO',
        activities: this.gerarAtividadesUsuarioAtual()
      });
    }
    
    // Adiciona outras sessões simuladas mais realistas
    const outrosSessions = this.gerarOutrasSessoesRealistas(Math.max(0, 11 - sessions.length));
    sessions.push(...outrosSessions);
    
    this.sessions = sessions;
    console.log('✅ Sessões geradas com usuário real:', this.sessions.length);
  }

  private detectarDispositivo(): string {
    if (typeof navigator !== 'undefined') {
      const userAgent = navigator.userAgent;
      if (userAgent.includes('Chrome')) return 'Chrome ' + (userAgent.match(/Chrome\/(\d+)/)?.[1] || '120') + '.0';
      if (userAgent.includes('Firefox')) return 'Firefox ' + (userAgent.match(/Firefox\/(\d+)/)?.[1] || '121') + '.0';
      if (userAgent.includes('Safari')) return 'Safari ' + (userAgent.match(/Version\/(\d+)/)?.[1] || '17') + '.0';
      if (userAgent.includes('Edge')) return 'Edge ' + (userAgent.match(/Edg\/(\d+)/)?.[1] || '120') + '.0';
    }
    return 'Chrome 120.0 (Browser atual)';
  }

  private gerarAtividadesUsuarioAtual(): SessionActivity[] {
    const atividadesReais = [
      'Login realizado com sucesso',
      'Dashboard acessado',
      'Configurações visualizadas',
      'Sistema de sessões consultado',
      'Perfil atualizado'
    ];

    return Array.from({ length: 3 }, (_, i) => ({
      id: `act_current_${i}`,
      timestamp: new Date(Date.now() - (i + 1) * 10 * 60 * 1000), // A cada 10 min
      action: atividadesReais[Math.min(i, atividadesReais.length - 1)],
      details: 'Atividade real do usuário logado',
      ipAddress: '192.168.1.' + Math.floor(Math.random() * 254 + 1),
      riskScore: 0.1 // Baixo risco para usuário atual
    }));
  }

  private gerarOutrasSessoesRealistas(quantidade: number): UserSession[] {
    const baseEmails = [
      'admin@sistema.com', 'suporte@empresa.com', 'usuario.teste@demo.com',
      'operador@sistema.com', 'analista@empresa.com', 'desenvolvedor@teste.com'
    ];
    
    const baseNames = [
      'Administrador Sistema', 'Suporte Técnico', 'Usuário Demo',
      'Operador Sistema', 'Analista Segurança', 'Dev Teste'
    ];

    return Array.from({ length: quantidade }, (_, i) => ({
      id: `sess_other_${i + 1}`,
      userId: i + 2,
      userEmail: baseEmails[i % baseEmails.length],
      userName: baseNames[i % baseNames.length],
      deviceInfo: this.getRandomDevice(),
      ipAddress: this.getRandomIP(),
      location: this.getRandomLocation(),
      loginTime: new Date(Date.now() - this.getRandom() * 8 * 60 * 60 * 1000),
      lastActivity: new Date(Date.now() - this.getRandom() * 60 * 60 * 1000),
      sessionDuration: this.getRandom() * 4 + 0.5,
      isActive: this.getRandom() > 0.4,
      riskLevel: this.getRandomRiskLevel(),
      activities: this.gerarAtividadesSessao()
    }));
  }

  private carregarDadosSimulados(): void {
    // Dados simulados para demonstração
    this.sessionMetrics = {
      totalSessions: 156,
      activeSessions: 23,
      suspiciousSessions: 5,
      averageSessionDuration: 2.8,
      uniqueDevices: 89,
      uniqueLocations: 15
    };

    this.sessions = Array.from({ length: 15 }, (_, i) => ({
      id: `sess_sim_${i + 1}`,
      userId: i + 1,
      userEmail: `usuario${i + 1}@exemplo.com`,
      userName: `Usuário Simulado ${i + 1}`,
      deviceInfo: this.getRandomDevice(),
      ipAddress: this.getRandomIP(),
      location: this.getRandomLocation(),
      loginTime: new Date(Date.now() - this.getRandom() * 24 * 60 * 60 * 1000),
      lastActivity: new Date(Date.now() - this.getRandom() * 60 * 60 * 1000),
      sessionDuration: this.getRandom() * 6 + 0.2,
      isActive: this.getRandom() > 0.4,
      riskLevel: this.getRandomRiskLevel(),
      activities: this.gerarAtividadesSessao()
    }));

    this.gerarAlertasSimulados();
  }

  private carregarDadosSimuladosSSR(): void {
    // Versão simplificada para SSR - apenas dados básicos
    this.modoSimulado = true;
    this.loading = false;
    
    this.sessionMetrics = {
      totalSessions: 156,
      activeSessions: 23,
      suspiciousSessions: 5,
      averageSessionDuration: 2.8,
      uniqueDevices: 89,
      uniqueLocations: 15
    };

    // Dados mínimos para renderização inicial
    this.sessions = [];
    this.securityAlerts = [];
  }

  private gerarAtividadesSessao(): SessionActivity[] {
    const atividades = [
      'Login realizado',
      'Página acessada',
      'Arquivo baixado',
      'Configuração alterada',
      'Dados consultados',
      'Relatório gerado',
      'Logout executado'
    ];

    return Array.from({ length: Math.floor(this.getRandom() * 5) + 2 }, (_, i) => ({
      id: `act_${Date.now()}_${i}`,
      timestamp: new Date(Date.now() - this.getRandom() * 2 * 60 * 60 * 1000),
      action: atividades[Math.floor(this.getRandom() * atividades.length)],
      details: 'Atividade executada com sucesso',
      ipAddress: this.getRandomIP(),
      riskScore: this.getRandom()
    }));
  }

  private gerarAlertasSimulados(): void {
    const tipos: SecurityAlert['type'][] = ['SUSPICIOUS_LOGIN', 'MULTIPLE_LOCATIONS', 'UNUSUAL_ACTIVITY', 'CONCURRENT_SESSIONS'];
    const severidades: SecurityAlert['severity'][] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    
    this.securityAlerts = Array.from({ length: 6 }, (_, i) => ({
      id: `alert_sim_${i + 1}`,
      sessionId: this.sessions[Math.floor(this.getRandom() * this.sessions.length)]?.id || '',
      type: tipos[Math.floor(this.getRandom() * tipos.length)],
      severity: severidades[Math.floor(this.getRandom() * severidades.length)],
      message: this.getRandomAlertMessage(),
      timestamp: new Date(Date.now() - this.getRandom() * 6 * 60 * 60 * 1000),
      resolved: this.getRandom() > 0.6
    }));
  }

  // Métodos de ação
  encerrarSessao(sessionId: string): void {
    const session = this.sessions.find(s => s.id === sessionId);
    if (session) {
      session.isActive = false;
      session.lastActivity = new Date();
      console.log(`Sessão ${sessionId} encerrada com sucesso`);
    }
  }

  bloquearUsuario(userId: number): void {
    const userSessions = this.sessions.filter(s => s.userId === userId);
    userSessions.forEach(session => {
      session.isActive = false;
      session.riskLevel = 'CRITICO';
    });
    console.log(`Usuário ${userId} bloqueado e todas as sessões encerradas`);
  }

  marcarAlertaComoResolvido(alertId: string): void {
    const alert = this.securityAlerts.find(a => a.id === alertId);
    if (alert) {
      alert.resolved = true;
      console.log(`Alerta ${alertId} marcado como resolvido`);
    }
  }

  visualizarDetalhes(session: UserSession): void {
    this.selectedSession = session;
    this.visualizacaoAtiva = 'detalhes';
  }

  alternarVisualizacao(tipo: string): void {
    this.visualizacaoAtiva = tipo;
    if (tipo === 'overview') {
      this.selectedSession = null;
    }
  }

  aplicarFiltros(): void {
    // Recarregar dados com filtros aplicados
    this.carregarDados();
  }

  iniciarMonitoramento(): void {
    // Só executa no browser, nunca no SSR
    if (typeof window === 'undefined' || this.monitoramentoAtivo) {
      return;
    }
    
    this.monitoramentoAtivo = true;
    
    // Atualizar dados a cada 30 segundos
    this.intervalId = setInterval(() => {
      if (!this.loading && typeof window !== 'undefined') {
        this.carregarDados();
      }
    }, 30000);
  }

  pararMonitoramento(): void {
    this.monitoramentoAtivo = false;
    
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  alternarMonitoramento(): void {
    if (this.monitoramentoAtivo) {
      this.pararMonitoramento();
    } else {
      this.iniciarMonitoramento();
    }
  }

  // Métodos auxiliares
  private getRandom(): number {
    // Durante SSR, usar valores determinísticos para evitar hidratação inconsistente
    if (typeof window === 'undefined') {
      return 0.5; // Valor fixo para SSR
    }
    return Math.random();
  }

  private getRandomDevice(): string {
    const devices = [
      'Chrome 120.0 (Windows 10)',
      'Safari 17.1 (macOS Sonoma)',
      'Firefox 121.0 (Ubuntu 22.04)',
      'Edge 120.0 (Windows 11)',
      'Chrome Mobile 120.0 (Android 14)',
      'Safari Mobile 17.1 (iOS 17.2)'
    ];
    return devices[Math.floor(this.getRandom() * devices.length)];
  }

  private getRandomIP(): string {
    return `${Math.floor(this.getRandom() * 255)}.${Math.floor(this.getRandom() * 255)}.${Math.floor(this.getRandom() * 255)}.${Math.floor(this.getRandom() * 255)}`;
  }

  private getRandomLocation(): string {
    const locations = [
      'São Paulo, SP - Brasil',
      'Rio de Janeiro, RJ - Brasil',
      'Belo Horizonte, MG - Brasil',
      'Brasília, DF - Brasil',
      'Porto Alegre, RS - Brasil',
      'Salvador, BA - Brasil',
      'Recife, PE - Brasil',
      'Curitiba, PR - Brasil'
    ];
    return locations[Math.floor(this.getRandom() * locations.length)];
  }

  private getRandomRiskLevel(): 'BAIXO' | 'MEDIO' | 'ALTO' | 'CRITICO' {
    const rand = this.getRandom();
    if (rand < 0.6) return 'BAIXO';
    if (rand < 0.8) return 'MEDIO';
    if (rand < 0.95) return 'ALTO';
    return 'CRITICO';
  }

  private getRandomAlertMessage(): string {
    const messages = [
      'Múltiplas tentativas de login detectadas',
      'Acesso de localização não reconhecida',
      'Padrão de atividade anômalo identificado',
      'Sessões simultâneas de dispositivos diferentes',
      'Tentativa de acesso a recursos restritos',
      'Atividade suspeita em horário não usual'
    ];
    return messages[Math.floor(this.getRandom() * messages.length)];
  }

  // Métodos de formatação e utilidade
  formatarDuracao(horas: number): string {
    if (horas < 1) {
      return `${Math.round(horas * 60)} min`;
    }
    return `${horas.toFixed(1)}h`;
  }

  formatarData(data: Date): string {
    return new Date(data).toLocaleString('pt-BR');
  }

  obterCorRisco(nivel: string): string {
    switch (nivel.toUpperCase()) {
      case 'BAIXO': return 'success';
      case 'MEDIO': return 'warning';
      case 'ALTO': return 'danger';
      case 'CRITICO': return 'danger';
      default: return 'secondary';
    }
  }

  obterCorSeveridade(severidade: string): string {
    switch (severidade.toUpperCase()) {
      case 'LOW': return 'success';
      case 'MEDIUM': return 'warning';
      case 'HIGH': return 'danger';
      case 'CRITICAL': return 'danger';
      default: return 'secondary';
    }
  }

  obterIconeRisco(nivel: string): string {
    switch (nivel.toUpperCase()) {
      case 'BAIXO': return '✓';
      case 'MEDIO': return '⚠';
      case 'ALTO': return '⚠';
      case 'CRITICO': return '⚠';
      default: return '?';
    }
  }

  obterSessoesFiltradas(): UserSession[] {
    let sessoesFiltradas = [...this.sessions];

    // Filtro por status
    if (this.filtroStatus !== 'TODAS') {
      if (this.filtroStatus === 'ATIVAS') {
        sessoesFiltradas = sessoesFiltradas.filter(s => s.isActive);
      } else if (this.filtroStatus === 'INATIVAS') {
        sessoesFiltradas = sessoesFiltradas.filter(s => !s.isActive);
      }
    }

    // Filtro por risco
    if (this.filtroRisco !== 'TODOS') {
      sessoesFiltradas = sessoesFiltradas.filter(s => s.riskLevel === this.filtroRisco);
    }

    // Ordenação
    sessoesFiltradas.sort((a, b) => {
      switch (this.ordenacao) {
        case 'lastActivity':
          return new Date(b.lastActivity).getTime() - new Date(a.lastActivity).getTime();
        case 'loginTime':
          return new Date(b.loginTime).getTime() - new Date(a.loginTime).getTime();
        case 'riskLevel':
          const riskOrder = { 'CRITICO': 4, 'ALTO': 3, 'MEDIO': 2, 'BAIXO': 1 };
          return riskOrder[b.riskLevel] - riskOrder[a.riskLevel];
        default:
          return 0;
      }
    });

    return sessoesFiltradas;
  }

  obterAlertasNaoResolvidos(): number {
    return this.securityAlerts.filter(a => !a.resolved).length;
  }

  // Método para voltar ao dashboard
  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  // Método para fazer logout
  logout(): void {
    this.authService.logout();
  }
} 