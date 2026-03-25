import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-gestao-acesso',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
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

  // Modal editar
  modalEditarAberto = false;
  usuarioEditando: any = null;
  editNome = '';
  editEmail = '';
  editStatus = '';

  // Modal perfil
  modalPerfilAberto = false;
  usuarioAlterandoPerfil: any = null;
  novoPerfil = '';

  // Modal excluir
  modalExcluirAberto = false;
  usuarioExcluindo: any = null;

  perfisDisponiveis = ['USUARIO_PADRAO', 'ADMIN', 'GESTOR', 'AUDITOR'];

  constructor(
    private apiService: ApiService,
    private authService: AuthService
  ) {}

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

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

  getPerfilLabel(perfis: string): string {
    if (!perfis) return 'N/A';
    const clean = perfis.replace(/[\[\]]/g, '').trim();
    switch (clean) {
      case 'USUARIO_PADRAO': return 'Usuário';
      case 'ADMIN': return 'Administrador';
      case 'GESTOR': return 'Gestor';
      case 'AUDITOR': return 'Auditor';
      default: return clean;
    }
  }

  getPerfilClass(perfis: string): string {
    if (!perfis) return 'badge-dark';
    const clean = perfis.replace(/[\[\]]/g, '').trim();
    switch (clean) {
      case 'ADMIN': return 'badge-danger';
      case 'GESTOR': return 'badge-warning';
      case 'AUDITOR': return 'badge-info';
      default: return 'badge-info';
    }
  }

  // === Editar Usuário ===
  abrirModalEditar(usuario: any): void {
    this.usuarioEditando = usuario;
    this.editNome = usuario.nome;
    this.editEmail = usuario.email;
    this.editStatus = usuario.statusConta;
    this.modalEditarAberto = true;
  }

  fecharModalEditar(): void {
    this.modalEditarAberto = false;
    this.usuarioEditando = null;
  }

  salvarEdicao(): void {
    if (!this.usuarioEditando) return;

    const dados: any = {};
    if (this.editNome !== this.usuarioEditando.nome) dados.nome = this.editNome;
    if (this.editEmail !== this.usuarioEditando.email) dados.email = this.editEmail;
    if (this.editStatus !== this.usuarioEditando.statusConta) dados.statusConta = this.editStatus;

    this.apiService.editarUsuario(this.usuarioEditando.id, dados).subscribe({
      next: () => {
        this.mensagem = 'Usuário editado com sucesso!';
        this.mensagemTipo = 'success';
        this.fecharModalEditar();
        this.carregarTodos();
        this.carregarPendentes();
        setTimeout(() => this.mensagem = '', 4000);
      },
      error: () => {
        this.mensagem = 'Erro ao editar usuário.';
        this.mensagemTipo = 'danger';
        setTimeout(() => this.mensagem = '', 4000);
      }
    });
  }

  // === Alterar Perfil ===
  abrirModalPerfil(usuario: any): void {
    this.usuarioAlterandoPerfil = usuario;
    const clean = (usuario.perfis || '').replace(/[\[\]]/g, '').trim();
    this.novoPerfil = clean || 'USUARIO_PADRAO';
    this.modalPerfilAberto = true;
  }

  fecharModalPerfil(): void {
    this.modalPerfilAberto = false;
    this.usuarioAlterandoPerfil = null;
  }

  salvarPerfil(): void {
    if (!this.usuarioAlterandoPerfil || !this.novoPerfil) return;

    this.apiService.alterarPerfilUsuario(this.usuarioAlterandoPerfil.id, this.novoPerfil).subscribe({
      next: () => {
        this.mensagem = 'Perfil alterado com sucesso!';
        this.mensagemTipo = 'success';
        this.fecharModalPerfil();
        this.carregarTodos();
        setTimeout(() => this.mensagem = '', 4000);
      },
      error: () => {
        this.mensagem = 'Erro ao alterar perfil.';
        this.mensagemTipo = 'danger';
        setTimeout(() => this.mensagem = '', 4000);
      }
    });
  }

  // === Excluir Usuário ===
  abrirModalExcluir(usuario: any): void {
    this.usuarioExcluindo = usuario;
    this.modalExcluirAberto = true;
  }

  fecharModalExcluir(): void {
    this.modalExcluirAberto = false;
    this.usuarioExcluindo = null;
  }

  confirmarExclusao(): void {
    if (!this.usuarioExcluindo) return;

    this.apiService.excluirUsuario(this.usuarioExcluindo.id).subscribe({
      next: () => {
        this.mensagem = 'Usuário excluído com sucesso!';
        this.mensagemTipo = 'success';
        this.fecharModalExcluir();
        this.carregarTodos();
        this.carregarPendentes();
        setTimeout(() => this.mensagem = '', 4000);
      },
      error: () => {
        this.mensagem = 'Erro ao excluir usuário.';
        this.mensagemTipo = 'danger';
        setTimeout(() => this.mensagem = '', 4000);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
