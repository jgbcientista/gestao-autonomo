import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subscription, forkJoin } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-blockchain',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './blockchain.component.html',
  styleUrl: './blockchain.component.scss'
})
export class BlockchainComponent implements OnInit, OnDestroy {
  loading = false;
  error: string | null = null;
  usuarioAtual: any = null;

  // Estatísticas gerais
  estatisticas: any = null;

  // Seleção de usuário
  usuarios: any[] = [];
  selectedUserId: number | null = null;

  // Transações do usuário
  transacoesUsuario: any[] = [];
  relatorioUsuario: any = null;

  // Todas as transações
  todasTransacoes: any[] = [];
  carregandoTransacoes = false;

  // Transações de alto risco
  transacoesAltoRisco: any[] = [];
  limiteRisco = 0.7;

  // Transação selecionada para detalhes
  transacaoSelecionada: any = null;

  // Busca por hash
  hashBusca = '';
  transacaoBuscada: any = null;
  buscandoHash = false;

  // Verificação de integridade
  hashVerificacao = '';
  resultadoVerificacao: any = null;
  verificando = false;

  // Aba ativa
  abaAtiva: 'visao-geral' | 'transacoes' | 'alto-risco' | 'integridade' = 'visao-geral';

  // Paginação
  paginaUsuario = 1;
  tamanhoPaginaUsuario = 10;
  paginaAltoRisco = 1;
  tamanhoPaginaAltoRisco = 10;

