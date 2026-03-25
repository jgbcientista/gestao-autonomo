import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-keystroke-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './keystroke-profile.component.html',
  styleUrl: './keystroke-profile.component.scss'
})
export class KeystrokeProfileComponent implements OnInit {
  Math = Math;
  loading = false;
  error: string | null = null;

  usuarioAtual: any = null;
  usuarios: any[] = [];
  selectedUserId: number | null = null;

  perfil: any = null;
  atualizandoBaseline = false;

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
      this.perfil = null;
      return;
    }

    this.carregarPerfil();
  }

  carregarPerfil(): void {
    if (!this.selectedUserId) return;

    this.loading = true;
    this.error = null;

    this.apiService.getKeystrokeProfile(this.selectedUserId).subscribe({
      next: (data) => {
        this.perfil = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar perfil keystroke:', err);
        this.error = 'Erro ao carregar perfil de digitacao. Verifique sua conexao com o servidor.';
        this.perfil = null;
        this.loading = false;
      }
    });
  }

  atualizarBaseline(): void {
    if (!this.selectedUserId || this.atualizandoBaseline) return;

    this.atualizandoBaseline = true;

    this.apiService.atualizarBaselineKeystroke(this.selectedUserId).subscribe({
      next: () => {
        this.atualizandoBaseline = false;
        this.carregarPerfil();
      },
      error: (err) => {
        console.error('Erro ao atualizar baseline:', err);
        this.error = 'Erro ao atualizar baseline de digitacao.';
        this.atualizandoBaseline = false;
      }
    });
  }

  getScoreSimilaridadeCor(score: number): string {
    if (score > 0.8) return 'success';
    if (score > 0.5) return 'warning';
    return 'danger';
  }

  getScoreSimilaridadeLabel(score: number): string {
    if (score > 0.8) return 'Alta Similaridade';
    if (score > 0.5) return 'Similaridade Moderada';
    return 'Baixa Similaridade';
  }

  formatarMs(valor: number): string {
    if (!valor && valor !== 0) return '0 ms';
    return `${Math.round(valor)} ms`;
  }

  formatarScore(valor: number): string {
    if (!valor && valor !== 0) return '0.000';
    return valor.toFixed(3);
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
