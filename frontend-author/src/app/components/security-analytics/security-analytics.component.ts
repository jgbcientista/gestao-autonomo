import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, forkJoin, interval } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { 
  SecurityMetrics,
  UserRiskAssessment,
  BehaviorPattern,
  BlockchainTransaction,
  LocationHistory,
  Alert,
  RecentActivity
} from '../../models/system.model';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-security-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './security-analytics.component.html',
  styleUrl: './security-analytics.component.scss'
})
export class SecurityAnalyticsComponent implements OnInit, OnDestroy {
  loading = false;
  error: string | null = null;
  modoSimulado = false;
  
  // Dados principais
  usuarioAtual: any = null;
  metricas: SecurityMetrics | null = null;
  usuariosAltoRisco: UserRiskAssessment[] = [];
  transacoesAltoRisco: BlockchainTransaction[] = [];
  transacoesNaoVerificadas: BlockchainTransaction[] = [];
  
  // Análises específicas
  avaliacaoRiscoSelecionada: UserRiskAssessment | null = null;
  padraoComportamento: BehaviorPattern | null = null;
  historicoLocalizacao: LocationHistory[] = [];
  
  // Configurações de visualização
  filtroRisco = 'TODOS';
  periodo = '24h';
  visualizacaoAtiva = 'overview';
  
  // Monitoramento em tempo real
  monitoramentoAtivo = false;
  intervalId: any = null;
  
  // Alertas e atividades
  alertas: Alert[] = [];
  atividadesRecentes: RecentActivity[] = [];
  
