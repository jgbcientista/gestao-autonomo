import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface Pais {
  codigo: string;
  nome: string;
  localizacao: string;
  totalIps: string;
  selecionado: boolean;
}

interface UsuarioItem {
  id: string;
  email: string;
  nome: string;
}

interface RegistroGerado {
  pais: string;
  ip: string;
  localizacao: string;
  aiRiskScore: number;
  trustScore: number;
  blockchain: string;
  classificacaoIA: string;
  tipoEvento: string;
  decisao: string;
  suspeito: boolean;
  tentativasFalhadas: number;
  dataHora: string;
}

@Component({
  selector: 'app-data-generator',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './data-generator.component.html',
  styleUrls: ['./data-generator.component.scss']
})
export class DataGeneratorComponent implements OnInit {
  usuarioAtual: any = null;
  paises: Pais[] = [];
  usuarios: UsuarioItem[] = [];
  usuarioSelecionado: string = '';
  registrosPorPais: number = 5;
  tipoRegistro: string = 'MISTO';
  gerando: boolean = false;
  progresso: number = 0;
  mensagemProgresso: string = '';

  resultado: any = null;
  registrosGerados: RegistroGerado[] = [];
  erroMensagem: string = '';

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
    this.carregarDados();
  }

  carregarDados(): void {
    this.apiService.getPaisesDisponiveis().subscribe({
      next: (paises) => {
        this.paises = paises.map(p => ({ ...p, selecionado: false }));
      },
      error: (err) => {
        console.error('Erro ao carregar paises:', err);
        this.erroMensagem = 'Erro ao carregar lista de países';
      }
    });

    this.apiService.getUsuariosParaGeracao().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        if (this.usuarioAtual?.email) {
          this.usuarioSelecionado = this.usuarioAtual.email;
        }
      },
      error: (err) => {
        console.error('Erro ao carregar usuários:', err);
        this.erroMensagem = 'Erro ao carregar lista de usuários';
      }
    });
  }

  get paisesSelecionados(): Pais[] {
    return this.paises.filter(p => p.selecionado);
  }

  get totalRegistrosEstimado(): number {
    return this.paisesSelecionados.length * this.registrosPorPais;
  }

  togglePais(pais: Pais): void {
    pais.selecionado = !pais.selecionado;
  }

  get todosSelecionados(): boolean {
    return this.paises.length > 0 && this.paises.every(p => p.selecionado);
  }

  selecionarTodos(): void {
    const novoValor = !this.todosSelecionados;
    this.paises.forEach(p => p.selecionado = novoValor);
  }

  isRegistrado(status: string): boolean {
    return status === 'registrado';
  }

  gerarRegistros(): void {
    if (!this.usuarioSelecionado) {
      this.erroMensagem = 'Selecione um usuário';
      return;
    }
    if (this.paisesSelecionados.length === 0) {
      this.erroMensagem = 'Selecione pelo menos um país';
      return;
    }

    this.erroMensagem = '';
    this.gerando = true;
    this.progresso = 10;
    this.mensagemProgresso = 'Iniciando geração de registros...';
    this.resultado = null;
    this.registrosGerados = [];

    const codigosPaises = this.paisesSelecionados.map(p => p.codigo);

    // Simular progresso visual
    const intervalo = setInterval(() => {
      if (this.progresso < 90) {
        this.progresso += 5;
        const etapas = [
          'Analisando comportamento com IA...',
          'Calculando scores de confiança...',
          'Registrando logs de auditoria...',
          'Gerando hashes blockchain...',
          'Atualizando perfis comportamentais...',
          'Processando registros...'
        ];
        this.mensagemProgresso = etapas[Math.floor(Math.random() * etapas.length)];
      }
    }, 800);

    this.apiService.gerarRegistrosAcesso(this.usuarioSelecionado, codigosPaises, this.registrosPorPais, this.tipoRegistro).subscribe({
      next: (resultado) => {
        clearInterval(intervalo);
        this.progresso = 100;
        this.mensagemProgresso = 'Geração concluída com sucesso!';
        this.resultado = resultado;
        this.registrosGerados = resultado.registros || [];

        setTimeout(() => {
          this.gerando = false;
        }, 1000);
      },
      error: (err) => {
        clearInterval(intervalo);
        this.gerando = false;
        this.progresso = 0;
        console.error('Erro ao gerar registros:', err);
        this.erroMensagem = 'Erro ao gerar registros: ' + (err.error?.erro || err.message || 'Erro desconhecido');
      }
    });
  }

  getClassificacaoClass(classificacao: string): string {
    if (!classificacao) return 'badge-secondary';
    switch (classificacao.toUpperCase()) {
      case 'ESPERADO': return 'badge-success';
      case 'SUSPEITO': return 'badge-warning';
      case 'ANOMALO': return 'badge-danger';
      case 'ALTAMENTE_SUSPEITO': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  getRiskClass(score: number): string {
    if (score <= 0.3) return 'risk-low';
    if (score <= 0.7) return 'risk-medium';
    return 'risk-high';
  }

  getDecisaoClass(decisao: string): string {
    if (!decisao) return 'badge-secondary';
    switch (decisao.toUpperCase()) {
      case 'ALLOWED': return 'badge-success';
      case 'REQUIRES_MFA': return 'badge-warning';
      case 'BLOCKED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  getDecisaoLabel(decisao: string): string {
    if (!decisao) return 'N/A';
    switch (decisao.toUpperCase()) {
      case 'ALLOWED': return 'Permitido';
      case 'REQUIRES_MFA': return 'MFA Exigido';
      case 'BLOCKED': return 'Bloqueado';
      default: return decisao;
    }
  }

  getTipoEventoLabel(tipo: string): string {
    if (!tipo) return 'N/A';
    switch (tipo) {
      case 'LOGIN_SUCCESS': return 'Login OK';
      case 'LOGIN_FAILED': return 'Login Falhou';
      case 'LOGIN_BLOCKED': return 'Bloqueado';
      case 'LOGIN_MFA_REQUIRED': return 'MFA Exigido';
      default: return tipo;
    }
  }

  getBandeira(pais: string): string {
    const bandeiras: { [key: string]: string } = {
      'Russia': '\uD83C\uDDF7\uD83C\uDDFA',
      'China': '\uD83C\uDDE8\uD83C\uDDF3',
      'Japao': '\uD83C\uDDEF\uD83C\uDDF5',
      'Coreia do Norte': '\uD83C\uDDF0\uD83C\uDDF5',
      'Estados Unidos': '\uD83C\uDDFA\uD83C\uDDF8',
      'Brasil': '\uD83C\uDDE7\uD83C\uDDF7',
      'Alemanha': '\uD83C\uDDE9\uD83C\uDDEA',
      'Nigeria': '\uD83C\uDDF3\uD83C\uDDEC',
      'India': '\uD83C\uDDEE\uD83C\uDDF3',
      'Ira': '\uD83C\uDDEE\uD83C\uDDF7'
    };
    return bandeiras[pais] || '\uD83C\uDFF3\uFE0F';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
