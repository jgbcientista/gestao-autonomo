import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subscription, forkJoin } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { 
  PerfilComportamentalIA, 
  ClassificacaoResponse, 
  DadosContextoRequest, 
  CalcularScoreRequest, 
  ScoreResponse, 
  EstatisticasAnomalias,
  FeedbackRequest
} from '../../models/system.model';

@Component({
  selector: 'app-ai-analysis',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './ai-analysis.component.html',
  styleUrl: './ai-analysis.component.scss'
})
export class AiAnalysisComponent implements OnInit, OnDestroy {
  loading = false;
  error: string | null = null;
  
  // Dados principais
  usuarioAtual: any = null;
  perfilAtual: PerfilComportamentalIA | null = null;
  classificacaoAtual: ClassificacaoResponse | null = null;
  statisticas: EstatisticasAnomalias | null = null;
  historicoAnalises: PerfilComportamentalIA[] = [];
  
  // Scores de anomalia
  scoreAtual: ScoreResponse | null = null;
  calculandoScore = false;
  
  // Treinamento de modelos
  treinandoModelos = false;
  ultimoTreinamento: Date | null = null;
  
  // Feedback
  feedbackAtivo = false;
  feedbackComentario = '';
  
  // Análise em tempo real
  monitoramentoAtivo = false;
  intervalId: any = null;
  
  private subscriptions = new Subscription();

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  // Getter para data atual
  get dataAtual(): Date {
    return new Date();
  }

  ngOnInit(): void {
    this.carregarDados();
    this.iniciarMonitoramento();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.pararMonitoramento();
  }

