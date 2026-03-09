import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { SessionManagerComponent } from './session-manager.component';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

describe('SessionManagerComponent', () => {
  let component: SessionManagerComponent;
  let fixture: ComponentFixture<SessionManagerComponent>;
  let mockApiService: jasmine.SpyObj<ApiService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockSessionMetrics = {
    totalSessions: 156,
    activeSessions: 23,
    suspiciousSessions: 5,
    averageSessionDuration: 2.8,
    uniqueDevices: 89,
    uniqueLocations: 15
  };

  const mockSession = {
    id: 'sess_1',
    userId: 1,
    userEmail: 'test@exemplo.com',
    userName: 'Usuário Teste',
    deviceInfo: 'Chrome 120.0 (Windows 10)',
    ipAddress: '192.168.1.1',
    location: 'São Paulo, SP - Brasil',
    loginTime: new Date(),
    lastActivity: new Date(),
    sessionDuration: 2.5,
    isActive: true,
    riskLevel: 'BAIXO' as const,
    activities: []
  };

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiService', [
      'getSystemHealth',
      'getHealthStatus'
    ]);

    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getCurrentUser',
      'getToken',
      'isAuthenticated'
    ]);

    await TestBed.configureTestingModule({
      imports: [SessionManagerComponent, FormsModule],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SessionManagerComponent);
    component = fixture.componentInstance;
    mockApiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  describe('Inicialização', () => {
    it('deve criar o componente', () => {
      expect(component).toBeTruthy();
    });

    it('deve inicializar com valores padrão', () => {
      expect(component.loading).toBeFalse();
      expect(component.error).toBeNull();
      expect(component.modoSimulado).toBeFalse();
      expect(component.sessions).toEqual([]);
      expect(component.sessionMetrics).toBeNull();
      expect(component.securityAlerts).toEqual([]);
      expect(component.visualizacaoAtiva).toBe('overview');
      expect(component.monitoramentoAtivo).toBeFalse();
    });

    it('deve carregar dados no ngOnInit quando no browser', fakeAsync(() => {
      spyOn(component, 'carregarDados');
      spyOn(component, 'iniciarMonitoramento');
      
      component.ngOnInit();
      tick(1000);
      
      expect(component.carregarDados).toHaveBeenCalled();
      expect(component.iniciarMonitoramento).toHaveBeenCalled();
    }));
  });

  describe('Carregamento de Dados', () => {
    it('deve carregar dados reais quando usuário autenticado', fakeAsync(() => {
      mockAuthService.isAuthenticated.and.returnValue(true);
      mockAuthService.getToken.and.returnValue('mock-token');
      mockAuthService.getCurrentUser.and.returnValue({ id: 1, name: 'Test User', email: 'test@test.com' });
      
      mockApiService.getSystemHealth.and.returnValue(of({ status: 'ok' }));
      mockApiService.getHealthStatus.and.returnValue(of({ healthy: true }));

      component.carregarDados();
      tick();

      expect(component.loading).toBeFalse();
      expect(component.modoSimulado).toBeFalse();
      expect(mockApiService.getSystemHealth).toHaveBeenCalled();
      expect(mockApiService.getHealthStatus).toHaveBeenCalled();
    }));

    it('deve usar dados simulados quando usuário não autenticado', () => {
      mockAuthService.isAuthenticated.and.returnValue(false);
      mockAuthService.getToken.and.returnValue(null);

      component.carregarDados();

      expect(component.modoSimulado).toBeTrue();
      expect(component.loading).toBeFalse();
      expect(component.sessions.length).toBeGreaterThan(0);
      expect(component.sessionMetrics).toBeTruthy();
    });

    it('deve usar fallback para dados simulados em caso de erro da API', fakeAsync(() => {
      mockAuthService.isAuthenticated.and.returnValue(true);
      mockAuthService.getToken.and.returnValue('mock-token');
      
      mockApiService.getSystemHealth.and.returnValue(throwError(() => new Error('API Error')));
      mockApiService.getHealthStatus.and.returnValue(throwError(() => new Error('API Error')));

      component.carregarDados();
      tick();

      expect(component.modoSimulado).toBeTrue();
      expect(component.loading).toBeFalse();
      expect(component.sessions.length).toBeGreaterThan(0);
    }));
  });

  describe('Ações de Sessão', () => {
    beforeEach(() => {
      component.sessions = [mockSession];
    });

    it('deve encerrar sessão corretamente', () => {
      const sessionId = 'sess_1';
      
      component.encerrarSessao(sessionId);
      
      const session = component.sessions.find(s => s.id === sessionId);
      expect(session?.isActive).toBeFalse();
      expect(session?.lastActivity).toBeInstanceOf(Date);
    });

    it('deve bloquear usuário e encerrar todas as sessões', () => {
      const userId = 1;
      
      component.bloquearUsuario(userId);
      
      const userSessions = component.sessions.filter(s => s.userId === userId);
      userSessions.forEach(session => {
        expect(session.isActive).toBeFalse();
        expect(session.riskLevel).toBe('CRITICO');
      });
    });

    it('deve visualizar detalhes da sessão', () => {
      component.visualizarDetalhes(mockSession);
      
      expect(component.selectedSession).toBe(mockSession);
      expect(component.visualizacaoAtiva).toBe('detalhes');
    });
  });

  describe('Alertas de Segurança', () => {
    beforeEach(() => {
      component.securityAlerts = [
        {
          id: 'alert_1',
          sessionId: 'sess_1',
          type: 'SUSPICIOUS_LOGIN',
          severity: 'HIGH',
          message: 'Login suspeito detectado',
          timestamp: new Date(),
          resolved: false
        }
      ];
    });

    it('deve marcar alerta como resolvido', () => {
      const alertId = 'alert_1';
      
      component.marcarAlertaComoResolvido(alertId);
      
      const alert = component.securityAlerts.find(a => a.id === alertId);
      expect(alert?.resolved).toBeTrue();
    });

    it('deve contar alertas não resolvidos corretamente', () => {
      const count = component.obterAlertasNaoResolvidos();
      expect(count).toBe(1);
      
      component.marcarAlertaComoResolvido('alert_1');
      const newCount = component.obterAlertasNaoResolvidos();
      expect(newCount).toBe(0);
    });
  });

  describe('Navegação e Visualização', () => {
    it('deve alternar visualização corretamente', () => {
      component.alternarVisualizacao('sessions');
      expect(component.visualizacaoAtiva).toBe('sessions');
      
      component.alternarVisualizacao('overview');
      expect(component.visualizacaoAtiva).toBe('overview');
      expect(component.selectedSession).toBeNull();
    });

    it('deve aplicar filtros e recarregar dados', () => {
      spyOn(component, 'carregarDados');
      
      component.aplicarFiltros();
      
      expect(component.carregarDados).toHaveBeenCalled();
    });
  });

  describe('Monitoramento', () => {
    it('deve iniciar monitoramento corretamente', fakeAsync(() => {
      spyOn(component, 'carregarDados');
      
      component.iniciarMonitoramento();
      
      expect(component.monitoramentoAtivo).toBeTrue();
      
      tick(30000);
      expect(component.carregarDados).toHaveBeenCalled();
      
      component.pararMonitoramento();
    }));

    it('deve parar monitoramento corretamente', () => {
      component.iniciarMonitoramento();
      expect(component.monitoramentoAtivo).toBeTrue();
      
      component.pararMonitoramento();
      expect(component.monitoramentoAtivo).toBeFalse();
    });

    it('deve alternar monitoramento', () => {
      expect(component.monitoramentoAtivo).toBeFalse();
      
      component.alternarMonitoramento();
      expect(component.monitoramentoAtivo).toBeTrue();
      
      component.alternarMonitoramento();
      expect(component.monitoramentoAtivo).toBeFalse();
    });
  });

  describe('Filtros e Ordenação', () => {
    beforeEach(() => {
      component.sessions = [
        { ...mockSession, id: 'sess_1', isActive: true, riskLevel: 'BAIXO' },
        { ...mockSession, id: 'sess_2', isActive: false, riskLevel: 'ALTO' },
        { ...mockSession, id: 'sess_3', isActive: true, riskLevel: 'CRITICO' }
      ];
    });

    it('deve filtrar sessões por status', () => {
      component.filtroStatus = 'ATIVAS';
      const filtered = component.obterSessoesFiltradas();
      expect(filtered.length).toBe(2);
      expect(filtered.every(s => s.isActive)).toBeTrue();
    });

    it('deve filtrar sessões por risco', () => {
      component.filtroRisco = 'CRITICO';
      const filtered = component.obterSessoesFiltradas();
      expect(filtered.length).toBe(1);
      expect(filtered[0].riskLevel).toBe('CRITICO');
    });

    it('deve ordenar sessões por nível de risco', () => {
      component.ordenacao = 'riskLevel';
      const filtered = component.obterSessoesFiltradas();
      expect(filtered[0].riskLevel).toBe('CRITICO');
      expect(filtered[2].riskLevel).toBe('BAIXO');
    });
  });

  describe('Métodos de Formatação', () => {
    it('deve formatar duração corretamente', () => {
      expect(component.formatarDuracao(0.5)).toBe('30 min');
      expect(component.formatarDuracao(1.5)).toBe('1.5h');
      expect(component.formatarDuracao(2.75)).toBe('2.8h');
    });

    it('deve formatar data corretamente', () => {
      const data = new Date('2024-01-15T10:30:00');
      const formatted = component.formatarData(data);
      expect(formatted).toContain('15/01/2024');
    });

    it('deve obter cor de risco corretamente', () => {
      expect(component.obterCorRisco('BAIXO')).toBe('success');
      expect(component.obterCorRisco('MEDIO')).toBe('warning');
      expect(component.obterCorRisco('ALTO')).toBe('danger');
      expect(component.obterCorRisco('CRITICO')).toBe('danger');
    });

    it('deve obter cor de severidade corretamente', () => {
      expect(component.obterCorSeveridade('LOW')).toBe('success');
      expect(component.obterCorSeveridade('MEDIUM')).toBe('warning');
      expect(component.obterCorSeveridade('HIGH')).toBe('danger');
      expect(component.obterCorSeveridade('CRITICAL')).toBe('danger');
    });

    it('deve obter ícone de risco corretamente', () => {
      expect(component.obterIconeRisco('BAIXO')).toBe('✓');
      expect(component.obterIconeRisco('MEDIO')).toBe('⚠');
      expect(component.obterIconeRisco('ALTO')).toBe('⚠');
      expect(component.obterIconeRisco('CRITICO')).toBe('⚠');
    });
  });

  describe('Limpeza', () => {
    it('deve limpar recursos no ngOnDestroy', () => {
      spyOn(component['subscriptions'], 'unsubscribe');
      spyOn(component, 'pararMonitoramento');
      
      component.ngOnDestroy();
      
      expect(component['subscriptions'].unsubscribe).toHaveBeenCalled();
      expect(component.pararMonitoramento).toHaveBeenCalled();
    });
  });
});
