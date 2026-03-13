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

  // Seleção de usuário
  usuarios: any[] = [];
  selectedUserId: number | null = null;

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
    this.usuarioAtual = this.authService.getCurrentUser();

    if (!this.usuarioAtual) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUsuarios();
    this.carregarEstatisticas();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.pararMonitoramento();
  }

  loadUsuarios(): void {
    this.apiService.getUsuariosCadastrados().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        // Seleciona o usuário logado por padrão
        if (this.usuarioAtual?.email) {
          const found = usuarios.find((u: any) => u.email === this.usuarioAtual.email);
          if (found) {
            this.selectedUserId = found.id;
            this.onUsuarioSelecionado();
          }
        }
      },
      error: (err) => {
        console.error('Erro ao carregar usuarios:', err);
        this.error = 'Erro ao carregar lista de usuários.';
      }
    });
  }

  onUsuarioSelecionado(): void {
    if (!this.selectedUserId) {
      this.perfilAtual = null;
      this.classificacaoAtual = null;
      this.scoreAtual = null;
      this.historicoAnalises = [];
      this.pararMonitoramento();
      return;
    }

    this.carregarDadosUsuario();
    this.iniciarMonitoramento();
  }

  carregarDadosUsuario(): void {
    if (!this.selectedUserId) return;

    this.loading = true;
    this.error = null;

    const userId = this.selectedUserId;

    const subscricao = forkJoin({
      estatisticas: this.apiService.getAnomalyStatistics(),
      historico: this.apiService.getUserAnalysisHistory(userId)
    }).subscribe({
      next: (dados) => {
        this.statisticas = dados.estatisticas;
        this.historicoAnalises = Array.isArray(dados.historico) ? dados.historico : [];
        this.loading = false;

        this.executarAnaliseCompleta();
      },
      error: (erro) => {
        console.error('Erro ao carregar dados de IA:', erro);
        this.error = 'Erro ao carregar dados de análise IA. Verifique sua conexão com o servidor.';
        this.statisticas = null;
        this.historicoAnalises = [];
        this.loading = false;
      }
    });

    this.subscriptions.add(subscricao);
  }

  executarAnaliseCompleta(): void {
    if (this.loading || !this.selectedUserId) return;

    this.loading = true;
    this.error = null;

    const userId = this.selectedUserId;
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

  getUsuarioSelecionadoNome(): string {
    const u = this.usuarios.find((u: any) => u.id === this.selectedUserId);
    return u?.nome || 'Usuário';
  }

  getDescricaoAnalise(): string {
    if (!this.perfilAtual && !this.classificacaoAtual) return '';

    const nome = this.getUsuarioSelecionadoNome();
    const scoreAnomalia = this.perfilAtual?.scoreAnomaliaGlobal || 0;
    const confiabilidade = this.perfilAtual?.confiabilidade || 0;
    const classificacao = this.classificacaoAtual?.classificacao || '';
    const nivelRisco = this.classificacaoAtual?.nivelRisco || '';
    let desc = '';

    // Classificação geral
    desc += `O usuário ${nome} foi classificado como "${classificacao}" com nível de risco "${nivelRisco}". `;

    // Score de anomalia
    if (scoreAnomalia <= 0.3) {
      desc += `O score global de anomalia é ${this.formatarScore(scoreAnomalia)}, indicando um comportamento consistente com o padrão habitual do usuário. `;
    } else if (scoreAnomalia <= 0.7) {
      desc += `O score global de anomalia é ${this.formatarScore(scoreAnomalia)}, indicando desvios moderados em relação ao padrão habitual — isso pode ocorrer por mudança de dispositivo, horário incomum ou localização diferente. `;
    } else {
      desc += `O score global de anomalia é ${this.formatarScore(scoreAnomalia)}, indicando desvios significativos do comportamento esperado — múltiplos fatores como IP desconhecido, horário atípico ou dispositivo novo contribuíram para esse resultado. `;
    }

    // Scores dos algoritmos
    if (this.scoreAtual) {
      const scores = [];
      if (this.scoreAtual.isolationForest !== undefined) {
        scores.push(`Isolation Forest: ${this.formatarScore(this.scoreAtual.isolationForest)}`);
      }
      if (this.scoreAtual.randomForest !== undefined) {
        scores.push(`Random Forest: ${this.formatarScore(this.scoreAtual.randomForest)}`);
      }
      if (this.scoreAtual.deepLearning !== undefined) {
        scores.push(`Deep Learning: ${this.formatarScore(this.scoreAtual.deepLearning)}`);
      }
      if (scores.length > 0) {
        desc += `Os modelos de IA retornaram os seguintes scores individuais: ${scores.join(', ')}. `;
        if (this.scoreAtual.ensemble !== undefined) {
          desc += `O score ensemble (combinação ponderada dos 3 modelos) resultou em ${this.formatarScore(this.scoreAtual.ensemble)}. `;
        }
      }
    }

    // Confiabilidade
    desc += `A confiabilidade desta análise é de ${this.formatarPercentual(confiabilidade)}. `;

    // Scores comportamentais detalhados
    if (this.perfilAtual?.scoresComportamentais) {
      const fatoresAltos: string[] = [];
      for (const [key, value] of Object.entries(this.perfilAtual.scoresComportamentais)) {
        if ((value as number) > 0.5) {
          fatoresAltos.push(key);
        }
      }
      if (fatoresAltos.length > 0) {
        desc += `Os fatores comportamentais que mais contribuíram para o score foram: ${fatoresAltos.join(', ')}. `;
      }
    }

    // Decisão
    if (scoreAnomalia <= 0.3) {
      desc += `Com base nesta análise, o sistema recomenda permitir o acesso normalmente.`;
    } else if (scoreAnomalia <= 0.7) {
      desc += `Com base nesta análise, o sistema recomenda exigir autenticação multi-fator (MFA) antes de liberar o acesso.`;
    } else {
      desc += `Com base nesta análise, o sistema recomenda bloquear o acesso por motivos de segurança.`;
    }

    return desc;
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