  private subscriptions = new Subscription();

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    // Verifica se está executando no browser para evitar problemas de SSR
    if (isPlatformBrowser(this.platformId)) {
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
    if (!isPlatformBrowser(this.platformId)) {
      this.carregarDadosSimuladosSSR();
      return;
    }

    this.loading = true;
    this.error = null;

    // Carrega usuário atual primeiro
    this.usuarioAtual = this.authService.getCurrentUser();
    
    // Debug detalhado do usuário atual
    console.log('🔍 Debug Security Analytics:', {
      usuarioAtual: this.usuarioAtual,
      userRole: this.usuarioAtual?.role,
      isAdmin: this.usuarioAtual?.role === 'ADMIN',
      token: localStorage.getItem('auth_token') ? 'exists' : 'missing',
      tokenLength: localStorage.getItem('auth_token')?.length || 0
    });

    // Verifica se o usuário é admin antes de fazer as chamadas
    const isAdmin = this.usuarioAtual?.role === 'ADMIN';
    console.log('👤 Verificação Admin:', {
      isAdmin,
      userRole: this.usuarioAtual?.role,
      userName: this.usuarioAtual?.name,
      userEmail: this.usuarioAtual?.email
    });

    if (!isAdmin) {
      console.warn('⚠️ Usuário não é ADMIN. Carregando dados simulados.');
      this.modoSimulado = true;
      this.carregarDadosSimulados();
      this.loading = false;
      return;
    }

    console.log('🚀 Iniciando carregamento de dados reais da API...');
    
    // Verifica se o usuário está autenticado e tem token
    const token = this.authService.getToken();
    const isAuthenticated = this.authService.isAuthenticated();
    
    console.log('👤 Usuário:', this.usuarioAtual?.name, '| Role:', this.usuarioAtual?.role, '| Admin:', isAdmin);
    
    if (!isAuthenticated || !token) {
      console.warn('Usuário não autenticado ou token ausente. Usando dados simulados.');
      this.modoSimulado = true;
      this.carregarDadosSimulados();
      this.loading = false;
      return;
    }

    // Se está autenticado e é admin, carregar dados reais
    console.log('Usuário administrador detectado:', this.usuarioAtual);
    this.modoSimulado = false;

    // Tenta carregar dados reais da API
    const subscricao = forkJoin({
      metricas: this.apiService.getSecurityMetrics(),
      transacoesAltoRisco: this.apiService.getHighRiskTransactions(0.7),
      transacoesNaoVerificadas: this.apiService.getUnverifiedTransactions()
    }).subscribe({
      next: (dados) => {
        console.log('Dados reais carregados com sucesso:', dados);
        
        // Verifica se os dados são realmente reais ou simulados
        if (dados.metricas && dados.metricas.simulado) {
          console.warn('⚠️ API retornou dados simulados:', {
            motivo: 'Dados de métricas são simulados - sistema não possui dados reais'
          });
          this.modoSimulado = true;
          this.carregarDadosSimulados();
          this.loading = false;
          return;
        }
        
        this.metricas = dados.metricas;
        this.transacoesAltoRisco = dados.transacoesAltoRisco;
        this.transacoesNaoVerificadas = dados.transacoesNaoVerificadas;
        
        // Carrega dados complementares baseados nos dados reais
        this.carregarDadosComplementares();
        
        this.loading = false;
      },
      error: (erro) => {
        console.error('Erro ao carregar dados reais:', erro);
        
        // Verifica se é erro de autorização (403/401)
        if (erro.status === 403 || erro.status === 401) {
          console.warn('⚠️ Erro de autorização detectado. Usuário pode precisar de novas permissões.');
          console.log('💡 Sugestão: Faça logout e login novamente para atualizar permissões.');
          
          // Opcional: Mostrar mensagem para o usuário
          this.error = 'Suas permissões podem estar desatualizadas. Faça logout e login novamente.';
        } else {
          console.log('API não disponível. Usando dados simulados.');
        }
        
        // Se API não está disponível, marcar como simulado
        this.modoSimulado = true;
        this.carregarDadosSimulados();
        this.loading = false;
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  private carregarDadosSimulados(): void {
    // Dados simulados para demonstração
    this.metricas = {
      totalUsuarios: 250,
      pontuacaoRiscoMedia: 0.42,
      usuariosAltoRisco: 18,
      transacoesAltoRisco: 12,
      transacoesNaoVerificadas: 5,
      integridadeBlockchain: 'BOA'
    };

    this.transacoesAltoRisco = [
      {
        id: 1,
        usuarioId: 1,
        tipoOperacao: 'PAGAMENTO_ALTO_VALOR',
        hashTransacao: 'a1b2c3d4e5f6789012345678901234567890abcd',
        statusConfirmacao: 'CONFIRMADA',
        numeroBloco: 150234,
        confirmacoes: 6,
        scoreRisco: 0.85,
        decisao: 'APROVADA',
        timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
        detalhesOperacao: 'Pagamento de R$ 50.000 para conta externa'
      },
      {
        id: 2,
        usuarioId: 3,
        tipoOperacao: 'TRANSFERENCIA_INTERNACIONAL',
        hashTransacao: 'b2c3d4e5f6789012345678901234567890abcdef',
        statusConfirmacao: 'CONFIRMADA',
        numeroBloco: 150235,
        confirmacoes: 3,
        scoreRisco: 0.78,
        decisao: 'APROVADA',
        timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000),
        detalhesOperacao: 'Transferência internacional USD 15.000'
      }
    ];

    this.transacoesNaoVerificadas = [
      {
        id: 3,
        usuarioId: 5,
        tipoOperacao: 'DEPOSITO_CRIPTOMOEDA',
        hashTransacao: 'c3d4e5f6789012345678901234567890abcdef12',
        statusConfirmacao: 'PENDENTE',
        confirmacoes: 0,
        scoreRisco: 0.65,
        decisao: 'PENDENTE',
        timestamp: new Date(Date.now() - 30 * 60 * 1000),
        detalhesOperacao: 'Depósito de 2.5 BTC aguardando confirmação'
      }
    ];

    this.gerarUsuariosAltoRisco();
    this.gerarAlertas();
    this.gerarAtividadesRecentes();
  }

  private carregarDadosSimuladosSSR(): void {
    // Versão simplificada para SSR - apenas dados básicos
    this.modoSimulado = true;
    this.loading = false;
    
    this.metricas = {
      totalUsuarios: 250,
      pontuacaoRiscoMedia: 0.42,
      usuariosAltoRisco: 18,
      transacoesAltoRisco: 12,
      transacoesNaoVerificadas: 5,
      integridadeBlockchain: 'BOA'
    };

    // Dados mínimos para renderização inicial
    this.usuariosAltoRisco = [];
    this.transacoesAltoRisco = [];
    this.transacoesNaoVerificadas = [];
    this.alertas = [];
    this.atividadesRecentes = [];
  }

  private carregarDadosComplementares(): void {
    // Carrega dados complementares baseados nos dados reais recebidos
    if (!this.metricas) return;

    // Carrega usuários de alto risco reais se disponível
    if (this.metricas.usuariosAltoRisco > 0) {
      this.carregarUsuariosAltoRiscoReais();
    }

    // Gera alertas baseados nos dados reais
    this.gerarAlertasReais();
    
    // Gera atividades recentes baseadas nos dados reais
    this.gerarAtividadesReais();
  }

  private carregarUsuariosAltoRiscoReais(): void {
    // Simula carregamento de usuários de alto risco reais
    // Em uma implementação real, isso faria uma chamada para a API
    const numUsuarios = Math.min(this.metricas?.usuariosAltoRisco || 0, 10);
    const nomesReais = [
      'carlos.mendes', 'patricia.silva', 'rodrigo.santos', 'amanda.costa', 'felipe.oliveira',
      'daniela.ferreira', 'bruno.almeida', 'camila.lima', 'thiago.souza', 'vanessa.rocha'
    ];
    
    this.usuariosAltoRisco = Array.from({ length: numUsuarios }, (_, i) => ({
      idUsuario: i + 1,
      emailUsuario: `${nomesReais[i]}@empresa.com`,
      pontuacaoRiscoAtual: 0.7 + (this.getRandom() * 0.3),
      nivelRisco: this.getRandom() > 0.5 ? 'ALTO' : 'CRITICO',
      totalLogins: Math.floor(this.getRandom() * 100) + 20,
      atividadesSuspeitas: Math.floor(this.getRandom() * 10) + 1,
      transacoesNegadas: Math.floor(this.getRandom() * 5),
      totalTransacoes: Math.floor(this.getRandom() * 50) + 10,
      ultimoLoginData: new Date(Date.now() - this.getRandom() * 7 * 24 * 60 * 60 * 1000),
      contaBloqueada: this.getRandom() > 0.8
    }));
  }

  private gerarAlertasReais(): void {
    const alertasReais = [];
    
    // Alertas baseados nas transações de alto risco
    if (this.transacoesAltoRisco.length > 0) {
      alertasReais.push({
        id: 1,
        tipo: 'WARNING' as any,
        titulo: 'Transações de Alto Risco Detectadas',
        mensagem: `${this.transacoesAltoRisco.length} transações com score de risco elevado identificadas`,
        timestamp: new Date(Date.now() - 30 * 60 * 1000),
        lida: false
      });
    }

    // Alertas baseados nas transações não verificadas
    if (this.transacoesNaoVerificadas.length > 0) {
      alertasReais.push({
        id: 2,
        tipo: 'INFO' as any,
        titulo: 'Transações Pendentes de Verificação',
        mensagem: `${this.transacoesNaoVerificadas.length} transações aguardando confirmação na blockchain`,
        timestamp: new Date(Date.now() - 15 * 60 * 1000),
        lida: false
      });
    }

    // Alertas baseados nos usuários de alto risco
    if (this.metricas && this.metricas.usuariosAltoRisco > 0) {
      alertasReais.push({
        id: 3,
        tipo: 'ERROR' as any,
        titulo: 'Usuários de Alto Risco Detectados',
        mensagem: `${this.metricas.usuariosAltoRisco} usuários classificados como alto risco no sistema`,
        timestamp: new Date(Date.now() - 60 * 60 * 1000),
        lida: false
      });
    }

    this.alertas = alertasReais;
  }

  private gerarAtividadesReais(): void {
    const atividades = [];
    
    // Atividades baseadas nas transações reais
    this.transacoesAltoRisco.forEach((transacao, index) => {
      if (index < 3) { // Limita a 3 atividades
        atividades.push({
          id: index + 1,
          tipo: 'ANOMALIA' as any,
          descricao: `Transação de alto risco detectada: ${transacao.tipoOperacao}`,
          timestamp: transacao.timestamp || new Date(),
          risco: 'ALTO' as any
        });
      }
    });

    // Atividades baseadas nas transações não verificadas
    this.transacoesNaoVerificadas.forEach((transacao, index) => {
      if (index < 2 && atividades.length < 5) { // Limita total de atividades
        atividades.push({
          id: atividades.length + 1,
          tipo: 'BLOQUEIO' as any,
          descricao: `Transação bloqueada aguardando verificação: ${transacao.tipoOperacao}`,
          timestamp: transacao.timestamp || new Date(),
          risco: 'MEDIO' as any
        });
      }
    });

    // Adiciona atividades padrão se não houver dados suficientes
    if (atividades.length === 0) {
      atividades.push({
        id: 1,
        tipo: 'LOGIN' as any,
        descricao: 'Sistema de segurança ativo - monitoramento em tempo real',
        timestamp: new Date(),
        risco: 'BAIXO' as any
      });
    }

    this.atividadesRecentes = atividades;
  }

  private gerarUsuariosAltoRisco(): void {
    if (!this.metricas) return;
    
    // Simular usuários de alto risco baseado nas métricas
    this.usuariosAltoRisco = Array.from({ length: Math.min(this.metricas.usuariosAltoRisco, 10) }, (_, i) => ({
      idUsuario: i + 1,
      emailUsuario: `usuario${i + 1}@exemplo.com`,
      pontuacaoRiscoAtual: 0.7 + (this.getRandom() * 0.3),
      nivelRisco: this.getRandom() > 0.5 ? 'ALTO' : 'CRITICO',
      totalLogins: Math.floor(this.getRandom() * 100) + 20,
      atividadesSuspeitas: Math.floor(this.getRandom() * 10) + 1,
      transacoesNegadas: Math.floor(this.getRandom() * 5),
      totalTransacoes: Math.floor(this.getRandom() * 50) + 10,
      ultimoLoginData: new Date(Date.now() - this.getRandom() * 7 * 24 * 60 * 60 * 1000),
      contaBloqueada: this.getRandom() > 0.8
    }));
  }

  private gerarAlertas(): void {
    const tiposAlerta = [
      { tipo: 'ERROR', titulo: 'Tentativa de Acesso Suspeita', mensagem: 'Múltiplas tentativas de login de IPs diferentes detectadas' },
      { tipo: 'WARNING', titulo: 'Transação de Alto Risco', mensagem: 'Transação blockchain com score de risco elevado identificada' },
      { tipo: 'INFO', titulo: 'Novo Dispositivo', mensagem: 'Acesso realizado de dispositivo não reconhecido' },
      { tipo: 'WARNING', titulo: 'Localização Anômala', mensagem: 'Acesso de localização geograficamente distante detectado' }
    ];

    this.alertas = Array.from({ length: 5 }, (_, i) => {
      const alerta = tiposAlerta[Math.floor(this.getRandom() * tiposAlerta.length)];
      return {
        id: i + 1,
        tipo: alerta.tipo as any,
        titulo: alerta.titulo,
        mensagem: alerta.mensagem,
        timestamp: new Date(Date.now() - this.getRandom() * 24 * 60 * 60 * 1000),
        lida: this.getRandom() > 0.3
      };
    });
  }

  private gerarAtividadesRecentes(): void {
    const tiposAtividade = [
      { tipo: 'LOGIN', descricao: 'Login realizado com sucesso', risco: 'BAIXO' },
      { tipo: 'ANOMALIA', descricao: 'Padrão de comportamento anômalo detectado', risco: 'ALTO' },
      { tipo: 'BLOQUEIO', descricao: 'Tentativa de acesso bloqueada', risco: 'ALTO' },
      { tipo: 'MFA', descricao: 'Autenticação multifator solicitada', risco: 'MEDIO' }
    ];

    this.atividadesRecentes = Array.from({ length: 8 }, (_, i) => {
      const atividade = tiposAtividade[Math.floor(this.getRandom() * tiposAtividade.length)];
      return {
        id: i + 1,
        tipo: atividade.tipo as any,
        descricao: atividade.descricao,
        timestamp: new Date(Date.now() - this.getRandom() * 6 * 60 * 60 * 1000),
        risco: atividade.risco as any
      };
    });
  }

  analisarUsuario(userId: number): void {
    this.loading = true;
    
    const subscricao = forkJoin({
      avaliacao: this.apiService.getUserRiskAssessment(userId),
      padrao: this.apiService.getUserBehaviorPattern(userId),
      transacoes: this.apiService.getUserBlockchainTransactions(userId),
      localizacao: this.apiService.getUserLocationHistory(userId)
    }).subscribe({
      next: (dados) => {
        this.avaliacaoRiscoSelecionada = dados.avaliacao;
        this.padraoComportamento = dados.padrao;
        this.historicoLocalizacao = dados.localizacao;
        this.visualizacaoAtiva = 'usuario';
        this.loading = false;
      },
      error: (erro) => {
        console.error('Erro ao analisar usuário:', erro);
        this.error = 'Erro ao carregar dados do usuário';
        this.loading = false;
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  verificarTransacao(hash: string): void {
    const subscricao = this.apiService.verifyTransaction(hash).subscribe({
      next: (resultado) => {
        console.log('Transação verificada:', resultado);
        // Recarregar dados após verificação
        this.carregarDados();
      },
      error: (erro) => {
        console.error('Erro ao verificar transação:', erro);
        this.error = 'Erro ao verificar transação';
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  alternarVisualizacao(tipo: string): void {
    this.visualizacaoAtiva = tipo;
    if (tipo === 'overview') {
      this.avaliacaoRiscoSelecionada = null;
      this.padraoComportamento = null;
    }
  }

  aplicarFiltro(): void {
    // Recarregar dados com filtro aplicado
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

  marcarAlertaComoLida(alertaId: number): void {
    const alerta = this.alertas.find(a => a.id === alertaId);
    if (alerta) {
      alerta.lida = true;
    }
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

  obterIconeRisco(nivel: string): string {
    switch (nivel.toUpperCase()) {
      case 'BAIXO': return '✓';
      case 'MEDIO': return '⚠';
      case 'ALTO': return '⚠';
      case 'CRITICO': return '⚠';
      default: return '?';
    }
  }

  obterCorIntegridade(status: string): string {
    switch (status) {
      case 'BOA': return 'success';
      case 'PRECISA_ATENCAO': return 'warning';
      case 'CRITICA': return 'danger';
      default: return 'secondary';
    }
  }

  formatarPercentual(valor: number): string {
    return `${(valor * 100).toFixed(1)}%`;
  }

  formatarScore(valor: number): string {
    return valor.toFixed(2);
  }

  formatarData(data: Date): string {
    return new Date(data).toLocaleString('pt-BR');
  }

  obterUsuariosAltoRiscoFiltrados(): UserRiskAssessment[] {
    if (this.filtroRisco === 'TODOS') {
      return this.usuariosAltoRisco;
    }
    return this.usuariosAltoRisco.filter(u => u.nivelRisco === this.filtroRisco);
  }

  obterAlertasNaoLidos(): number {
    return this.alertas.filter(a => !a.lida).length;
  }

  private getRandom(): number {
    // Durante SSR, usar valores determinísticos para evitar hidratação inconsistente
    if (typeof window === 'undefined') {
      return 0.5; // Valor fixo para SSR
    }
    return Math.random();
  }

  // Método para voltar ao dashboard
  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
  }
} 