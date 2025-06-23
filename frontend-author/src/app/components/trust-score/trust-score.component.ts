import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { TrustScore } from '../../models/system.model';

@Component({
  selector: 'app-trust-score',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trust-score.component.html',
  styleUrls: ['./trust-score.component.scss']
})
export class TrustScoreComponent implements OnInit {
  
  trustScore: TrustScore | null = null;
  statistics: any = null;
  usersByLevel: any = null;
  loading = false;
  error: string | null = null;
  
  // Ajuste manual
  adjustmentValue = 0;
  adjustmentReason = '';

  constructor(
    private apiService: ApiService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadTrustScoreData();
  }

  refreshData(): void {
    this.loadTrustScoreData();
  }

  private loadTrustScoreData(): void {
    this.loading = true;
    this.error = null;

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.email) {
      this.error = 'Usuário não autenticado';
      this.loading = false;
      return;
    }

    // Carrega score do usuário atual
    this.apiService.getTrustScore(currentUser.email).subscribe({
      next: (score) => {
        this.trustScore = score;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar score de confiança';
        this.loading = false;
        console.error('Erro:', err);
      }
    });

    // Carrega estatísticas gerais
    this.apiService.getTrustScoreStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (err) => {
        console.error('Erro ao carregar estatísticas:', err);
      }
    });
  }

  adjustScore(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.email || !this.adjustmentReason) {
      return;
    }

    this.loading = true;
    
    this.apiService.adjustTrustScore(
      currentUser.email, 
      this.adjustmentValue, 
      this.adjustmentReason
    ).subscribe({
      next: (result) => {
        console.log('Score ajustado:', result);
        this.loadTrustScoreData();
        this.adjustmentValue = 0;
        this.adjustmentReason = '';
      },
      error: (err) => {
        this.error = 'Erro ao ajustar score';
        this.loading = false;
        console.error('Erro:', err);
      }
    });
  }

  loadUsersByLevel(level: string): void {
    this.apiService.getUsersByTrustLevel(level).subscribe({
      next: (users) => {
        this.usersByLevel = users;
      },
      error: (err) => {
        console.error('Erro ao carregar usuários:', err);
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
}
