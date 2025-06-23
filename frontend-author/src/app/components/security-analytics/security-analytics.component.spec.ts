import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { SecurityAnalyticsComponent } from './security-analytics.component';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { 
  SecurityMetrics,
  UserRiskAssessment,
  BlockchainTransaction,
  Alert,
  RecentActivity
} from '../../models/system.model';

describe('SecurityAnalyticsComponent', () => {
  let component: SecurityAnalyticsComponent;
  let fixture: ComponentFixture<SecurityAnalyticsComponent>;
  let mockApiService: jasmine.SpyObj<ApiService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockSecurityMetrics: SecurityMetrics = {
    totalUsuarios: 150,
    pontuacaoRiscoMedia: 0.35,
    usuariosAltoRisco: 12,
    transacoesAltoRisco: 8,
    transacoesNaoVerificadas: 3,
    integridadeBlockchain: 'BOA'
  };

  const mockUsuarioAtual = {
    id: 1,
    email: 'admin@teste.com',
    name: 'Admin'
  };

  const mockTransacoesAltoRisco: BlockchainTransaction[] = [
    {
      id: 1,
      usuarioId: 1,
      tipoOperacao: 'PAGAMENTO',
      hashTransacao: 'abc123def456',
      statusConfirmacao: 'CONFIRMADA',
      numeroBloco: 12345,
      confirmacoes: 6,
      scoreRisco: 0.85,
      decisao: 'APROVADA',
      timestamp: new Date(),
      detalhesOperacao: 'Pagamento de alto valor'
    }
  ];

  const mockTransacoesNaoVerificadas: BlockchainTransaction[] = [
    {
      id: 2,
      usuarioId: 2,
      tipoOperacao: 'TRANSFERENCIA',
      hashTransacao: 'xyz789uvw012',
      statusConfirmacao: 'PENDENTE',
      confirmacoes: 0,
      scoreRisco: 0.45,
      decisao: 'PENDENTE',
      timestamp: new Date(),
      detalhesOperacao: 'Transferência pendente'
    }
  ];

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiService', [
      'getSecurityMetrics',
      'getHighRiskTransactions',
      'getUnverifiedTransactions',
      'getUserRiskAssessment',
      'getUserBehaviorPattern',
      'getUserBlockchainTransactions',
      'getUserLocationHistory',
      'verifyTransaction'
    ]);

    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getCurrentUser'
    ]);

    await TestBed.configureTestingModule({
      imports: [SecurityAnalyticsComponent],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecurityAnalyticsComponent);
    component = fixture.componentInstance;
    mockApiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    // Configurar mocks padrão
    mockAuthService.getCurrentUser.and.returnValue(mockUsuarioAtual);
    mockApiService.getSecurityMetrics.and.returnValue(of(mockSecurityMetrics));
    mockApiService.getHighRiskTransactions.and.returnValue(of(mockTransacoesAltoRisco));
    mockApiService.getUnverifiedTransactions.and.returnValue(of(mockTransacoesNaoVerificadas));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component and load data on ngOnInit', () => {
    spyOn(component, 'carregarDados');
    spyOn(component, 'iniciarMonitoramento');

    component.ngOnInit();

    expect(component.carregarDados).toHaveBeenCalled();
    expect(component.iniciarMonitoramento).toHaveBeenCalled();
  });

  it('should load security data successfully', () => {
    component.carregarDados();

    expect(component.loading).toBe(true);
    expect(mockApiService.getSecurityMetrics).toHaveBeenCalled();
    expect(mockApiService.getHighRiskTransactions).toHaveBeenCalledWith(0.7);
    expect(mockApiService.getUnverifiedTransactions).toHaveBeenCalled();
  });

  it('should handle error when loading data', () => {
    const errorMessage = 'Erro de rede';
    mockApiService.getSecurityMetrics.and.returnValue(throwError(() => new Error(errorMessage)));

    component.carregarDados();

    expect(component.error).toBe('Erro ao carregar dados de segurança');
    expect(component.loading).toBe(false);
  });

  it('should handle unauthenticated user', () => {
    mockAuthService.getCurrentUser.and.returnValue(null);

    component.carregarDados();

    expect(component.error).toBe('Usuário não autenticado');
    expect(component.loading).toBe(false);
  });

  it('should generate high risk users based on metrics', () => {
    component.metricas = mockSecurityMetrics;
    (component as any).gerarUsuariosAltoRisco();

    expect(component.usuariosAltoRisco.length).toBe(mockSecurityMetrics.usuariosAltoRisco);
    expect(component.usuariosAltoRisco[0].pontuacaoRiscoAtual).toBeGreaterThanOrEqual(0.7);
    expect(['ALTO', 'CRITICO']).toContain(component.usuariosAltoRisco[0].nivelRisco);
  });

  it('should generate alerts', () => {
    (component as any).gerarAlertas();

    expect(component.alertas.length).toBe(5);
    expect(component.alertas[0].id).toBeDefined();
    expect(component.alertas[0].tipo).toBeDefined();
    expect(component.alertas[0].titulo).toBeDefined();
    expect(component.alertas[0].mensagem).toBeDefined();
    expect(component.alertas[0].timestamp).toBeDefined();
    expect(component.alertas[0].lida).toBeDefined();
  });

  it('should generate recent activities', () => {
    (component as any).gerarAtividadesRecentes();

    expect(component.atividadesRecentes.length).toBe(8);
    expect(component.atividadesRecentes[0].id).toBeDefined();
    expect(component.atividadesRecentes[0].tipo).toBeDefined();
    expect(component.atividadesRecentes[0].descricao).toBeDefined();
    expect(component.atividadesRecentes[0].timestamp).toBeDefined();
    expect(component.atividadesRecentes[0].risco).toBeDefined();
  });

  it('should analyze user and call all related APIs', () => {
    const userId = 1;
    const mockRiskAssessment: UserRiskAssessment = {
      idUsuario: userId,
      emailUsuario: 'teste@exemplo.com',
      pontuacaoRiscoAtual: 0.75,
      nivelRisco: 'ALTO',
      totalLogins: 50,
      atividadesSuspeitas: 5,
      transacoesNegadas: 2,
      totalTransacoes: 20,
      ultimoLoginData: new Date(),
      contaBloqueada: false
    };

    mockApiService.getUserRiskAssessment.and.returnValue(of(mockRiskAssessment));
    mockApiService.getUserBehaviorPattern.and.returnValue(of({}));
    mockApiService.getUserBlockchainTransactions.and.returnValue(of([]));
    mockApiService.getUserLocationHistory.and.returnValue(of([]));

    component.analisarUsuario(userId);

    expect(mockApiService.getUserRiskAssessment).toHaveBeenCalledWith(userId);
    expect(mockApiService.getUserBehaviorPattern).toHaveBeenCalledWith(userId);
    expect(mockApiService.getUserBlockchainTransactions).toHaveBeenCalledWith(userId);
    expect(mockApiService.getUserLocationHistory).toHaveBeenCalledWith(userId);
    expect(component.visualizacaoAtiva).toBe('usuario');
  });

  it('should verify transaction', () => {
    const hash = 'abc123def456';
    const mockResult = { verified: true };
    mockApiService.verifyTransaction.and.returnValue(of(mockResult));
    spyOn(component, 'carregarDados');

    component.verificarTransacao(hash);

    expect(mockApiService.verifyTransaction).toHaveBeenCalledWith(hash);
    expect(component.carregarDados).toHaveBeenCalled();
  });

  it('should toggle visualization', () => {
    component.alternarVisualizacao('usuarios');
    expect(component.visualizacaoAtiva).toBe('usuarios');

    component.alternarVisualizacao('overview');
    expect(component.visualizacaoAtiva).toBe('overview');
    expect(component.avaliacaoRiscoSelecionada).toBeNull();
    expect(component.padraoComportamento).toBeNull();
  });

  it('should apply filter and reload data', () => {
    spyOn(component, 'carregarDados');

    component.aplicarFiltro();

    expect(component.carregarDados).toHaveBeenCalled();
  });

  it('should start monitoring', () => {
    jasmine.clock().install();
    spyOn(component, 'carregarDados');

    component.iniciarMonitoramento();

    expect(component.monitoramentoAtivo).toBe(true);
    expect(component.intervalId).toBeDefined();

    jasmine.clock().tick(30001);
    expect(component.carregarDados).toHaveBeenCalled();

    jasmine.clock().uninstall();
  });

  it('should stop monitoring', () => {
    component.monitoramentoAtivo = true;
    component.intervalId = setInterval(() => {}, 1000);

    component.pararMonitoramento();

    expect(component.monitoramentoAtivo).toBe(false);
    expect(component.intervalId).toBeNull();
  });

  it('should toggle monitoring state', () => {
    spyOn(component, 'iniciarMonitoramento');
    spyOn(component, 'pararMonitoramento');

    // Test starting monitoring
    component.monitoramentoAtivo = false;
    component.alternarMonitoramento();
    expect(component.iniciarMonitoramento).toHaveBeenCalled();

    // Test stopping monitoring
    component.monitoramentoAtivo = true;
    component.alternarMonitoramento();
    expect(component.pararMonitoramento).toHaveBeenCalled();
  });

  it('should mark alert as read', () => {
    component.alertas = [
      { id: 1, tipo: 'INFO', titulo: 'Teste', mensagem: 'Teste', timestamp: new Date(), lida: false }
    ];

    component.marcarAlertaComoLida(1);

    expect(component.alertas[0].lida).toBe(true);
  });

  it('should get correct risk color', () => {
    expect(component.obterCorRisco('BAIXO')).toBe('success');
    expect(component.obterCorRisco('MEDIO')).toBe('warning');
    expect(component.obterCorRisco('ALTO')).toBe('danger');
    expect(component.obterCorRisco('CRITICO')).toBe('danger');
    expect(component.obterCorRisco('UNKNOWN')).toBe('secondary');
  });

  it('should get correct risk icon', () => {
    expect(component.obterIconeRisco('BAIXO')).toBe('✓');
    expect(component.obterIconeRisco('MEDIO')).toBe('⚠');
    expect(component.obterIconeRisco('ALTO')).toBe('⚠');
    expect(component.obterIconeRisco('CRITICO')).toBe('⚠');
    expect(component.obterIconeRisco('UNKNOWN')).toBe('?');
  });

  it('should get correct integrity color', () => {
    expect(component.obterCorIntegridade('BOA')).toBe('success');
    expect(component.obterCorIntegridade('PRECISA_ATENCAO')).toBe('warning');
    expect(component.obterCorIntegridade('CRITICA')).toBe('danger');
    expect(component.obterCorIntegridade('UNKNOWN')).toBe('secondary');
  });

  it('should format percentage correctly', () => {
    expect(component.formatarPercentual(0.75)).toBe('75.0%');
    expect(component.formatarPercentual(0.123)).toBe('12.3%');
  });

  it('should format score correctly', () => {
    expect(component.formatarScore(0.12345)).toBe('0.12');
    expect(component.formatarScore(0.9876)).toBe('0.99');
  });

  it('should format date correctly', () => {
    const testDate = new Date('2023-01-01T12:00:00');
    const formatted = component.formatarData(testDate);
    expect(formatted).toContain('01/01/2023');
    expect(formatted).toContain('12:00');
  });

  it('should filter high risk users correctly', () => {
    component.usuariosAltoRisco = [
      { idUsuario: 1, emailUsuario: 'test1@test.com', nivelRisco: 'ALTO', pontuacaoRiscoAtual: 0.8, totalLogins: 10, atividadesSuspeitas: 2, transacoesNegadas: 1, totalTransacoes: 5, contaBloqueada: false },
      { idUsuario: 2, emailUsuario: 'test2@test.com', nivelRisco: 'CRITICO', pontuacaoRiscoAtual: 0.9, totalLogins: 15, atividadesSuspeitas: 5, transacoesNegadas: 3, totalTransacoes: 8, contaBloqueada: true }
    ];

    component.filtroRisco = 'TODOS';
    expect(component.obterUsuariosAltoRiscoFiltrados().length).toBe(2);

    component.filtroRisco = 'ALTO';
    expect(component.obterUsuariosAltoRiscoFiltrados().length).toBe(1);
    expect(component.obterUsuariosAltoRiscoFiltrados()[0].nivelRisco).toBe('ALTO');

    component.filtroRisco = 'CRITICO';
    expect(component.obterUsuariosAltoRiscoFiltrados().length).toBe(1);
    expect(component.obterUsuariosAltoRiscoFiltrados()[0].nivelRisco).toBe('CRITICO');
  });

  it('should count unread alerts correctly', () => {
    component.alertas = [
      { id: 1, tipo: 'INFO', titulo: 'Test1', mensagem: 'Test1', timestamp: new Date(), lida: false },
      { id: 2, tipo: 'WARNING', titulo: 'Test2', mensagem: 'Test2', timestamp: new Date(), lida: true },
      { id: 3, tipo: 'ERROR', titulo: 'Test3', mensagem: 'Test3', timestamp: new Date(), lida: false }
    ];

    expect(component.obterAlertasNaoLidos()).toBe(2);
  });

  it('should handle cleanup on destroy', () => {
    spyOn(component['subscriptions'], 'unsubscribe');
    spyOn(component, 'pararMonitoramento');

    component.ngOnDestroy();

    expect(component['subscriptions'].unsubscribe).toHaveBeenCalled();
    expect(component.pararMonitoramento).toHaveBeenCalled();
  });

  it('should handle error when analyzing user', () => {
    const userId = 1;
    const errorMessage = 'Erro de API';
    mockApiService.getUserRiskAssessment.and.returnValue(throwError(() => new Error(errorMessage)));

    component.analisarUsuario(userId);

    expect(component.error).toBe('Erro ao carregar dados do usuário');
    expect(component.loading).toBe(false);
  });

  it('should handle error when verifying transaction', () => {
    const hash = 'abc123def456';
    const errorMessage = 'Erro de verificação';
    mockApiService.verifyTransaction.and.returnValue(throwError(() => new Error(errorMessage)));

    component.verificarTransacao(hash);

    expect(component.error).toBe('Erro ao verificar transação');
  });
});
