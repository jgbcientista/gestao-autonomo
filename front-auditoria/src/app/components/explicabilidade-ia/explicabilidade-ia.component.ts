import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-explicabilidade-ia',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './explicabilidade-ia.component.html',
  styleUrl: './explicabilidade-ia.component.scss'
})
export class ExplicabilidadeIAComponent implements OnInit {
  Math = Math;
  loading = false;
  error: string | null = null;

  usuarioAtual: any = null;
  usuarios: any[] = [];
  selectedUserId: number | null = null;

  explicabilidade: any = null;
  comparacao: any = null;

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
      this.explicabilidade = null;
      this.comparacao = null;
      return;
    }

    this.carregarDados();
  }

  carregarDados(): void {
    if (!this.selectedUserId) return;

    this.loading = true;
    this.error = null;

    const userId = this.selectedUserId;

    this.apiService.getExplicabilidade(userId).subscribe({
      next: (data) => {
        this.explicabilidade = data;
        this.loading = false;

        // Carregar comparacao
        this.apiService.getExplicabilidadeComparacao(userId).subscribe({
          next: (comp) => {
            this.comparacao = comp;
          },
          error: (err) => {
            console.error('Erro ao carregar comparacao:', err);
          }
        });
      },
      error: (err) => {
        console.error('Erro ao carregar explicabilidade:', err);
        this.error = 'Erro ao carregar dados de explicabilidade. Verifique sua conexão com o servidor.';
        this.explicabilidade = null;
        this.loading = false;
      }
    });
  }

  getClassificacaoCor(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESPERADO': return 'success';
      case 'SUSPEITO': return 'warning';
      case 'ANOMALO': return 'orange';
      case 'ALTAMENTE_SUSPEITO': return 'danger';
      default: return 'secondary';
    }
  }

  getClassificacaoBackground(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESPERADO': return '#E8F5E9';
      case 'SUSPEITO': return '#FFF8E1';
      case 'ANOMALO': return '#FFF3E0';
      case 'ALTAMENTE_SUSPEITO': return '#FDE8E8';
      default: return '#F8F8F8';
    }
  }

  getClassificacaoTextColor(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESPERADO': return '#168821';
      case 'SUSPEITO': return '#B8860B';
      case 'ANOMALO': return '#E65100';
      case 'ALTAMENTE_SUSPEITO': return '#E52207';
      default: return '#555555';
    }
  }

  getFatorCor(contribuicao: number): string {
    if (contribuicao < 0.3) return '#168821';
    if (contribuicao < 0.6) return '#B8860B';
    return '#E52207';
  }

  getFatorBarWidth(contribuicao: number): number {
    return Math.min(contribuicao * 100, 100);
  }

  getModeloScoreCor(score: number): string {
    if (score < 0.3) return 'success';
    if (score < 0.7) return 'warning';
    return 'danger';
  }

  getArrowIcon(atual: number, habitual: number): string {
    if (atual > habitual * 1.1) return 'bi-arrow-up';
    if (atual < habitual * 0.9) return 'bi-arrow-down';
    return 'bi-dash';
  }

  getArrowColor(atual: number, habitual: number): string {
    if (atual > habitual * 1.1) return '#E52207';
    if (atual < habitual * 0.9) return '#168821';
    return '#555555';
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
