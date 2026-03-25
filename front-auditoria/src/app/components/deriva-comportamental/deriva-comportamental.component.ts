import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-deriva-comportamental',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './deriva-comportamental.component.html',
  styleUrl: './deriva-comportamental.component.scss'
})
export class DerivaComportamentalComponent implements OnInit {
  Math = Math;
  loading = false;
  error: string | null = null;

  usuarioAtual: any = null;
  usuarios: any[] = [];
  selectedUserId: number | null = null;

  deriva: any = null;
  historico: any[] = [];
  ajustandoBaseline = false;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

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
  }

  loadUsuarios(): void {
    this.apiService.getUsuariosCadastrados().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
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
        this.error = 'Erro ao carregar lista de usuarios.';
      }
    });
  }

  onUsuarioSelecionado(): void {
    if (!this.selectedUserId) {
      this.deriva = null;
      this.historico = [];
      return;
    }

    this.carregarDados();
  }

  carregarDados(): void {
    if (!this.selectedUserId) return;

    this.loading = true;
    this.error = null;

    const userId = this.selectedUserId;

    this.apiService.getDerivaComportamental(userId).subscribe({
      next: (data) => {
        this.deriva = data;
        this.loading = false;

        // Carregar historico
        this.apiService.getDerivaHistorico(userId).subscribe({
          next: (hist) => {
            this.historico = Array.isArray(hist) ? hist : [];
          },
          error: (err) => {
            console.error('Erro ao carregar historico de deriva:', err);
            this.historico = [];
          }
        });
      },
      error: (err) => {
        console.error('Erro ao carregar deriva comportamental:', err);
        this.error = 'Erro ao carregar dados de deriva comportamental. Verifique sua conexão com o servidor.';
        this.deriva = null;
        this.loading = false;
      }
    });
  }

  ajustarBaseline(): void {
    if (!this.selectedUserId || this.ajustandoBaseline) return;

    this.ajustandoBaseline = true;

    this.apiService.ajustarBaselineDrift(this.selectedUserId).subscribe({
      next: () => {
        this.ajustandoBaseline = false;
        this.carregarDados();
      },
      error: (err) => {
        console.error('Erro ao ajustar baseline:', err);
        this.error = 'Erro ao ajustar baseline de deriva.';
        this.ajustandoBaseline = false;
      }
    });
  }

  getClassificacaoCor(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESTAVEL': return 'success';
      case 'EVOLUCAO_NATURAL': return 'warning';
      case 'RUPTURA_COMPORTAMENTAL': return 'danger';
      default: return 'secondary';
    }
  }

  getClassificacaoBackground(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESTAVEL': return '#E8F5E9';
      case 'EVOLUCAO_NATURAL': return '#FFF8E1';
      case 'RUPTURA_COMPORTAMENTAL': return '#FDE8E8';
      default: return '#F8F8F8';
    }
  }

  getClassificacaoTextColor(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESTAVEL': return '#168821';
      case 'EVOLUCAO_NATURAL': return '#B8860B';
      case 'RUPTURA_COMPORTAMENTAL': return '#E52207';
      default: return '#555555';
    }
  }

  getClassificacaoLabel(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESTAVEL': return 'Estavel';
      case 'EVOLUCAO_NATURAL': return 'Evolucao Natural';
      case 'RUPTURA_COMPORTAMENTAL': return 'Ruptura Comportamental';
      default: return classificacao || 'N/A';
    }
  }

  getTimelineLineColor(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESTAVEL': return '#168821';
      case 'EVOLUCAO_NATURAL': return '#B8860B';
      case 'RUPTURA_COMPORTAMENTAL': return '#E52207';
      default: return '#CCCCCC';
    }
  }

  formatarScore(valor: number): string {
    if (!valor && valor !== 0) return '0.000';
    return valor.toFixed(3);
  }

  formatarPercentual(valor: number): string {
    if (!valor && valor !== 0) return '0%';
    return `${Math.round(valor * 100)}%`;
  }

  getUsuarioSelecionadoNome(): string {
    const u = this.usuarios.find((u: any) => u.id === this.selectedUserId);
    return u?.nome || 'Usuario';
  }

  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
