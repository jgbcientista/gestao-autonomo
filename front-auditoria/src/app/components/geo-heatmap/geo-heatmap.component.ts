import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface PontoMapa {
  latitude: number;
  longitude: number;
  cidade: string;
  contagem: number;
  falhas?: number;
  nivelRisco: string;
  ultimoAcesso?: string;
}

interface ConexaoMapa {
  origem: { latitude: number; longitude: number; cidade: string };
  destino: { latitude: number; longitude: number; cidade: string };
  distanciaKm: number;
  intervaloMinutos: number;
  velocidadeKmH: number;
  viagemImpossivel: boolean;
}

@Component({
  selector: 'app-geo-heatmap',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './geo-heatmap.component.html',
  styleUrls: ['./geo-heatmap.component.scss']
})
export class GeoHeatmapComponent implements OnInit {

  usuarioAtual: any = null;
  usuarios: any[] = [];
  selectedUserId: number | null = null;
  loading = false;
  error: string | null = null;

  acessosNormais: PontoMapa[] = [];
  acessosSuspeitos: PontoMapa[] = [];
  conexoes: ConexaoMapa[] = [];
  totalAcessos = 0;

  // SVG map dimensions
  mapWidth = 1000;
  mapHeight = 500;

  // Tooltip
  tooltipVisible = false;
  tooltipX = 0;
  tooltipY = 0;
  tooltipData: PontoMapa | null = null;

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
            this.loadMapData();
          }
        }
      },
      error: () => { this.error = 'Erro ao carregar usuários.'; }
    });
  }

  onUsuarioSelecionado(): void {
    if (this.selectedUserId) {
      this.loadMapData();
    } else {
      this.acessosNormais = [];
      this.acessosSuspeitos = [];
      this.conexoes = [];
    }
  }

  loadMapData(): void {
    if (!this.selectedUserId) return;
    this.loading = true;
    this.error = null;

    this.apiService.getMapaAcessos(this.selectedUserId).subscribe({
      next: (data) => {
        this.acessosNormais = data.acessosNormais || [];
        this.acessosSuspeitos = data.acessosSuspeitos || [];
        this.conexoes = data.conexoes || [];
        this.totalAcessos = data.totalAcessos || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erro ao carregar dados do mapa.';
        this.loading = false;
      }
    });
  }

  // Mercator projection
  projectX(lon: number): number {
    return (lon + 180) * (this.mapWidth / 360);
  }

  projectY(lat: number): number {
    const latRad = lat * Math.PI / 180;
    const mercN = Math.log(Math.tan((Math.PI / 4) + (latRad / 2)));
    return (this.mapHeight / 2) - (this.mapWidth * mercN / (2 * Math.PI));
  }

  getDotRadius(contagem: number): number {
    return Math.min(12, Math.max(5, 3 + contagem * 1.5));
  }

  getDotColor(ponto: PontoMapa): string {
    if (ponto.nivelRisco === 'ALTO' || (ponto.falhas && ponto.falhas > 0)) return '#E52207';
    if (ponto.nivelRisco === 'MEDIO') return '#B8860B';
    return '#168821';
  }

  getConnectionColor(conexao: ConexaoMapa): string {
    return conexao.viagemImpossivel ? '#E52207' : '#1351B4';
  }

  getConnectionDash(conexao: ConexaoMapa): string {
    return conexao.viagemImpossivel ? '8,4' : 'none';
  }

  getConnectionOpacity(conexao: ConexaoMapa): number {
    return conexao.viagemImpossivel ? 0.8 : 0.3;
  }

  showTooltip(event: MouseEvent, ponto: PontoMapa): void {
    this.tooltipData = ponto;
    this.tooltipX = event.offsetX + 15;
    this.tooltipY = event.offsetY - 10;
    this.tooltipVisible = true;
  }

  hideTooltip(): void {
    this.tooltipVisible = false;
    this.tooltipData = null;
  }

  get viagensImpossiveis(): ConexaoMapa[] {
    return this.conexoes.filter(c => c.viagemImpossivel);
  }

  get todosOsPontos(): PontoMapa[] {
    return [...this.acessosNormais, ...this.acessosSuspeitos];
  }

  formatarData(data: string | null | undefined): string {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
