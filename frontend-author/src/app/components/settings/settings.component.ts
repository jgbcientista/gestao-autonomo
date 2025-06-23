import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { Subscription } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface UserSettings {
  id?: number;
  userId: number;
  // Configurações de Segurança
  twoFactorEnabled: boolean;
  loginNotifications: boolean;
  suspiciousActivityAlerts: boolean;
  sessionTimeout: number; // em minutos
  passwordExpirationDays: number;
  
  // Configurações de Notificações
  emailNotifications: boolean;
  pushNotifications: boolean;
  smsNotifications: boolean;
  securityAlerts: boolean;
  systemUpdates: boolean;
  
  // Configurações de Privacidade
  profileVisibility: 'PUBLIC' | 'PRIVATE' | 'RESTRICTED';
  dataSharing: boolean;
  analyticsTracking: boolean;
  locationTracking: boolean;
  
  // Configurações Avançadas
  theme: 'LIGHT' | 'DARK' | 'AUTO';
  language: 'PT_BR' | 'EN_US' | 'ES_ES';
  timezone: string;
  dateFormat: 'DD/MM/YYYY' | 'MM/DD/YYYY' | 'YYYY-MM-DD';
  
  // Configurações de API
  apiRateLimit: number;
  apiKeyRotationDays: number;
  
  createdAt?: Date;
  updatedAt?: Date;
}

interface SecurityPolicy {
  minPasswordLength: number;
  requireSpecialChars: boolean;
  requireNumbers: boolean;
  requireUppercase: boolean;
  maxLoginAttempts: number;
  lockoutDuration: number; // em minutos
}

