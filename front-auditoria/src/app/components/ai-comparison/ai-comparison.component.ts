import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface MetricaModelo {
  precisao: number;
  recall: number;
  f1Score: number;
  totalPredicoes: number;
  acuracia?: number;
}

interface AnaliseComparativa {
  id: number;
  timestamp: string;
  isolationForestScore: number;
  randomForestScore: number;
  deepLearningScore: number;
  ensembleScore: number;
  classificacaoAcesso: string;
  modelosDiscordam: boolean;
}

@Component({
  selector: 'app-ai-comparison',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './ai-comparison.component.html',
  styleUrls: ['./ai-comparison.component.scss']
})
export class AiComparisonComponent implements OnInit {

  Math = Math;
  usuarioAtual: any = null;
  usuarios: any[] = [];
  selectedUserId: number | null = null;
  loading = false;
  error: string | null = null;

  // Data
  analises: AnaliseComparativa[] = [];
  metricas: { [key: string]: MetricaModelo } = {};
  totalDiscordancias = 0;
  percentualConcordancia = 0;

  // Pagination
  page = 1;
  pageSize = 8;

  // Radar chart data
  radarSize = 300;
  radarCenter = 150;
  radarRadius = 120;

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
    this.loadMetricas();
  }

  loadUsuarios(): void {
    this.apiService.getUsuariosCadastrados().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        if (this.usuarioAtual?.email) {
          const found = usuarios.find((u: any) => u.email === this.usuarioAtual.email);
          if (found) {
            this.selectedUserId = found.id;
            this.loadComparacao();
          }
        }
      },
      error: () => { this.error = 'Erro ao carregar usuários.'; }
    });
  }

  onUsuarioSelecionado(): void {
    if (this.selectedUserId) {
      this.loadComparacao();
    } else {
      this.analises = [];
    }
  }

  loadComparacao(): void {
    if (!this.selectedUserId) return;
    this.loading = true;
    this.error = null;
    this.page = 1;

    this.apiService.getComparacaoModelos(this.selectedUserId).subscribe({
      next: (data) => {
        // Map API field names to frontend field names
        this.analises = (data.analises || []).map((a: any) => ({
          ...a,
          modelosDiscordam: a.desacordo ?? a.modelosDiscordam ?? false
        }));
        this.totalDiscordancias = data.totalDesacordos ?? data.totalDiscordancias ?? 0;
        this.percentualConcordancia = (data.percentualAcordo ?? data.percentualConcordancia ?? 0) * 100;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erro ao carregar comparação de modelos.';
        this.loading = false;
      }
    });
  }

  loadMetricas(): void {
    this.apiService.getMetricasModelos().subscribe({
      next: (data) => {
        // Map API field names (precisaoMedia -> precisao, recallMedio -> recall, etc.)
        const mapped: { [key: string]: MetricaModelo } = {};
        for (const key of ['isolationForest', 'randomForest', 'deepLearning']) {
          if (data[key]) {
            mapped[key] = {
              precisao: data[key].precisaoMedia ?? data[key].precisao ?? 0,
              recall: data[key].recallMedio ?? data[key].recall ?? 0,
              f1Score: data[key].f1ScoreMedio ?? data[key].f1Score ?? 0,
              totalPredicoes: data.totalRegistros ?? data[key].totalPredicoes ?? 0,
              acuracia: data[key].acuracia ?? 0
            };
          }
        }
        // Create ensemble as weighted average
        if (mapped['isolationForest'] && mapped['randomForest'] && mapped['deepLearning']) {
          mapped['ensemble'] = {
            precisao: mapped['isolationForest'].precisao * 0.4 + mapped['randomForest'].precisao * 0.3 + mapped['deepLearning'].precisao * 0.3,
            recall: mapped['isolationForest'].recall * 0.4 + mapped['randomForest'].recall * 0.3 + mapped['deepLearning'].recall * 0.3,
            f1Score: mapped['isolationForest'].f1Score * 0.4 + mapped['randomForest'].f1Score * 0.3 + mapped['deepLearning'].f1Score * 0.3,
            totalPredicoes: data.totalRegistros ?? 0
          };
        }
        this.metricas = mapped;
      },
      error: () => {
        console.error('Erro ao carregar métricas');
      }
    });
  }

  // Radar chart helpers
  getRadarPoint(index: number, value: number, total: number): string {
    const angle = (Math.PI * 2 * index / total) - Math.PI / 2;
    const r = this.radarRadius * value;
    const x = this.radarCenter + r * Math.cos(angle);
    const y = this.radarCenter + r * Math.sin(angle);
    return `${x},${y}`;
  }

  getRadarPolygon(scores: number[]): string {
    return scores.map((s, i) => this.getRadarPoint(i, s, scores.length)).join(' ');
  }

  getRadarLabelPos(index: number, total: number): { x: number; y: number } {
    const angle = (Math.PI * 2 * index / total) - Math.PI / 2;
    const r = this.radarRadius + 20;
    return {
      x: this.radarCenter + r * Math.cos(angle),
      y: this.radarCenter + r * Math.sin(angle)
    };
  }

  getGridPolygon(level: number): string {
    const axes = 5;
    const points = [];
    for (let i = 0; i < axes; i++) {
      points.push(this.getRadarPoint(i, level, axes));
    }
    return points.join(' ');
  }

  get radarLabels(): string[] {
    return ['Precisão', 'Recall', 'F1-Score', 'Consistência', 'Velocidade'];
  }

  getModelRadarScores(model: string): number[] {
    const m = this.metricas[model];
    if (!m) return [0, 0, 0, 0, 0];
    return [
      m.precisao || 0,
      m.recall || 0,
      m.f1Score || 0,
      this.percentualConcordancia / 100 || 0.85,
      0.9 // Speed is generally high for all
    ];
  }

  // Classification helpers
  getClassificacaoColor(classificacao: string): string {
    switch (classificacao?.toUpperCase()) {
      case 'ESPERADO': return '#168821';
      case 'SUSPEITO': return '#B8860B';
      case 'ANOMALO': return '#E52207';
      case 'ALTAMENTE_SUSPEITO': return '#991b1b';
      default: return '#555';
    }
  }

  getScoreClassificacao(score: number): string {
    if (score <= 0.3) return 'ESPERADO';
    if (score <= 0.7) return 'SUSPEITO';
    return 'ANOMALO';
  }

  formatPercent(value: number): string {
    return (value * 100).toFixed(1) + '%';
  }

  // Pagination
  get pagedAnalises(): AnaliseComparativa[] {
    const start = (this.page - 1) * this.pageSize;
    return this.analises.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.analises.length / this.pageSize);
  }

  get pages(): number[] {
    const p: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) p.push(i);
    return p;
  }

  goToPage(p: number): void {
    if (p >= 1 && p <= this.totalPages) this.page = p;
  }

  // Models info
  modelos = [
    { key: 'isolationForest', nome: 'Isolation Forest', cor: '#1351B4', peso: '40%', desc: 'Detecção de outliers sem supervisão' },
    { key: 'randomForest', nome: 'Random Forest', cor: '#168821', peso: '30%', desc: 'Classificação supervisionada por árvores' },
    { key: 'deepLearning', nome: 'Deep Learning', cor: '#B8860B', peso: '30%', desc: 'Rede neural com 4 camadas' },
    { key: 'ensemble', nome: 'Ensemble', cor: '#7c3aed', peso: '100%', desc: 'Combinação ponderada dos 3 modelos' }
  ];

  voltarDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
