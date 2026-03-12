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

  // Detalhes das features
  mostrarDetalhesFeatures = false;

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
      this.error = 'Usuario nao autenticado';
      this.loading = false;
      return;
    }

    const userId = this.usuarioAtual.id || 1;

    // Carrega estatísticas e histórico em paralelo
    const subscricao = forkJoin({
      estatisticas: this.apiService.getAnomalyStatistics(),
      historico: this.apiService.getUserAnalysisHistory(userId)
    }).subscribe({
      next: (dados) => {
        this.statisticas = dados.estatisticas;
        this.historicoAnalises = Array.isArray(dados.historico) ? dados.historico : [];
        this.loading = false;

        // Após carregar dados, executar primeira análise
        this.executarAnaliseCompleta();
      },
      error: (erro) => {
        console.error('Erro ao carregar dados de IA:', erro);
        this.error = 'Erro ao carregar dados de analise IA. Verifique sua conexao com o servidor.';
        this.statisticas = null;
        this.historicoAnalises = [];
        this.loading = false;
      }
    });

    this.subscriptions.add(subscricao);
  }

  executarAnaliseCompleta(): void {
    if (this.loading) return;

    this.loading = true;
    this.error = null;

    const userId = this.usuarioAtual?.id || 1;
    const contexto = this.obterContextoAtual();

    // Chama as APIs reais para análise e classificação
    const subscricao = forkJoin({
      analise: this.apiService.analyzeUserBehavior(userId, contexto),
      classificacao: this.apiService.classifyAccess(userId, contexto)
    }).subscribe({
      next: (dados) => {
        this.perfilAtual = dados.analise;
        this.classificacaoAtual = dados.classificacao;
        this.loading = false;

        // Calcular scores detalhados
        this.calcularScoresDetalhados();
      },
      error: (erro) => {
        console.error('Erro ao executar analise completa:', erro);
        this.error = 'Erro ao executar analise comportamental. Verifique sua conexao com o servidor.';
        this.perfilAtual = null;
        this.classificacaoAtual = null;
        this.loading = false;
      }
    });

    this.subscriptions.add(subscricao);
  }

  private obterContextoAtual(): DadosContextoRequest {
    return {
      enderecoIp: '',
      userAgent: typeof navigator !== 'undefined' ? navigator.userAgent : '',
      localizacaoGeografica: '',
      timezone: typeof Intl !== 'undefined' ? Intl.DateTimeFormat().resolvedOptions().timeZone : '',
      idiomaBrowser: typeof navigator !== 'undefined' ? navigator.language : '',
      resolucaoTela: typeof screen !== 'undefined' ? `${screen.width}x${screen.height}` : '',
      tentativasLogin: 1
    };
  }

  calcularScoresDetalhados(): void {
    if (this.calculandoScore) return;

    this.calculandoScore = true;

    const requestScore: CalcularScoreRequest = {
      horaAcesso: new Date().getHours(),
      diaSemana: new Date().getDay(),
      frequenciaAcessoSemanal: 5.2,
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
        this.error = 'Erro ao calcular score de anomalia. Verifique sua conexao com o servidor.';
        this.scoreAtual = null;
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
        this.error = 'Erro ao enviar feedback';
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
        console.error('Erro ao carregar estatisticas:', erro);
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
}
