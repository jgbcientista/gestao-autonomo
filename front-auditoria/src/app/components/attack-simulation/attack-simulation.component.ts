import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface EtapaSimulacao {
  numero: number;
  descricao: string;
  scoreConfianca: number;
  scoreAnomalia?: number;
  decisao: string;
  ip?: string;
  localizacao?: string;
  dispositivo?: string;
  blockchainHash?: string;
  distanciaKm?: number;
  velocidadeKmH?: number;
  timestamp: string;
}

interface ResultadoSimulacao {
  cenario: string;
  etapas: EtapaSimulacao[];
  duracaoTotalMs: number;
}

@Component({
  selector: 'app-attack-simulation',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './attack-simulation.component.html',
  styleUrls: ['./attack-simulation.component.scss']
})
export class AttackSimulationComponent implements OnInit {

  usuarioAtual: any = null;
  usuarios: any[] = [];
  selectedEmail: string = '';
  loading = false;
  simulando = false;
  etapaAtual = 0;
  resultado: ResultadoSimulacao | null = null;
  etapasVisiveis: EtapaSimulacao[] = [];
  cenarioAtivo: string = '';
  error: string | null = null;
  resetando = false;

  cenarios = [
    {
      id: 'forca-bruta',
      titulo: 'Força Bruta',
      descricao: 'Simula 10 tentativas rápidas de login do mesmo IP. O sistema detecta o padrão e bloqueia automaticamente.',
      icone: 'bi-shield-exclamation',
      cor: '#E52207',
      corBg: '#FDE8E8'
    },
    {
      id: 'credential-stuffing',
      titulo: 'Credential Stuffing',
      descricao: 'Simula tentativas de login de 8 IPs internacionais diferentes em curto intervalo.',
      icone: 'bi-globe2',
      cor: '#B8860B',
      corBg: '#FFF8E1'
    },
    {
      id: 'viagem-impossivel',
      titulo: 'Viagem Impossível',
      descricao: 'Login em São Paulo e 5 minutos depois em Tokyo. Distância impossível de percorrer.',
      icone: 'bi-airplane-fill',
      cor: '#1351B4',
      corBg: '#E3F2FD'
    },
    {
      id: 'sequestro-dispositivo',
      titulo: 'Sequestro de Dispositivo',
      descricao: 'Acesso de dispositivo e localização totalmente novos e desconhecidos pelo sistema.',
      icone: 'bi-laptop',
      cor: '#7c3aed',
      corBg: '#F3E8FF'
    }
  ];

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
          this.selectedEmail = this.usuarioAtual.email;
        }
      },
      error: () => {
        this.error = 'Erro ao carregar usuários.';
      }
    });
  }

  executarSimulacao(cenarioId: string): void {
    if (!this.selectedEmail || this.simulando) return;

    this.simulando = true;
    this.cenarioAtivo = cenarioId;
    this.etapaAtual = 0;
    this.etapasVisiveis = [];
    this.resultado = null;
    this.error = null;

    let observable;
    switch (cenarioId) {
      case 'forca-bruta':
        observable = this.apiService.simularForcaBruta(this.selectedEmail);
        break;
      case 'credential-stuffing':
        observable = this.apiService.simularCredentialStuffing(this.selectedEmail);
        break;
      case 'viagem-impossivel':
        observable = this.apiService.simularViagemImpossivel(this.selectedEmail);
        break;
      case 'sequestro-dispositivo':
        observable = this.apiService.simularSequestroDispositivo(this.selectedEmail);
        break;
      default:
        this.simulando = false;
        return;
    }

    observable.subscribe({
      next: (data: ResultadoSimulacao) => {
        this.resultado = data;
        this.animarEtapas(data.etapas);
      },
      error: (err: any) => {
        console.error('Erro na simulacao:', err);
        this.error = 'Erro ao executar simulação. Verifique a conexão com o servidor.';
        this.simulando = false;
      }
    });
  }

  private animarEtapas(etapas: EtapaSimulacao[]): void {
    if (!etapas || etapas.length === 0) {
      this.simulando = false;
      return;
    }

    let index = 0;
    const interval = setInterval(() => {
      if (index < etapas.length) {
        this.etapasVisiveis.push(etapas[index]);
        this.etapaAtual = index + 1;
        index++;
      } else {
        clearInterval(interval);
        this.simulando = false;
      }
    }, 800);
  }

  resetarSimulacao(): void {
    if (!this.selectedEmail || this.resetando) return;

    this.resetando = true;
    this.apiService.resetarSimulacao(this.selectedEmail).subscribe({
      next: () => {
        this.resultado = null;
        this.etapasVisiveis = [];
        this.cenarioAtivo = '';
        this.etapaAtual = 0;
        this.resetando = false;
      },
      error: () => {
        this.error = 'Erro ao resetar simulação.';
        this.resetando = false;
      }
    });
  }

  getDecisaoColor(decisao: string): string {
    switch (decisao) {
      case 'PERMITIR': return '#168821';
      case 'EXIGIR_MFA': return '#B8860B';
      case 'BLOQUEAR': return '#E52207';
      default: return '#555';
    }
  }

  getDecisaoBg(decisao: string): string {
    switch (decisao) {
      case 'PERMITIR': return '#E8F5E9';
      case 'EXIGIR_MFA': return '#FFF8E1';
      case 'BLOQUEAR': return '#FDE8E8';
      default: return '#F8F8F8';
    }
  }

  getDecisaoIcone(decisao: string): string {
    switch (decisao) {
      case 'PERMITIR': return 'bi-check-circle-fill';
      case 'EXIGIR_MFA': return 'bi-lock-fill';
      case 'BLOQUEAR': return 'bi-x-octagon-fill';
      default: return 'bi-question-circle';
    }
  }

  getUltimaEtapaScore(): number {
    if (!this.etapasVisiveis || this.etapasVisiveis.length === 0) return 0;
    return this.etapasVisiveis[this.etapasVisiveis.length - 1].scoreConfianca || 0;
  }

  getUltimaEtapaDecisao(): string {
    if (!this.etapasVisiveis || this.etapasVisiveis.length === 0) return '';
    return this.etapasVisiveis[this.etapasVisiveis.length - 1].decisao || '';
  }

  getScoreColor(score: number): string {
    if (score >= 0.7) return '#168821';
    if (score >= 0.5) return '#B8860B';
    if (score >= 0.2) return '#E52207';
    return '#991b1b';
  }

  getScoreWidth(score: number): number {
    return Math.max(2, score * 100);
  }

  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
