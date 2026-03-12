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

  trustScore: TrustScore | null = null;
  statistics: any = null;
  usersByLevel: any = null;
  loading = false;
  error: string | null = null;

  // Usuário atual para menu
  usuarioAtual: any = null;

  // Ajuste manual
  adjustmentValue = 0;
  adjustmentReason = '';

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.usuarioAtual = this.authService.getCurrentUser();

    if (!this.usuarioAtual) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadTrustScoreData();
  }

  refreshData(): void {
    this.loadTrustScoreData();
  }

  private loadTrustScoreData(): void {
    this.loading = true;
    this.error = null;

    if (!this.usuarioAtual?.email) {
      this.error = 'Usuario nao autenticado';
      this.loading = false;
      return;
    }

    // Carrega score do usuário atual
    this.apiService.getTrustScore(this.usuarioAtual.email).subscribe({
      next: (score) => {
        this.trustScore = score;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar score:', err);
        this.error = 'Erro ao carregar score de confianca. Verifique sua conexao com o servidor.';
        this.loading = false;
      }
    });

    // Carrega estatísticas gerais
    this.apiService.getTrustScoreStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (err) => {
        console.error('Erro ao carregar estatisticas:', err);
        this.error = 'Erro ao carregar estatisticas. Verifique sua conexao com o servidor.';
      }
    });
  }

  adjustScore(): void {
    if (!this.usuarioAtual?.email || !this.adjustmentReason) {
      return;
    }

    this.loading = true;

    this.apiService.adjustTrustScore(
      this.usuarioAtual.email,
      this.adjustmentValue,
      this.adjustmentReason
    ).subscribe({
      next: (result) => {
        this.loadTrustScoreData();
        this.adjustmentValue = 0;
        this.adjustmentReason = '';
      },
      error: (err) => {
        console.error('Erro ao ajustar score:', err);
        this.error = 'Erro ao ajustar score. Verifique sua conexao com o servidor.';
        this.loading = false;
      }
    });
  }

  loadUsersByLevel(level: string): void {
    this.apiService.getUsersByTrustLevel(level).subscribe({
      next: (users) => {
        this.usersByLevel = users;
      },
      error: (err) => {
        console.error('Erro ao carregar usuarios:', err);
        this.error = 'Erro ao carregar usuarios. Verifique sua conexao com o servidor.';
      }
    });
  }

  getScoreColor(score: number): string {
    if (score >= 0.8) return 'text-green-600';
    if (score >= 0.6) return 'text-yellow-600';
    if (score >= 0.4) return 'text-orange-600';
    return 'text-red-600';
  }

  getScoreIcon(score: number): string {
    if (score >= 0.8) return '🟢';
    if (score >= 0.6) return '🟡';
    if (score >= 0.4) return '🟠';
    return '🔴';
  }

  getLevelColor(level: string): string {
    switch (level) {
      case 'MUITO_ALTO': return 'text-green-600';
      case 'ALTO': return 'text-green-500';
      case 'MEDIO': return 'text-yellow-600';
      case 'BAIXO': return 'text-orange-600';
      case 'MUITO_BAIXO': return 'text-red-600';
      default: return 'text-gray-600';
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
