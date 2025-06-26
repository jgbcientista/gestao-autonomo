import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
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
  modoSimulado = false;
  
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
    console.log('👤 Usuário atual:', this.usuarioAtual);
    
    if (!this.usuarioAtual) {
      console.warn('⚠️ Usuário não autenticado');
      this.router.navigate(['/login']);
      return;
    }

    if (this.usuarioAtual.role !== 'ADMIN') {
      console.warn('⚠️ Usuário não é ADMIN:', this.usuarioAtual.role);
      this.modoSimulado = true;
    } else {
      console.log('✅ Usuário ADMIN confirmado');
      this.modoSimulado = false;
    }

    this.loadTrustScoreData();
  }

  refreshData(): void {
    console.log('🔄 Atualizando dados...');
    this.loadTrustScoreData();
  }

  private loadTrustScoreData(): void {
    this.loading = true;
    this.error = null;

    if (!this.usuarioAtual?.email) {
      this.error = 'Usuário não autenticado';
      this.loading = false;
      return;
    }

    console.log('📊 Carregando dados para:', this.usuarioAtual.email);
    console.log('🔒 Modo simulado:', this.modoSimulado);

    // Carrega score do usuário atual
    this.apiService.getTrustScore(this.usuarioAtual.email).subscribe({
      next: (score) => {
        console.log('✅ Score recebido:', score);
        this.trustScore = score;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erro ao carregar score:', err);
        this.error = 'Erro ao carregar score de confiança: ' + (err.message || err);
        this.loading = false;
      }
    });

    // Carrega estatísticas gerais
    this.apiService.getTrustScoreStatistics().subscribe({
      next: (stats) => {
        console.log('📈 Estatísticas recebidas:', stats);
        this.statistics = stats;
      },
      error: (err) => {
        console.error('❌ Erro ao carregar estatísticas:', err);
        this.error = 'Erro ao carregar estatísticas: ' + (err.message || err);
      }
    });
  }

  adjustScore(): void {
    if (!this.usuarioAtual?.email || !this.adjustmentReason) {
      console.warn('⚠️ Dados inválidos para ajuste');
      return;
    }

    if (this.modoSimulado) {
      console.warn('⚠️ Ajuste não permitido em modo simulado');
      this.error = 'Ajuste não permitido em modo simulado';
      return;
    }

    console.log('⚖️ Iniciando ajuste de score:', {
      email: this.usuarioAtual.email,
      valor: this.adjustmentValue,
      motivo: this.adjustmentReason
    });

    this.loading = true;
    
    this.apiService.adjustTrustScore(
      this.usuarioAtual.email, 
      this.adjustmentValue, 
      this.adjustmentReason
    ).subscribe({
      next: (result) => {
        console.log('✅ Score ajustado:', result);
        this.loadTrustScoreData();
        this.adjustmentValue = 0;
        this.adjustmentReason = '';
      },
      error: (err) => {
        console.error('❌ Erro ao ajustar score:', err);
        this.error = 'Erro ao ajustar score: ' + (err.message || err);
        this.loading = false;
      }
    });
  }

  loadUsersByLevel(level: string): void {
    if (this.modoSimulado) {
      console.warn('⚠️ Carregamento por nível não permitido em modo simulado');
      return;
    }

    console.log('👥 Carregando usuários do nível:', level);
    
    this.apiService.getUsersByTrustLevel(level).subscribe({
      next: (users) => {
        console.log('✅ Usuários recebidos:', users);
        this.usersByLevel = users;
      },
      error: (err) => {
        console.error('❌ Erro ao carregar usuários:', err);
        this.error = 'Erro ao carregar usuários: ' + (err.message || err);
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
