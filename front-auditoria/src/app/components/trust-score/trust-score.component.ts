import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { TrustScore } from '../../models/system.model';

@Component({
  selector: 'app-trust-score',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './trust-score.component.html',
  styleUrls: ['./trust-score.component.scss']
})
export class TrustScoreComponent implements OnInit {

  Math = Math;
  trustScore: TrustScore | null = null;
  statistics: any = null;
  loading = false;
  error: string | null = null;

  // Usuario atual para menu
  usuarioAtual: any = null;

  // Lista de usuarios para o combo
  usuarios: any[] = [];
  selectedEmail: string = '';

  // Ajuste manual
  adjustmentValue = 0;
  adjustmentReason = '';

  // Decisao simulada
  decisaoSimulada: any = null;

  // Historico de localizacoes
  locationHistory: any[] = [];
  locationPage = 1;
  locationPageSize = 8;
  loadingLocations = false;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) { }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  ngOnInit(): void {
    this.usuarioAtual = this.authService.getCurrentUser();

    if (!this.usuarioAtual) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUsuarios();
    this.loadStatistics();
  }

  loadUsuarios(): void {
    this.apiService.getUsuariosCadastrados().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        // Seleciona o usuário logado por padrão
        if (this.usuarioAtual?.email) {
          this.selectedEmail = this.usuarioAtual.email;
          this.onUsuarioSelecionado();
        }
      },
      error: (err) => {
        console.error('Erro ao carregar usuários:', err);
        this.error = 'Erro ao carregar lista de usuários.';
      }
    });
  }

  onUsuarioSelecionado(): void {
    if (!this.selectedEmail) {
      this.trustScore = null;
      this.decisaoSimulada = null;
      this.locationHistory = [];
      return;
    }
    this.loadTrustScoreData();
    this.loadDecisaoSimulada();
    this.loadLocationHistory();
  }

  refreshData(): void {
    if (this.selectedEmail) {
      this.loadTrustScoreData();
      this.loadDecisaoSimulada();
    }
    this.loadStatistics();
  }

  private loadTrustScoreData(): void {
    this.loading = true;
    this.error = null;

    this.apiService.getTrustScore(this.selectedEmail).subscribe({
      next: (score) => {
        this.trustScore = score;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar score:', err);
        this.error = 'Erro ao carregar score de confiança.';
        this.loading = false;
      }
    });
  }

  private loadStatistics(): void {
    this.apiService.getTrustScoreStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (err) => {
        console.error('Erro ao carregar estatísticas:', err);
      }
    });
  }

  private loadDecisaoSimulada(): void {
    this.apiService.simulateAuthDecision(this.selectedEmail).subscribe({
      next: (decisao) => {
        this.decisaoSimulada = decisao;
      },
      error: (err) => {
        console.error('Erro ao simular decisao:', err);
      }
    });
  }

  adjustScore(): void {
    if (!this.selectedEmail || !this.adjustmentReason) {
      return;
    }

    this.loading = true;

    this.apiService.adjustTrustScore(
      this.selectedEmail,
      this.adjustmentValue,
      this.adjustmentReason
    ).subscribe({
      next: (result) => {
        this.loadTrustScoreData();
        this.loadDecisaoSimulada();
        this.adjustmentValue = 0;
        this.adjustmentReason = '';
      },
      error: (err) => {
        console.error('Erro ao ajustar score:', err);
        this.error = 'Erro ao ajustar score.';
        this.loading = false;
      }
    });
  }

  private loadLocationHistory(): void {
    const usuario = this.usuarios.find(u => u.email === this.selectedEmail);
    if (!usuario?.id) return;

    this.loadingLocations = true;
    this.locationPage = 1;

    this.apiService.getUserLocationHistory(usuario.id).subscribe({
      next: (data) => {
        this.locationHistory = Array.isArray(data) ? data : [];
        this.loadingLocations = false;
      },
      error: (err) => {
        console.error('Erro ao carregar histórico de localizações:', err);
        this.locationHistory = [];
        this.loadingLocations = false;
      }
    });
  }

  get locationPagedData(): any[] {
    const start = (this.locationPage - 1) * this.locationPageSize;
    return this.locationHistory.slice(start, start + this.locationPageSize);
  }

  get locationTotalPages(): number {
    return Math.ceil(this.locationHistory.length / this.locationPageSize);
  }

  get locationPages(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.locationTotalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToLocationPage(page: number): void {
    if (page >= 1 && page <= this.locationTotalPages) {
      this.locationPage = page;
    }
  }

  getScoreColor(score: number): string {
    if (score >= 0.7) return '#4ade80';
    if (score >= 0.5) return '#fbbf24';
    if (score >= 0.2) return '#fb923c';
    return '#f87171';
  }

  getDecisaoLabel(decisao: string): string {
    switch (decisao) {
      case 'PERMITIR': return 'Acesso Permitido';
      case 'EXIGIR_MFA': return 'Exige Autenticação MFA';
      case 'BLOQUEAR': return 'Acesso Bloqueado';
      case 'PERMITIR_COM_MONITORAMENTO': return 'Permitido com Monitoramento';
      default: return decisao;
    }
  }

  getDecisaoIcon(decisao: string): string {
    switch (decisao) {
      case 'PERMITIR': return '✅';
      case 'EXIGIR_MFA': return '🔐';
      case 'BLOQUEAR': return '🚫';
      case 'PERMITIR_COM_MONITORAMENTO': return '👁️';
      default: return '❓';
    }
  }

  getDescricaoContextual(): string {
    if (!this.trustScore) return '';

    const score = this.trustScore.scoreAtual;
    const nivel = this.trustScore.nivelConfianca;
    const stats = this.trustScore.estatisticas;
    const motivo = this.trustScore.motivoAlteracao || '';
    const usuario = this.usuarios.find(u => u.email === this.selectedEmail);
    const nome = usuario?.nome || this.selectedEmail;

    // Parse motivo to extract component scores
    let historico = 0, ia = 0, recente = 0, externos = 0;
    const matchHist = motivo.match(/Histórico:\s*([\d.]+)/);
    const matchIA = motivo.match(/IA:\s*([\d.]+)/);
    const matchRecente = motivo.match(/Recente:\s*([\d.]+)/);
    const matchExternos = motivo.match(/Externos:\s*([\d.]+)/);
    if (matchHist) historico = parseFloat(matchHist[1]);
    if (matchIA) ia = parseFloat(matchIA[1]);
    if (matchRecente) recente = parseFloat(matchRecente[1]);
    if (matchExternos) externos = parseFloat(matchExternos[1]);

    let desc = '';

    // Score level description
    if (score >= 0.8) {
      desc += `${nome} possui score de confiança muito alto (${score.toFixed(2)})`;
    } else if (score >= 0.7) {
      desc += `${nome} possui score de confiança alto (${score.toFixed(2)})`;
    } else if (score >= 0.5) {
      desc += `${nome} possui score de confiança médio (${score.toFixed(2)})`;
    } else if (score >= 0.2) {
      desc += `${nome} possui score de confiança baixo (${score.toFixed(2)})`;
    } else {
      desc += `${nome} possui score de confiança muito baixo (${score.toFixed(2)})`;
    }

    // Login history
    const totalLogins = (stats?.totalLoginsSucesso || 0) + (stats?.totalLoginsSuspeitos || 0);
    if (totalLogins > 0) {
      const taxaSucesso = ((stats.totalLoginsSucesso / totalLogins) * 100).toFixed(0);
      desc += `. O histórico mostra ${stats.totalLoginsSucesso} logins bem-sucedidos de ${totalLogins} tentativas (${taxaSucesso}% de sucesso)`;
      if (stats.totalLoginsSuspeitos > 0) {
        desc += `, com ${stats.totalLoginsSuspeitos} acessos suspeitos detectados`;
      }
    }

    // IA analysis
    if (matchIA) {
      if (ia < 0.3) {
        desc += `. A análise de IA em tempo real detectou alto nível de anomalia — o usuário acessou de muitas localizações geográficas distintas, incluindo países diferentes, o que eleva o score de anomalia`;
      } else if (ia < 0.6) {
        desc += `. A análise de IA detectou padrões moderadamente incomuns nos acessos`;
      } else {
        desc += `. A análise de IA considera os padrões de acesso consistentes`;
      }
    }

    // Two-layer explanation
    if (score >= 0.7 && ia < 0.3) {
      desc += `. Isso demonstra as duas camadas complementares do sistema: o score histórico indica confiança baseada no volume de logins legítimos, enquanto o motor de IA analisa padrões comportamentais em tempo real e pode classificar o acesso como suspeito independentemente`;
    }

    // Manual adjustment
    const fatorAjuste = this.trustScore.fatorAjuste || 0;
    if (fatorAjuste !== 0) {
      const direcao = fatorAjuste > 0 ? 'positivo' : 'negativo';
      const efeito = fatorAjuste > 0 ? 'elevando' : 'reduzindo';
      desc += `. Foi aplicado um ajuste manual ${direcao} de ${fatorAjuste.toFixed(2)}, ${efeito} o score final — isso indica intervenção administrativa no sistema`;
    }

    // Decision
    if (this.decisaoSimulada) {
      const decisao = this.decisaoSimulada.decisaoSimulada;
      if (decisao === 'PERMITIR') {
        desc += `. Com este score, o sistema permite o acesso automaticamente.`;
      } else if (decisao === 'EXIGIR_MFA') {
        desc += `. Com este score, o sistema exige autenticação multi-fator (MFA) antes de permitir o acesso.`;
      } else if (decisao === 'BLOQUEAR') {
        desc += `. Com este score, o sistema bloqueia o acesso automaticamente por segurança.`;
      } else if (decisao === 'PERMITIR_COM_MONITORAMENTO') {
        desc += `. Com este score, o sistema permite o acesso mas mantém monitoramento ativo.`;
      }
    } else {
      desc += '.';
    }

    return desc;
  }

  getDecisaoColor(decisao: string): string {
    switch (decisao) {
      case 'PERMITIR': return '#4ade80';
      case 'EXIGIR_MFA': return '#fbbf24';
      case 'BLOQUEAR': return '#f87171';
      case 'PERMITIR_COM_MONITORAMENTO': return '#60a5fa';
      default: return '#94a3b8';
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
