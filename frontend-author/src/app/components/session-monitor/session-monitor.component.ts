import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

interface SessionStats {
  totalSessions: number;
  activeSessions: number;
  blockedSessions: number;
  suspiciousSessions: number;
  uniqueDevices: number;
  averageSessionDuration: number;
  riskDistribution: {
    low: number;
    medium: number;
    high: number;
  };
}

interface Session {
  id: string;
  userId: string;
  deviceInfo: string;
  location: string;
  loginTime: string;
  lastActivity: string;
  riskLevel: string;
  status: string;
  trustScore: number;
  usuario?: {
    email: string;
  };
  ipAddress: string;
}

interface DashboardData {
  stats: SessionStats;
  sessions: Session[];
}

@Component({
  selector: 'app-session-monitor',
  templateUrl: './session-monitor.component.html',
  styleUrls: ['./session-monitor.component.scss']
})
export class SessionMonitorComponent implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  sessoes: Session[] = [];
  estatisticas: SessionStats = {
    totalSessions: 0,
    activeSessions: 0,
    blockedSessions: 0,
    suspiciousSessions: 0,
    uniqueDevices: 0,
    averageSessionDuration: 0,
    riskDistribution: {
      low: 0,
      medium: 0,
      high: 0
    }
  };
  loading = true;
  error: string | null = null;
  private refreshSubscription?: Subscription;

  // Configurações dos gráficos
  deviceChartData: ChartData = {
    labels: [],
    datasets: [{
      data: [],
      label: 'Dispositivos'
    }]
  };

  locationChartData: ChartData = {
    labels: [],
    datasets: [{
      data: [],
      label: 'Localizações'
    }]
  };

  riskLevelChartData: ChartData = {
    labels: ['Baixo', 'Médio', 'Alto'],
    datasets: [{
      data: [0, 0, 0],
      label: 'Níveis de Risco'
    }]
  };

  sessionTrendChartData: ChartData = {
    labels: [],
    datasets: [{
      data: [],
      label: 'Sessões'
    }]
  };

  // Opções comuns dos gráficos
  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      }
    }
  };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    // Atualizar dados a cada 30 segundos
    this.refreshSubscription = interval(30000)
      .pipe(
        switchMap(() => this.apiService.getSessionStats())
      )
      .subscribe({
        next: (data: DashboardData) => this.updateDashboard(data),
        error: (err: Error) => {
          console.error('Erro ao atualizar dashboard:', err);
          this.error = 'Erro ao carregar dados do dashboard';
        }
      });

    // Carregar dados iniciais
    this.loadDashboardData();
  }

  ngOnDestroy() {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  private loadDashboardData() {
    this.loading = true;
    this.apiService.getSessionStats().subscribe({
      next: (data: DashboardData) => {
        this.updateDashboard(data);
        this.loading = false;
      },
      error: (err: Error) => {
        console.error('Erro ao carregar dashboard:', err);
        this.error = 'Erro ao carregar dados do dashboard';
        this.loading = false;
      }
    });
  }

  private updateDashboard(data: DashboardData) {
    this.estatisticas = data.stats;
    this.sessoes = data.sessions;
    
    // Atualizar dados dos gráficos
    this.updateChartData();
  }

  private updateChartData() {
    // Dados de dispositivos
    const deviceData = this.processDeviceData();
    this.deviceChartData = {
      labels: deviceData.map(d => d.name),
      datasets: [{
        data: deviceData.map(d => d.value),
        label: 'Dispositivos'
      }]
    };

    // Dados de localização
    const locationData = this.processLocationData();
    this.locationChartData = {
      labels: locationData.map(d => d.name),
      datasets: [{
        data: locationData.map(d => d.value),
        label: 'Localizações'
      }]
    };

    // Dados de nível de risco
    const riskData = this.processRiskLevelData();
    this.riskLevelChartData = {
      labels: riskData.map(d => d.name),
      datasets: [{
        data: riskData.map(d => d.value),
        label: 'Níveis de Risco'
      }]
    };

    // Tendência de sessões
    const trendData = this.processSessionTrendData();
    this.sessionTrendChartData = {
      labels: trendData.map(d => d.name),
      datasets: [{
        data: trendData.map(d => d.value),
        label: 'Sessões'
      }]
    };

    // Atualizar os gráficos
    this.chart?.update();
  }

  private processDeviceData() {
    const deviceCount = new Map<string, number>();
    this.sessoes.forEach(sessao => {
      const device = sessao.deviceInfo || 'Desconhecido';
      deviceCount.set(device, (deviceCount.get(device) || 0) + 1);
    });
    
    return Array.from(deviceCount.entries()).map(([name, value]) => ({
      name,
      value
    }));
  }

  private processLocationData() {
    const locationCount = new Map<string, number>();
    this.sessoes.forEach(sessao => {
      const location = sessao.location || 'Desconhecido';
      locationCount.set(location, (locationCount.get(location) || 0) + 1);
    });
    
    return Array.from(locationCount.entries()).map(([name, value]) => ({
      name,
      value
    }));
  }

  private processRiskLevelData() {
    const riskCount = new Map<string, number>();
    this.sessoes.forEach(sessao => {
      const risk = sessao.riskLevel || 'DESCONHECIDO';
      riskCount.set(risk, (riskCount.get(risk) || 0) + 1);
    });
    
    return Array.from(riskCount.entries()).map(([name, value]) => ({
      name,
      value
    }));
  }

  private processSessionTrendData() {
    // Agrupar sessões por hora
    const hourlyData = new Map<number, number>();
    const now = new Date();
    
    this.sessoes.forEach(sessao => {
      const sessionDate = new Date(sessao.loginTime);
      const hourDiff = Math.floor((now.getTime() - sessionDate.getTime()) / (1000 * 60 * 60));
      if (hourDiff <= 24) { // Últimas 24 horas
        hourlyData.set(hourDiff, (hourlyData.get(hourDiff) || 0) + 1);
      }
    });
    
    // Criar array com as últimas 24 horas
    return Array.from({length: 24}, (_, i) => ({
      name: `${23-i}h atrás`,
      value: hourlyData.get(i) || 0
    })).reverse();
  }

  bloquearSessao(sessaoId: string) {
    this.apiService.blockSession(sessaoId).subscribe({
      next: () => {
        this.loadDashboardData(); // Recarregar dados
      },
      error: (err: Error) => {
        console.error('Erro ao bloquear sessão:', err);
        this.error = 'Erro ao bloquear sessão';
      }
    });
  }

  encerrarSessao(sessaoId: string) {
    this.apiService.endSession(sessaoId).subscribe({
      next: () => {
        this.loadDashboardData(); // Recarregar dados
      },
      error: (err: Error) => {
        console.error('Erro ao encerrar sessão:', err);
        this.error = 'Erro ao encerrar sessão';
      }
    });
  }
} 