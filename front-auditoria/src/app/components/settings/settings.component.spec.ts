import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { SettingsComponent } from './settings.component';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

describe('SettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;
  let mockApiService: jasmine.SpyObj<ApiService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockUserSettings = {
    id: 1,
    userId: 1,
    twoFactorEnabled: false,
    loginNotifications: true,
    suspiciousActivityAlerts: true,
    sessionTimeout: 30,
    passwordExpirationDays: 90,
    emailNotifications: true,
    pushNotifications: true,
    smsNotifications: false,
    securityAlerts: true,
    systemUpdates: true,
    profileVisibility: 'PRIVATE' as const,
    dataSharing: false,
    analyticsTracking: true,
    locationTracking: false,
    theme: 'AUTO' as const,
    language: 'PT_BR' as const,
    timezone: 'America/Sao_Paulo',
    dateFormat: 'DD/MM/YYYY' as const,
    apiRateLimit: 1000,
    apiKeyRotationDays: 30,
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const mockSecurityPolicy = {
    minPasswordLength: 8,
    requireSpecialChars: true,
    requireNumbers: true,
    requireUppercase: true,
    maxLoginAttempts: 5,
    lockoutDuration: 15
  };

  const mockSystemConfig = {
    maintenanceMode: false,
    maxConcurrentSessions: 10,
    sessionCleanupInterval: 60,
    logRetentionDays: 30,
    backupFrequency: 'DAILY' as const
  };

  const mockCurrentUser = {
    id: 1,
    name: 'João Silva',
    email: 'joao@exemplo.com',
    role: 'USER',
    createdAt: new Date()
  };

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiService', [
      'getUserSettings',
      'updateUserSettings',
      'getSecurityPolicy',
      'updateSecurityPolicy',
      'getSystemConfiguration',
      'updateSystemConfiguration',
      'regenerateApiKey',
      'testNotifications'
    ]);

    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getCurrentUser'
    ]);

    await TestBed.configureTestingModule({
      imports: [SettingsComponent, ReactiveFormsModule],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    mockApiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    mockAuthService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;

    // Setup default mocks
    mockAuthService.getCurrentUser.and.returnValue(mockCurrentUser);
    mockApiService.getUserSettings.and.returnValue(of(mockUserSettings));
    mockApiService.getSecurityPolicy.and.returnValue(of(mockSecurityPolicy));
    mockApiService.getSystemConfiguration.and.returnValue(of(mockSystemConfig));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Inicialização', () => {
    it('deve inicializar com valores padrão', () => {
      expect(component.loading).toBeFalse();
      expect(component.saving).toBeFalse();
      expect(component.error).toBeNull();
      expect(component.successMessage).toBeNull();
      expect(component.activeTab).toBe('personal');
      expect(component.userSettings).toBeNull();
      expect(component.securityPolicy).toBeNull();
      expect(component.systemConfig).toBeNull();
    });

    it('deve inicializar formulários com valores padrão', () => {
      expect(component.settingsForm).toBeDefined();
      expect(component.securityForm).toBeDefined();
      expect(component.systemForm).toBeDefined();
      
      expect(component.settingsForm.get('twoFactorEnabled')?.value).toBeFalse();
      expect(component.settingsForm.get('sessionTimeout')?.value).toBe(30);
      expect(component.settingsForm.get('theme')?.value).toBe('AUTO');
    });

    it('deve identificar usuário admin corretamente', () => {
      const adminUser = { ...mockCurrentUser, role: 'ADMIN' };
      mockAuthService.getCurrentUser.and.returnValue(adminUser);
      
      component.ngOnInit();
      
      expect(component.isAdmin).toBeTrue();
    });

    it('deve identificar usuário comum corretamente', () => {
      component.ngOnInit();
      
      expect(component.isAdmin).toBeFalse();
    });
  });

  describe('Carregamento de dados', () => {
    it('deve carregar configurações do usuário com sucesso', fakeAsync(() => {
      component.ngOnInit();
      tick();

      expect(mockApiService.getUserSettings).toHaveBeenCalled();
      expect(component.userSettings).toEqual(mockUserSettings);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeNull();
    }));

    it('deve carregar configurações padrão em caso de erro', fakeAsync(() => {
      mockApiService.getUserSettings.and.returnValue(throwError(() => new Error('API Error')));
      
      component.ngOnInit();
      tick();

      expect(component.userSettings).toBeDefined();
      expect(component.userSettings?.userId).toBe(mockCurrentUser.id);
      expect(component.loading).toBeFalse();
    }));

    it('deve carregar configurações de admin quando usuário é admin', fakeAsync(() => {
      const adminUser = { ...mockCurrentUser, role: 'ADMIN' };
      mockAuthService.getCurrentUser.and.returnValue(adminUser);
      
      component.ngOnInit();
      tick();

      expect(mockApiService.getSecurityPolicy).toHaveBeenCalled();
      expect(mockApiService.getSystemConfiguration).toHaveBeenCalled();
      expect(component.securityPolicy).toEqual(mockSecurityPolicy);
      expect(component.systemConfig).toEqual(mockSystemConfig);
    }));

    it('deve carregar configurações padrão de admin em caso de erro', fakeAsync(() => {
      const adminUser = { ...mockCurrentUser, role: 'ADMIN' };
      mockAuthService.getCurrentUser.and.returnValue(adminUser);
      mockApiService.getSecurityPolicy.and.returnValue(throwError(() => new Error('API Error')));
      mockApiService.getSystemConfiguration.and.returnValue(throwError(() => new Error('API Error')));
      
      component.ngOnInit();
      tick();

      expect(component.securityPolicy).toBeDefined();
      expect(component.systemConfig).toBeDefined();
    }));
  });

  describe('Salvamento de configurações', () => {
    beforeEach(() => {
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('deve salvar configurações do usuário com sucesso', fakeAsync(() => {
      const updatedSettings = { ...mockUserSettings, sessionTimeout: 60 };
      mockApiService.updateUserSettings.and.returnValue(of(updatedSettings));
      
      component.settingsForm.patchValue({ sessionTimeout: 60 });
      component.saveSettings();
      tick();

      expect(mockApiService.updateUserSettings).toHaveBeenCalled();
      expect(component.userSettings?.sessionTimeout).toBe(60);
      expect(component.successMessage).toContain('sucesso');
      expect(component.saving).toBeFalse();
    }));

    it('deve mostrar erro ao falhar no salvamento', fakeAsync(() => {
      mockApiService.updateUserSettings.and.returnValue(throwError(() => new Error('Save Error')));
      
      component.saveSettings();
      tick();

      expect(component.error).toContain('Erro');
      expect(component.saving).toBeFalse();
    }));

    it('não deve salvar se formulário inválido', () => {
      component.settingsForm.patchValue({ sessionTimeout: -1 });
      
      component.saveSettings();
      
      expect(mockApiService.updateUserSettings).not.toHaveBeenCalled();
    });

    it('deve salvar política de segurança (admin)', fakeAsync(() => {
      const adminUser = { ...mockCurrentUser, role: 'ADMIN' };
      mockAuthService.getCurrentUser.and.returnValue(adminUser);
      component.ngOnInit();
      tick();
      
      const updatedPolicy = { ...mockSecurityPolicy, minPasswordLength: 10 };
      mockApiService.updateSecurityPolicy.and.returnValue(of(updatedPolicy));
      
      component.securityForm.patchValue({ minPasswordLength: 10 });
      component.saveSecurityPolicy();
      tick();

      expect(mockApiService.updateSecurityPolicy).toHaveBeenCalled();
      expect(component.securityPolicy?.minPasswordLength).toBe(10);
      expect(component.successMessage).toContain('sucesso');
    }));

    it('deve salvar configuração do sistema (admin)', fakeAsync(() => {
      const adminUser = { ...mockCurrentUser, role: 'ADMIN' };
      mockAuthService.getCurrentUser.and.returnValue(adminUser);
      component.ngOnInit();
      tick();
      
      const updatedConfig = { ...mockSystemConfig, maxConcurrentSessions: 20 };
      mockApiService.updateSystemConfiguration.and.returnValue(of(updatedConfig));
      
      component.systemForm.patchValue({ maxConcurrentSessions: 20 });
      component.saveSystemConfiguration();
      tick();

      expect(mockApiService.updateSystemConfiguration).toHaveBeenCalled();
      expect(component.systemConfig?.maxConcurrentSessions).toBe(20);
      expect(component.successMessage).toContain('sucesso');
    }));
  });

  describe('Navegação por abas', () => {
    it('deve alternar entre abas corretamente', () => {
      component.switchTab('security');
      expect(component.activeTab).toBe('security');
      
      component.switchTab('notifications');
      expect(component.activeTab).toBe('notifications');
      
      component.switchTab('privacy');
      expect(component.activeTab).toBe('privacy');
      
      component.switchTab('advanced');
      expect(component.activeTab).toBe('advanced');
    });

    it('deve limpar mensagens ao trocar de aba', () => {
      component.error = 'Erro teste';
      component.successMessage = 'Sucesso teste';
      
      component.switchTab('security');
      
      expect(component.error).toBeNull();
      expect(component.successMessage).toBeNull();
    });
  });

  describe('Funcionalidades específicas', () => {
    beforeEach(() => {
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('deve restaurar configurações padrão', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      
      component.resetToDefaults();
      
      expect(component.userSettings).toBeDefined();
      expect(component.successMessage).toContain('padrão');
    });

    it('não deve restaurar se usuário cancelar', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      const originalSettings = component.userSettings;
      
      component.resetToDefaults();
      
      expect(component.userSettings).toBe(originalSettings);
    });

    it('deve exportar configurações', () => {
      const mockLink = jasmine.createSpyObj('a', ['click']);
      spyOn(document, 'createElement').and.returnValue(mockLink);
      spyOn(URL, 'createObjectURL').and.returnValue('blob:url');
      spyOn(URL, 'revokeObjectURL');
      
      component.exportSettings();
      
      expect(mockLink.click).toHaveBeenCalled();
      expect(component.successMessage).toContain('exportadas');
    });

    it('deve regenerar chave API', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockApiService.regenerateApiKey.and.returnValue(of({ success: true }));
      
      component.regenerateApiKey();
      tick();

      expect(mockApiService.regenerateApiKey).toHaveBeenCalled();
      expect(component.successMessage).toContain('chave');
    }));

    it('deve testar notificações', fakeAsync(() => {
      mockApiService.testNotifications.and.returnValue(of({ success: true }));
      
      component.testNotifications();
      tick();

      expect(mockApiService.testNotifications).toHaveBeenCalled();
      expect(component.successMessage).toContain('teste');
    }));

    it('deve habilitar 2FA', () => {
      component.enable2FA();
      
      expect(component.successMessage).toContain('2FA');
    });
  });

  describe('Importação de configurações', () => {
    it('deve importar configurações válidas', () => {
      const validSettings = {
        twoFactorEnabled: true,
        loginNotifications: false,
        theme: 'DARK',
        language: 'EN_US'
      };
      
      const mockFile = new Blob([JSON.stringify(validSettings)], { type: 'application/json' });
      const mockEvent = {
        target: {
          files: [mockFile]
        }
      };
      
      component.importSettings(mockEvent);
      
      // Simular leitura do arquivo
      const reader = new FileReader();
      reader.onload = jasmine.createSpy('onload');
      reader.readAsText(mockFile);
    });

    it('deve rejeitar configurações inválidas', () => {
      const invalidSettings = { invalid: true };
      
      spyOn(component as any, 'validateImportedSettings').and.returnValue(false);
      
      const mockFile = new Blob([JSON.stringify(invalidSettings)], { type: 'application/json' });
      const mockEvent = {
        target: {
          files: [mockFile]
        }
      };
      
      component.importSettings(mockEvent);
    });
  });

  describe('Validação de formulários', () => {
    it('deve validar campo inválido corretamente', () => {
      component.settingsForm.get('sessionTimeout')?.setValue(-1);
      component.settingsForm.get('sessionTimeout')?.markAsTouched();
      
      const isInvalid = component.isFieldInvalid('settings', 'sessionTimeout');
      
      expect(isInvalid).toBeTrue();
    });

    it('deve retornar mensagem de erro apropriada', () => {
      component.settingsForm.get('sessionTimeout')?.setValue(-1);
      component.settingsForm.get('sessionTimeout')?.markAsTouched();
      
      const errorMessage = component.getFieldError('settings', 'sessionTimeout');
      
      expect(errorMessage).toContain('mínimo');
    });

    it('deve retornar erro de campo obrigatório', () => {
      component.settingsForm.get('sessionTimeout')?.setValue(null);
      component.settingsForm.get('sessionTimeout')?.markAsTouched();
      
      const errorMessage = component.getFieldError('settings', 'sessionTimeout');
      
      expect(errorMessage).toContain('obrigatório');
    });
  });

  describe('Limpeza de recursos', () => {
    it('deve limpar subscriptions no ngOnDestroy', () => {
      spyOn(component['subscriptions'], 'unsubscribe');
      
      component.ngOnDestroy();
      
      expect(component['subscriptions'].unsubscribe).toHaveBeenCalled();
    });

    it('deve limpar mensagens automaticamente', fakeAsync(() => {
      component.error = 'Erro teste';
      component.successMessage = 'Sucesso teste';
      
      component['clearMessages']();
      tick(5000);
      
      expect(component.error).toBeNull();
      expect(component.successMessage).toBeNull();
    }));
  });

  describe('Métodos auxiliares', () => {
    it('deve marcar campos do formulário como touched', () => {
      const form = component.settingsForm;
      
      component['markFormGroupTouched'](form);
      
      Object.keys(form.controls).forEach(key => {
        expect(form.get(key)?.touched).toBeTrue();
      });
    });

    it('deve validar configurações importadas', () => {
      const validSettings = {
        twoFactorEnabled: true,
        loginNotifications: false,
        theme: 'DARK',
        language: 'EN_US'
      };
      
      const isValid = component['validateImportedSettings'](validSettings);
      
      expect(isValid).toBeTrue();
    });

    it('deve rejeitar configurações inválidas', () => {
      const invalidSettings = {
        invalid: true
      };
      
      const isValid = component['validateImportedSettings'](invalidSettings);
      
      expect(isValid).toBeFalse();
    });
  });
});
