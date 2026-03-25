import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-gestao-acesso',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './gestao-acesso.component.html',
  styleUrl: './gestao-acesso.component.scss'
})
export class GestaoAcessoComponent implements OnInit {
  pendentes: any[] = [];
  todosUsuarios: any[] = [];
  isLoading = true;
  mensagem = '';
  mensagemTipo = '';
  abaAtiva = 'pendentes';
  currentUser: any;

  constructor(
    private apiService: ApiService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.carregarPendentes();
    this.carregarTodos();
  }

  carregarPendentes(): void {
    this.apiService.getUsuariosPendentes().subscribe({
      next: (data) => {
        this.pendentes = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar pendentes:', err);
        this.isLoading = false;
      }
    });
  }

  carregarTodos(): void {
    this.apiService.getTodosUsuarios().subscribe({
      next: (data) => {
        this.todosUsuarios = data;
      },
      error: (err) => {
        console.error('Erro ao carregar usuarios:', err);
      }
    });
  }

  aprovar(usuarioId: number): void {
    this.apiService.aprovarUsuario(usuarioId).subscribe({
      next: (res) => {
        this.mensagem = res.mensagem || 'Usuario aprovado com sucesso!';
        this.mensagemTipo = 'success';
        this.carregarPendentes();
        this.carregarTodos();
        setTimeout(() => this.mensagem = '', 4000);
      },
      error: (err) => {
        this.mensagem = 'Erro ao aprovar usuario.';
        this.mensagemTipo = 'danger';
        setTimeout(() => this.mensagem = '', 4000);
      }
    });
  }

  rejeitar(usuarioId: number): void {
    this.apiService.rejeitarUsuario(usuarioId).subscribe({
      next: (res) => {
        this.mensagem = res.mensagem || 'Usuario rejeitado.';
        this.mensagemTipo = 'warning';
        this.carregarPendentes();
        this.carregarTodos();
        setTimeout(() => this.mensagem = '', 4000);
      },
      error: (err) => {
        this.mensagem = 'Erro ao rejeitar usuario.';
        this.mensagemTipo = 'danger';
        setTimeout(() => this.mensagem = '', 4000);
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ATIVO': return 'badge-success';
      case 'PENDENTE_APROVACAO_GESTOR': return 'badge-warning';
      case 'REJEITADO': return 'badge-danger';
      case 'BLOQUEADO': return 'badge-dark';
      default: return 'badge-secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ATIVO': return 'Ativo';
      case 'PENDENTE_APROVACAO_GESTOR': return 'Pendente';
      case 'REJEITADO': return 'Rejeitado';
      case 'BLOQUEADO': return 'Bloqueado';
      default: return status;
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
