import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  systemStatus: string = '';
  lastLogin: string = '';
  showAlert: boolean = true;
  private subscription = new Subscription();

  // Métricas reais do sistema
  cpuPercent: number = 0;
  memoryPercent: number = 0;
  iaLoadPercent: number = 0;
  memoryUsedMb: number = 0;
  memoryMaxMb: number = 0;
  uptimeFormatted: string = '';
  totalUsuarios: number = 0;
  totalTransacoes: number = 0;
  totalAnalises: number = 0;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeComponent();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private initializeComponent(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUserInfo();
    this.checkSystemStatus();
    this.loadSystemMetrics();
    this.lastLogin = new Date().toLocaleString('pt-BR');
  }

  private loadUserInfo(): void {
    const userSub = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
    this.subscription.add(userSub);
  }

  checkSystemStatus(): void {
    const statusSub = this.apiService.getStatus().subscribe({
      next: (status) => {
        this.systemStatus = status || 'Sistema operacional';
      },
      error: (error) => {
        if (error.status === 0) {
          this.systemStatus = 'API não disponível';
        } else if (error.status === 401 || error.status === 403) {
          this.systemStatus = 'Não autorizado - faça login novamente';
        } else {
          this.systemStatus = `Erro ${error.status}: ${error.statusText || 'Erro desconhecido'}`;
        }
      }
    });
    this.subscription.add(statusSub);
  }

  loadSystemMetrics(): void {
    const metricsSub = this.apiService.getSystemHealth().subscribe({
      next: (metrics) => {
        if (metrics.cpu) {
          this.cpuPercent = metrics.cpu.percent || 0;
        }
        if (metrics.memory) {
          this.memoryPercent = metrics.memory.percent || 0;
          this.memoryUsedMb = metrics.memory.usedMb || 0;
          this.memoryMaxMb = metrics.memory.maxMb || 0;
        }
        if (metrics.iaLoad) {
          this.iaLoadPercent = metrics.iaLoad.percent || 0;
        }
        if (metrics.uptime) {
          this.uptimeFormatted = metrics.uptime.formatted || '';
        }
        if (metrics.appStats) {
          this.totalUsuarios = metrics.appStats.totalUsuarios || 0;
          this.totalTransacoes = metrics.appStats.totalTransacoesBlockchain || 0;
          this.totalAnalises = metrics.appStats.totalAnalisesIA || 0;
        }
      },
      error: (error) => {
        console.warn('Erro ao carregar métricas do sistema:', error);
      }
    });
    this.subscription.add(metricsSub);
  }

  logout(): void {
    this.authService.logout();
  }
}