interface SystemConfiguration {
  maintenanceMode: boolean;
  maxConcurrentSessions: number;
  sessionCleanupInterval: number;
  logRetentionDays: number;
  backupFrequency: 'DAILY' | 'WEEKLY' | 'MONTHLY';
}

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent implements OnInit, OnDestroy {
  loading = false;
  saving = false;
  error: string | null = null;
  successMessage: string | null = null;
  
  // Dados principais
  userSettings: UserSettings | null = null;
  securityPolicy: SecurityPolicy | null = null;
  systemConfig: SystemConfiguration | null = null;
  
  // Formulários
  settingsForm!: FormGroup;
  securityForm!: FormGroup;
  systemForm!: FormGroup;
  
  // Controles de visualização
  activeTab = 'personal';
  
  // Subscriptions
  private subscriptions = new Subscription();
  
  // Usuário atual
  currentUser: any = null;
  isAdmin = false;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private location: Location
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    console.log('🚀 SettingsComponent ngOnInit iniciado');
    this.initializeForms();
    this.currentUser = this.authService.getCurrentUser();
    console.log('👤 Current user:', this.currentUser);
    this.isAdmin = this.currentUser?.role === 'ADMIN';
    console.log('🔐 Is admin:', this.isAdmin);
    this.loadSettings();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private initializeForms(): void {
    // Formulário de configurações pessoais
    this.settingsForm = this.fb.group({
      // Segurança
      twoFactorEnabled: [false],
      loginNotifications: [true],
      suspiciousActivityAlerts: [true],
      sessionTimeout: [30, [Validators.required, Validators.min(5), Validators.max(480)]],
      passwordExpirationDays: [90, [Validators.required, Validators.min(30), Validators.max(365)]],
      
      // Notificações
      emailNotifications: [true],
      pushNotifications: [true],
      smsNotifications: [false],
      securityAlerts: [true],
      systemUpdates: [true],
      
      // Privacidade
      profileVisibility: ['PRIVATE'],
      dataSharing: [false],
      analyticsTracking: [true],
      locationTracking: [false],
      
      // Avançadas
      theme: ['AUTO'],
      language: ['PT_BR'],
      timezone: ['America/Sao_Paulo'],
      dateFormat: ['DD/MM/YYYY'],
      
      // API
      apiRateLimit: [1000, [Validators.required, Validators.min(100), Validators.max(10000)]],
      apiKeyRotationDays: [30, [Validators.required, Validators.min(7), Validators.max(365)]]
    });

    // Formulário de política de segurança (apenas admin)
    this.securityForm = this.fb.group({
      minPasswordLength: [8, [Validators.required, Validators.min(6), Validators.max(32)]],
      requireSpecialChars: [true],
      requireNumbers: [true],
      requireUppercase: [true],
      maxLoginAttempts: [5, [Validators.required, Validators.min(3), Validators.max(10)]],
      lockoutDuration: [15, [Validators.required, Validators.min(5), Validators.max(60)]]
    });

    // Formulário de configuração do sistema (apenas admin)
    this.systemForm = this.fb.group({
      maintenanceMode: [false],
      maxConcurrentSessions: [10, [Validators.required, Validators.min(1), Validators.max(100)]],
      sessionCleanupInterval: [60, [Validators.required, Validators.min(30), Validators.max(1440)]],
      logRetentionDays: [30, [Validators.required, Validators.min(7), Validators.max(365)]],
      backupFrequency: ['DAILY']
    });
  }

  loadSettings(): void {
    console.log('🔄 loadSettings() iniciado');
    this.loading = true;
    this.error = null;

    // Tentar carregar configurações reais da API
    console.log('🌐 Chamando getUserSettings API...');
    const userSettingsObs = this.apiService.getUserSettings();
    
    const subscription = userSettingsObs.subscribe({
      next: (settings) => {
        console.log('✅ Configurações carregadas da API:', settings);
        this.userSettings = settings;
        this.populateForm(settings);
        this.loading = false;
        
        // Se for admin, carregar configurações do sistema
        if (this.isAdmin) {
          console.log('🔧 Carregando configurações de admin...');
          this.loadAdminSettings();
        }
      },
      error: (error) => {
        console.error('❌ Erro ao carregar configurações:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        console.warn('⚠️ Fallback para configurações padrão devido ao erro:', error.message);
        this.loadDefaultSettings();
        this.loading = false;
      }
    });

    this.subscriptions.add(subscription);
  }

  private loadAdminSettings(): void {
    // Carregar política de segurança
    const securityObs = this.apiService.getSecurityPolicy();
    const securitySub = securityObs.subscribe({
      next: (policy) => {
        this.securityPolicy = policy;
        this.securityForm.patchValue(policy);
      },
      error: (error) => {
        console.error('Erro ao carregar política de segurança:', error);
        this.loadDefaultSecurityPolicy();
      }
    });

    // Carregar configuração do sistema
    const systemObs = this.apiService.getSystemConfiguration();
    const systemSub = systemObs.subscribe({
      next: (config) => {
        this.systemConfig = config;
        this.systemForm.patchValue(config);
      },
      error: (error) => {
        console.error('Erro ao carregar configuração do sistema:', error);
        this.loadDefaultSystemConfig();
      }
    });

    this.subscriptions.add(securitySub);
    this.subscriptions.add(systemSub);
  }

  private loadDefaultSettings(): void {
    const defaultSettings: UserSettings = {
      userId: this.currentUser?.id || 0,
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
      profileVisibility: 'PRIVATE',
      dataSharing: false,
      analyticsTracking: true,
      locationTracking: false,
      theme: 'AUTO',
      language: 'PT_BR',
      timezone: 'America/Sao_Paulo',
      dateFormat: 'DD/MM/YYYY',
      apiRateLimit: 1000,
      apiKeyRotationDays: 30
    };

    this.userSettings = defaultSettings;
    this.populateForm(defaultSettings);
  }

  private loadDefaultSecurityPolicy(): void {
    const defaultPolicy: SecurityPolicy = {
      minPasswordLength: 8,
      requireSpecialChars: true,
      requireNumbers: true,
      requireUppercase: true,
      maxLoginAttempts: 5,
      lockoutDuration: 15
    };

    this.securityPolicy = defaultPolicy;
    this.securityForm.patchValue(defaultPolicy);
  }

  private loadDefaultSystemConfig(): void {
    const defaultConfig: SystemConfiguration = {
      maintenanceMode: false,
      maxConcurrentSessions: 10,
      sessionCleanupInterval: 60,
      logRetentionDays: 30,
      backupFrequency: 'DAILY'
    };

    this.systemConfig = defaultConfig;
    this.systemForm.patchValue(defaultConfig);
  }

  private populateForm(settings: UserSettings): void {
    this.settingsForm.patchValue(settings);
  }

  saveSettings(): void {
    console.log('🔧 saveSettings() chamado');
    
    if (this.settingsForm.invalid) {
      console.log('❌ Formulário inválido:', this.settingsForm.errors);
      this.markFormGroupTouched(this.settingsForm);
      return;
    }

    this.saving = true;
    this.error = null;
    this.successMessage = null;

    const formData = this.settingsForm.value;
    console.log('📝 Form data:', formData);
    
    const settingsToSave: UserSettings = {
      ...this.userSettings,
      ...formData,
      userId: this.currentUser?.id || 0
    };
    
    console.log('💾 Settings to save:', settingsToSave);
    console.log('🌐 Calling API updateUserSettings...');

    const subscription = this.apiService.updateUserSettings(settingsToSave).subscribe({
      next: (updatedSettings) => {
        console.log('✅ Settings saved successfully:', updatedSettings);
        this.userSettings = updatedSettings;
        this.saving = false;
        this.successMessage = 'Configurações salvas com sucesso!';
        this.clearMessages();
      },
      error: (error) => {
        console.error('❌ Erro ao salvar configurações:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        if (error.error) {
          console.error('Error details:', error.error);
        }
        this.error = `Erro ao salvar configurações: ${error.status || 'Erro desconhecido'}`;
        this.saving = false;
        this.clearMessages();
      }
    });

    this.subscriptions.add(subscription);
  }

  saveSecurityPolicy(): void {
    if (!this.isAdmin || this.securityForm.invalid) {
      this.markFormGroupTouched(this.securityForm);
      return;
    }

    this.saving = true;
    this.error = null;
    this.successMessage = null;

    const policyData = this.securityForm.value;

    const subscription = this.apiService.updateSecurityPolicy(policyData).subscribe({
      next: (updatedPolicy) => {
        this.securityPolicy = updatedPolicy;
        this.saving = false;
        this.successMessage = 'Política de segurança atualizada com sucesso!';
        this.clearMessages();
      },
      error: (error) => {
        console.error('Erro ao salvar política de segurança:', error);
        this.error = 'Erro ao salvar política de segurança. Tente novamente.';
        this.saving = false;
        this.clearMessages();
      }
    });

    this.subscriptions.add(subscription);
  }

  saveSystemConfiguration(): void {
    if (!this.isAdmin || this.systemForm.invalid) {
      this.markFormGroupTouched(this.systemForm);
      return;
    }

    this.saving = true;
    this.error = null;
    this.successMessage = null;

    const configData = this.systemForm.value;

    const subscription = this.apiService.updateSystemConfiguration(configData).subscribe({
      next: (updatedConfig) => {
        this.systemConfig = updatedConfig;
        this.saving = false;
        this.successMessage = 'Configuração do sistema atualizada com sucesso!';
        this.clearMessages();
      },
      error: (error) => {
        console.error('Erro ao salvar configuração do sistema:', error);
        this.error = 'Erro ao salvar configuração do sistema. Tente novamente.';
        this.saving = false;
        this.clearMessages();
      }
    });

    this.subscriptions.add(subscription);
  }

  resetToDefaults(): void {
    if (confirm('Tem certeza que deseja restaurar as configurações padrão? Esta ação não pode ser desfeita.')) {
      this.loadDefaultSettings();
      this.successMessage = 'Configurações restauradas para os valores padrão.';
      this.clearMessages();
    }
  }

  exportSettings(): void {
    if (!this.userSettings) return;

    const dataStr = JSON.stringify(this.userSettings, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = `configuracoes-${this.currentUser?.email || 'usuario'}-${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    
    URL.revokeObjectURL(url);
    this.successMessage = 'Configurações exportadas com sucesso!';
    this.clearMessages();
  }

  importSettings(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const importedSettings = JSON.parse(e.target?.result as string);
        
        // Validar estrutura básica
        if (this.validateImportedSettings(importedSettings)) {
          this.settingsForm.patchValue(importedSettings);
          this.successMessage = 'Configurações importadas com sucesso! Clique em Salvar para aplicar.';
          this.clearMessages();
        } else {
          this.error = 'Arquivo de configurações inválido.';
          this.clearMessages();
        }
      } catch (error) {
        this.error = 'Erro ao ler arquivo de configurações.';
        this.clearMessages();
      }
    };
    reader.readAsText(file);
  }

  private validateImportedSettings(settings: any): boolean {
    // Validação básica da estrutura
    const requiredFields = ['twoFactorEnabled', 'loginNotifications', 'theme', 'language'];
    return requiredFields.every(field => settings.hasOwnProperty(field));
  }

  switchTab(tab: string): void {
    this.activeTab = tab;
    this.error = null;
    this.successMessage = null;
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  private clearMessages(): void {
    setTimeout(() => {
      this.error = null;
      this.successMessage = null;
    }, 5000);
  }

  // Métodos auxiliares para o template
  isFieldInvalid(formName: string, fieldName: string): boolean {
    const form = formName === 'settings' ? this.settingsForm : 
                 formName === 'security' ? this.securityForm : this.systemForm;
    const field = form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(formName: string, fieldName: string): string {
    const form = formName === 'settings' ? this.settingsForm : 
                 formName === 'security' ? this.securityForm : this.systemForm;
    const field = form.get(fieldName);
    
    if (field?.errors) {
      if (field.errors['required']) return 'Este campo é obrigatório';
      if (field.errors['min']) return `Valor mínimo: ${field.errors['min'].min}`;
      if (field.errors['max']) return `Valor máximo: ${field.errors['max'].max}`;
    }
    
    return '';
  }

  // Métodos para ações específicas
  enable2FA(): void {
    // Implementar lógica de habilitação de 2FA
    this.successMessage = '2FA será habilitado após salvar as configurações.';
    this.clearMessages();
  }

  regenerateApiKey(): void {
    if (confirm('Tem certeza que deseja regenerar sua chave de API? A chave atual será invalidada.')) {
      const subscription = this.apiService.regenerateApiKey().subscribe({
        next: (response) => {
          this.successMessage = 'Nova chave de API gerada com sucesso!';
          this.clearMessages();
        },
        error: (error) => {
          this.error = 'Erro ao regenerar chave de API.';
          this.clearMessages();
        }
      });
      
      this.subscriptions.add(subscription);
    }
  }

  testNotifications(): void {
    const subscription = this.apiService.testNotifications().subscribe({
      next: (response) => {
        this.successMessage = 'Notificação de teste enviada!';
        this.clearMessages();
      },
      error: (error) => {
        this.error = 'Erro ao enviar notificação de teste.';
        this.clearMessages();
      }
    });
    
    this.subscriptions.add(subscription);
  }

  // Método para voltar à página anterior
  goBack(): void {
    console.log('🔙 Voltando à página anterior');
    // Primeiro tenta voltar usando o histórico do navegador
    if (window.history.length > 1) {
      this.location.back();
    } else {
      // Se não há histórico, vai para o dashboard
      this.router.navigate(['/dashboard']);
    }
  }
} 