  carregarDados(): void {
    this.loading = true;
    this.error = null;
    
    this.usuarioAtual = this.authService.getCurrentUser();
    
    if (!this.usuarioAtual) {
      this.error = 'Usuário não autenticado';
      this.loading = false;
      return;
    }

    // Carrega estatísticas primeiro (não precisam de userId específico)
    const subscricao = this.apiService.getAnomalyStatistics().subscribe({
      next: (estatisticas) => {
        this.statisticas = estatisticas;
        
        // Simular histórico de análises se não conseguir do backend
        this.historicoAnalises = this.gerarHistoricoSimulado();
        
        this.loading = false;
        
        // Após carregar dados, executar primeira análise
        this.executarAnaliseCompleta();
      },
      error: (erro) => {
        console.error('Erro ao carregar estatísticas de IA:', erro);
        
        // Se falhar, usar dados simulados
        this.estatisticasSimuladas();
        this.historicoAnalises = this.gerarHistoricoSimulado();
        
        this.loading = false;
        this.executarAnaliseCompleta();
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  executarAnaliseCompleta(): void {
    if (this.loading) return;
    
    this.loading = true;
    this.error = null;
    const contexto = this.obterContextoAtual();
    
    // Usar dados simulados para análise (já que não temos usuários reais no backend)
    this.perfilAtual = this.gerarPerfilSimulado();
    this.classificacaoAtual = this.gerarClassificacaoSimulada();
    this.loading = false;
    
    // Calcular scores detalhados
    this.calcularScoresDetalhados();
  }

  private obterContextoAtual(): DadosContextoRequest {
    return {
      enderecoIp: '192.168.1.100', // Mock - em produção seria obtido do servidor
      userAgent: navigator.userAgent,
      localizacaoGeografica: 'São Paulo, BR',
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      idiomaBrowser: navigator.language,
      resolucaoTela: `${screen.width}x${screen.height}`,
      tentativasLogin: 1
    };
  }

  calcularScoresDetalhados(): void {
    if (this.calculandoScore) return;
    
    this.calculandoScore = true;
    
    const requestScore: CalcularScoreRequest = {
      horaAcesso: new Date().getHours(),
      diaSemana: new Date().getDay(),
      frequenciaAcessoSemanal: 5.2, // Mock
      ipJaUtilizado: true,
      dispositivoJaUtilizado: true,
      localizacaoJaUtilizada: true,
      distanciaLocalizacaoHabitualKm: 0,
      diferencaHorarioHabitualHoras: 1.5,
      tempoDesdeUltimoAcessoHoras: 8,
      mediaSessoesDiarias: 3.2,
      desvioPadraoHorarios: 2.1,
      totalIpsDistintos: 3,
      totalDispositivosDistintos: 2,
      padroesNavegacaoScore: 0.85
    };
    
    const subscricao = this.apiService.calculateAnomalyScore(requestScore).subscribe({
      next: (score) => {
        this.scoreAtual = score;
        this.calculandoScore = false;
      },
      error: (erro) => {
        console.error('Erro ao calcular scores:', erro);
        
        // Se falhar, usar scores simulados
        this.scoreAtual = this.gerarScoresSimulados();
        this.calculandoScore = false;
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  treinarModelos(): void {
    if (this.treinandoModelos) return;
    
    this.treinandoModelos = true;
    
    const subscricao = this.apiService.trainAIModels().subscribe({
      next: () => {
        this.ultimoTreinamento = new Date();
        this.treinandoModelos = false;
        
        // Recarregar estatísticas após treinamento
        this.carregarEstatisticas();
      },
      error: (erro) => {
        console.error('Erro ao treinar modelos:', erro);
        this.error = 'Erro ao treinar modelos de IA';
        this.treinandoModelos = false;
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  enviarFeedback(perfilId: number, acessoLegitimo: boolean): void {
    const feedback: FeedbackRequest = {
      acessoLegitimo,
      comentario: this.feedbackComentario
    };
    
    const subscricao = this.apiService.sendAIFeedback(perfilId, feedback).subscribe({
      next: () => {
        this.feedbackAtivo = false;
        this.feedbackComentario = '';
        console.log('Feedback enviado com sucesso');
      },
      error: (erro) => {
        console.error('Erro ao enviar feedback:', erro);
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  private carregarEstatisticas(): void {
    const subscricao = this.apiService.getAnomalyStatistics().subscribe({
      next: (estatisticas) => {
        this.statisticas = estatisticas;
      },
      error: (erro) => {
        console.error('Erro ao carregar estatísticas:', erro);
      }
    });
    
    this.subscriptions.add(subscricao);
  }

  iniciarMonitoramento(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    
    this.monitoramentoAtivo = true;
    
    // Executa análise a cada 30 segundos
    this.intervalId = setInterval(() => {
      if (!this.loading) {
        this.executarAnaliseCompleta();
      }
    }, 30000);
  }

  pararMonitoramento(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
    this.monitoramentoAtivo = false;
  }

  alternarMonitoramento(): void {
    if (this.monitoramentoAtivo) {
      this.pararMonitoramento();
    } else {
      this.iniciarMonitoramento();
    }
  }

  obterCorClassificacao(classificacao: string): string {
    switch (classificacao?.toLowerCase()) {
      case 'normal':
      case 'esperado':
        return 'success';
      case 'suspeito':
      case 'moderado':
        return 'warning';
      case 'anomalo':
      case 'alto':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  obterIconeClassificacao(classificacao: string): string {
    switch (classificacao?.toLowerCase()) {
      case 'normal':
      case 'esperado':
        return '✅';
      case 'suspeito':
      case 'moderado':
        return '⚠️';
      case 'anomalo':
      case 'alto':
        return '🚨';
      default:
        return '❓';
    }
  }

  formatarPercentual(valor: number): string {
    if (!valor && valor !== 0) return '0%';
    return `${Math.round(valor * 100)}%`;
  }

  formatarScore(valor: number): string {
    if (!valor && valor !== 0) return '0.000';
    return valor.toFixed(3);
  }

  // Métodos de navegação
  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // === MÉTODOS DE SIMULAÇÃO DE DADOS ===
  
  private estatisticasSimuladas(): void {
    this.statisticas = {
      totalAnalises: 1247,
      totalAnomalias: 89,
      percentualAnomalias: 7.14,
      taxaAcuracia: 94.2,
      tempoMedioAnalise: 1.8,
      distribuicaoClassificacoes: {
        'ESPERADO': 1158,
        'SUSPEITO': 45,
        'ANOMALO': 32,
        'ALTAMENTE_SUSPEITO': 12
      },
      precisaoModelo: 92.5,
      ultimoTreinamento: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 dia atrás
      modelosAtivos: ['Isolation Forest', 'Random Forest', 'Deep Learning', 'Ensemble']
    };
  }

  private gerarHistoricoSimulado(): PerfilComportamentalIA[] {
    const historico: PerfilComportamentalIA[] = [];
    const classificacoes = ['ESPERADO', 'SUSPEITO', 'ANOMALO'];
    
    for (let i = 0; i < 10; i++) {
      const data = new Date(Date.now() - i * 2 * 60 * 60 * 1000); // A cada 2 horas
      historico.push({
        id: i + 1,
        usuarioId: 1,
        classificacao: classificacoes[Math.floor(Math.random() * classificacoes.length)] as any,
        scoreAnomaliaGlobal: Math.random() * 0.3 + (i < 3 ? 0.7 : 0.1), // Primeiros mais suspeitos
        confiabilidade: Math.random() * 0.2 + 0.8,
        scoresComportamentais: {
          temporal: Math.random() * 0.4 + 0.6,
          localizacao: Math.random() * 0.3 + 0.7,
          dispositivo: Math.random() * 0.2 + 0.8,
          navegacao: Math.random() * 0.3 + 0.7
        },
        dadosContexto: {
          enderecoIp: `192.168.1.${100 + i}`,
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
          localizacaoGeografica: 'São Paulo, BR',
          timezone: 'America/Sao_Paulo',
          idiomaBrowser: 'pt-BR',
          resolucaoTela: '1920x1080',
          tentativasLogin: 1
        },
        timestamp: data,
        modelosUtilizados: ['IsolationForest', 'RandomForest', 'DeepLearning']
      });
    }
    
    return historico;
  }

  private gerarPerfilSimulado(): PerfilComportamentalIA {
    return {
      id: 1,
      usuarioId: 1,
      classificacao: 'ESPERADO',
      scoreAnomaliaGlobal: 0.15,
      confiabilidade: 0.92,
      scoresComportamentais: {
        temporal: 0.85,
        localizacao: 0.91,
        dispositivo: 0.96,
        navegacao: 0.88,
        comportamental: 0.89
      },
      dadosContexto: this.obterContextoAtual(),
      timestamp: new Date(),
      modelosUtilizados: ['IsolationForest', 'RandomForest', 'DeepLearning', 'Ensemble']
    };
  }

  private gerarClassificacaoSimulada(): ClassificacaoResponse {
    return {
      classificacao: 'ESPERADO',
      descricao: 'Acesso dentro dos padrões esperados do usuário',
      nivelRisco: 'BAIXO'
    };
  }

  private gerarScoresSimulados(): ScoreResponse {
    return {
      isolationForest: 0.12,
      randomForest: 0.08,
      deepLearning: 0.15,
      ensemble: 0.11,
      detalhes: {
        'isolation_forest': 0.12,
        'random_forest': 0.08,
        'deep_learning': 0.15,
        'ensemble': 0.11,
        'comportamental': 0.09,
        'temporal': 0.14,
        'localizacao': 0.06,
        'dispositivo': 0.03
      }
    };
  }
}
