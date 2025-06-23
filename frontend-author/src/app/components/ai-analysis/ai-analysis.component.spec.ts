import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { AiAnalysisComponent } from './ai-analysis.component';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

describe('AiAnalysisComponent', () => {
  let component: AiAnalysisComponent;
  let fixture: ComponentFixture<AiAnalysisComponent>;
  let mockApiService: jasmine.SpyObj<ApiService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    // Criar mocks dos serviços
    mockApiService = jasmine.createSpyObj('ApiService', [
      'analyzeUserBehavior',
      'classifyAccess',
      'trainAIModels',
      'sendAIFeedback',
      'getUserAnalysisHistory',
      'getAnomalyStatistics',
      'calculateAnomalyScore'
    ]);

    mockAuthService = jasmine.createSpyObj('AuthService', [
      'getCurrentUser',
      'isAuthenticated'
    ]);

    // Configurar retornos dos mocks
    mockApiService.getAnomalyStatistics.and.returnValue(of({
      totalAnalises: 100,
      totalAnomalias: 15,
      percentualAnomalias: 0.15,
      distribuicaoClassificacoes: {
        'ESPERADO': 85,
        'SUSPEITO': 10,
        'ANOMALO': 5
      },
      precisaoModelo: 0.92,
      ultimoTreinamento: new Date(),
      modelosAtivos: ['IsolationForest', 'RandomForest', 'DeepLearning']
    }));

    mockApiService.getUserAnalysisHistory.and.returnValue(of([]));

    mockApiService.analyzeUserBehavior.and.returnValue(of({
      id: 1,
      usuarioId: 1,
      classificacao: 'ESPERADO',
      scoreAnomaliaGlobal: 0.2,
      confiabilidade: 0.95,
      scoresComportamentais: {
        'horario': 0.1,
        'localizacao': 0.15,
        'dispositivo': 0.05
      },
      dadosContexto: {
        enderecoIp: '192.168.1.100',
        userAgent: 'Test Agent',
        localizacaoGeografica: 'São Paulo, BR',
        timezone: 'America/Sao_Paulo',
        idiomaBrowser: 'pt-BR',
        resolucaoTela: '1920x1080',
        tentativasLogin: 1
      },
      timestamp: new Date(),
      modelosUtilizados: ['IsolationForest', 'RandomForest']
    }));

    mockApiService.classifyAccess.and.returnValue(of({
      classificacao: 'ESPERADO',
      descricao: 'Acesso dentro do padrão esperado',
      nivelRisco: 'BAIXO'
    }));

    mockAuthService.getCurrentUser.and.returnValue({
      name: 'Test User',
      email: 'test@example.com'
    });

    mockAuthService.isAuthenticated.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [
        AiAnalysisComponent,
        HttpClientTestingModule,
        FormsModule
      ],
      providers: [
        { provide: ApiService, useValue: mockApiService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AiAnalysisComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
    expect(component.monitoramentoAtivo).toBeFalse();
    expect(component.feedbackAtivo).toBeFalse();
  });

  it('should load data on init', () => {
    fixture.detectChanges(); // Trigger ngOnInit
    
    expect(mockApiService.getAnomalyStatistics).toHaveBeenCalled();
    expect(mockApiService.getUserAnalysisHistory).toHaveBeenCalled();
  });

  it('should execute complete analysis', () => {
    component.executarAnaliseCompleta();
    
    expect(mockApiService.analyzeUserBehavior).toHaveBeenCalled();
    expect(mockApiService.classifyAccess).toHaveBeenCalled();
  });

  it('should toggle monitoring', () => {
    expect(component.monitoramentoAtivo).toBeFalse();
    
    component.alternarMonitoramento();
    expect(component.monitoramentoAtivo).toBeTrue();
    
    component.alternarMonitoramento();
    expect(component.monitoramentoAtivo).toBeFalse();
  });

  it('should calculate detailed scores', () => {
    mockApiService.calculateAnomalyScore.and.returnValue(of({
      isolationForest: 0.25,
      randomForest: 0.30,
      deepLearning: 0.28,
      ensemble: 0.27,
      detalhes: {
        'isolation_forest': 0.25,
        'random_forest': 0.30,
        'deep_learning': 0.28,
        'ensemble': 0.27
      }
    }));

    component.calcularScoresDetalhados();
    
    expect(mockApiService.calculateAnomalyScore).toHaveBeenCalled();
  });

  it('should train models', () => {
    mockApiService.trainAIModels.and.returnValue(of({}));
    
    component.treinarModelos();
    
    expect(mockApiService.trainAIModels).toHaveBeenCalled();
  });

  it('should send feedback', () => {
    mockApiService.sendAIFeedback.and.returnValue(of({
      status: 'sucesso',
      mensagem: 'Feedback registrado com sucesso'
    }));

    component.enviarFeedback(1, true);
    
    expect(mockApiService.sendAIFeedback).toHaveBeenCalledWith(1, {
      acessoLegitimo: true,
      comentario: ''
    });
  });

  it('should get correct classification color', () => {
    expect(component.obterCorClassificacao('ESPERADO')).toBe('success');
    expect(component.obterCorClassificacao('SUSPEITO')).toBe('warning');
    expect(component.obterCorClassificacao('ANOMALO')).toBe('danger');
    expect(component.obterCorClassificacao('ALTAMENTE_SUSPEITO')).toBe('danger');
  });

  it('should get correct classification icon', () => {
    expect(component.obterIconeClassificacao('ESPERADO')).toBe('✓');
    expect(component.obterIconeClassificacao('SUSPEITO')).toBe('⚠');
    expect(component.obterIconeClassificacao('ANOMALO')).toBe('⚠');
    expect(component.obterIconeClassificacao('ALTAMENTE_SUSPEITO')).toBe('⚠');
  });

  it('should format percentage correctly', () => {
    expect(component.formatarPercentual(0.85)).toBe('85.0%');
    expect(component.formatarPercentual(0.1234)).toBe('12.3%');
  });

  it('should format score correctly', () => {
    expect(component.formatarScore(0.85432)).toBe('0.854');
    expect(component.formatarScore(0.1)).toBe('0.100');
  });

  it('should handle user not authenticated', () => {
    mockAuthService.getCurrentUser.and.returnValue(null);
    
    fixture.detectChanges();
    
    expect(component.error).toBe('Usuário não autenticado');
  });

  it('should clean up on destroy', () => {
    component.ngOnInit();
    spyOn(component['subscriptions'], 'unsubscribe');
    spyOn(component, 'pararMonitoramento');
    
    component.ngOnDestroy();
    
    expect(component['subscriptions'].unsubscribe).toHaveBeenCalled();
    expect(component.pararMonitoramento).toHaveBeenCalled();
  });
});