  private subscriptions = new Subscription();

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuarioAtual = this.authService.getCurrentUser();
    if (!this.usuarioAtual) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUsuarios();
    this.carregarEstatisticas();
    this.carregarTodasTransacoes();
    this.carregarTransacoesAltoRisco();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadUsuarios(): void {
    this.apiService.getUsuariosCadastrados().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
      },
      error: (err) => {
        console.error('Erro ao carregar usuários:', err);
      }
    });
  }

  carregarEstatisticas(): void {
    this.loading = true;
    const sub = this.apiService.getBlockchainStatistics().subscribe({
      next: (stats) => {
        this.estatisticas = stats;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar estatísticas blockchain:', err);
        this.error = 'Erro ao carregar estatísticas da blockchain.';
        this.loading = false;
      }
    });
    this.subscriptions.add(sub);
  }

  onUsuarioSelecionado(): void {
    if (!this.selectedUserId) {
      this.transacoesUsuario = [];
      this.relatorioUsuario = null;
      return;
    }
    this.paginaUsuario = 1;
    this.carregarTransacoesUsuario();
  }

  carregarTransacoesUsuario(): void {
    if (!this.selectedUserId) return;
    this.loading = true;
    this.error = null;

    const sub = forkJoin({
      transacoes: this.apiService.getBlockchainUserTransactions(this.selectedUserId),
      relatorio: this.apiService.getBlockchainUserReport(this.selectedUserId)
    }).subscribe({
      next: (dados) => {
        this.transacoesUsuario = Array.isArray(dados.transacoes) ? dados.transacoes : [];
        this.relatorioUsuario = dados.relatorio;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar transações:', err);
        this.error = 'Erro ao carregar transações do usuário.';
        this.transacoesUsuario = [];
        this.relatorioUsuario = null;
        this.loading = false;
      }
    });
    this.subscriptions.add(sub);
  }

  carregarTodasTransacoes(): void {
    this.carregandoTransacoes = true;
    const sub = this.apiService.getBlockchainAllTransactions(100).subscribe({
      next: (transacoes) => {
        this.todasTransacoes = Array.isArray(transacoes) ? transacoes : [];
        this.carregandoTransacoes = false;
      },
      error: (err) => {
        console.error('Erro ao carregar todas as transações:', err);
        this.carregandoTransacoes = false;
      }
    });
    this.subscriptions.add(sub);
  }

  carregarTransacoesAltoRisco(): void {
    const sub = this.apiService.getBlockchainHighRiskTransactions(this.limiteRisco).subscribe({
      next: (transacoes) => {
        this.transacoesAltoRisco = Array.isArray(transacoes) ? transacoes : [];
      },
      error: (err) => {
        console.error('Erro ao carregar transações de alto risco:', err);
      }
    });
    this.subscriptions.add(sub);
  }

  buscarPorHash(): void {
    if (!this.hashBusca.trim()) return;
    this.buscandoHash = true;
    this.transacaoBuscada = null;
    this.error = null;

    const sub = this.apiService.getBlockchainTransactionByHash(this.hashBusca.trim()).subscribe({
      next: (transacao) => {
        this.transacaoBuscada = transacao;
        this.buscandoHash = false;
      },
      error: (err) => {
        console.error('Erro ao buscar transação:', err);
        this.error = 'Transação não encontrada com o hash informado.';
        this.buscandoHash = false;
      }
    });
    this.subscriptions.add(sub);
  }

  verificarIntegridade(): void {
    if (!this.hashVerificacao.trim()) return;
    this.verificando = true;
    this.resultadoVerificacao = null;

    const sub = this.apiService.verifyBlockchainIntegrity(this.hashVerificacao.trim()).subscribe({
      next: (resultado) => {
        this.resultadoVerificacao = resultado;
        this.verificando = false;
      },
      error: (err) => {
        console.error('Erro ao verificar integridade:', err);
        this.resultadoVerificacao = { integro: false, mensagem: 'Erro ao verificar integridade da transação.' };
        this.verificando = false;
      }
    });
    this.subscriptions.add(sub);
  }

  selecionarTransacao(tx: any): void {
    this.transacaoSelecionada = this.transacaoSelecionada?.id === tx.id ? null : tx;
  }

  selecionarAba(aba: 'visao-geral' | 'transacoes' | 'alto-risco' | 'integridade'): void {
    this.abaAtiva = aba;
    this.error = null;
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CONFIRMADO':
      case 'CONFIRMADA':
        return 'status-confirmado';
      case 'PENDENTE':
        return 'status-pendente';
      case 'FALHADO':
      case 'FALHADA':
        return 'status-falhado';
      default:
        return 'status-pendente';
    }
  }

  getStatusIcon(status: string): string {
    switch (status?.toUpperCase()) {
      case 'CONFIRMADO':
      case 'CONFIRMADA':
        return 'bi-check-circle-fill';
      case 'PENDENTE':
        return 'bi-clock-fill';
      case 'FALHADO':
      case 'FALHADA':
        return 'bi-x-circle-fill';
      default:
        return 'bi-question-circle-fill';
    }
  }

  getRiscoClass(score: number): string {
    if (score >= 0.7) return 'risco-alto';
    if (score >= 0.4) return 'risco-medio';
    return 'risco-baixo';
  }

  getRiscoLabel(score: number): string {
    if (score >= 0.7) return 'ALTO';
    if (score >= 0.4) return 'MEDIO';
    return 'BAIXO';
  }

  getDecisaoClass(decisao: string): string {
    switch (decisao?.toUpperCase()) {
      case 'PERMITIDO':
      case 'APROVADA':
        return 'decisao-permitido';
      case 'NEGADO':
      case 'NEGADA':
        return 'decisao-negado';
      case 'REQUER_MFA':
        return 'decisao-mfa';
      default:
        return 'decisao-pendente';
    }
  }

  truncarHash(hash: string): string {
    if (!hash || hash.length <= 16) return hash || '';
    return hash.substring(0, 8) + '...' + hash.substring(hash.length - 8);
  }

  copiarHash(hash: string): void {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(hash);
    }
  }

  formatarScore(valor: number): string {
    if (!valor && valor !== 0) return '0.000';
    return valor.toFixed(3);
  }

  getUsuarioSelecionadoNome(): string {
    const u = this.usuarios.find((u: any) => u.id === this.selectedUserId);
    return u?.nome || 'Usuário';
  }

  getDescricaoBlockchain(): string {
    if (!this.estatisticas) return '';

    let desc = 'A blockchain do sistema registra de forma imutável todos os eventos de autenticação. ';

    const total = this.estatisticas.totalTransacoes || 0;
    const confirmadas = this.estatisticas.transacoesConfirmadas || 0;
    const pendentes = this.estatisticas.transacoesPendentes || 0;

    desc += `Atualmente existem ${total} transações registradas, `;
    desc += `sendo ${confirmadas} confirmadas e ${pendentes} pendentes de confirmação. `;

    if (this.estatisticas.integridadeValida !== undefined) {
      if (this.estatisticas.integridadeValida) {
        desc += 'A integridade da cadeia de blocos está validada e consistente. ';
      } else {
        desc += 'ATENÇÃO: Foram detectadas inconsistências na integridade da cadeia de blocos. ';
      }
    }

    desc += 'Cada transação contém o hash SHA-256 dos dados do evento, garantindo que qualquer alteração seja imediatamente detectável.';

    return desc;
  }

  // Métodos de paginação
  get transacoesUsuarioPaginadas(): any[] {
    const inicio = (this.paginaUsuario - 1) * this.tamanhoPaginaUsuario;
    return this.transacoesUsuario.slice(inicio, inicio + this.tamanhoPaginaUsuario);
  }

  get totalPaginasUsuario(): number {
    return Math.ceil(this.transacoesUsuario.length / this.tamanhoPaginaUsuario);
  }

  get transacoesAltoRiscoPaginadas(): any[] {
    const inicio = (this.paginaAltoRisco - 1) * this.tamanhoPaginaAltoRisco;
    return this.transacoesAltoRisco.slice(inicio, inicio + this.tamanhoPaginaAltoRisco);
  }

  get totalPaginasAltoRisco(): number {
    return Math.ceil(this.transacoesAltoRisco.length / this.tamanhoPaginaAltoRisco);
  }

  getPaginas(total: number): number[] {
    const paginas: number[] = [];
    for (let i = 1; i <= total; i++) {
      paginas.push(i);
    }
    return paginas;
  }

  mudarPaginaUsuario(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginasUsuario) {
      this.paginaUsuario = pagina;
    }
  }

  mudarPaginaAltoRisco(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginasAltoRisco) {
      this.paginaAltoRisco = pagina;
    }
  }

  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
