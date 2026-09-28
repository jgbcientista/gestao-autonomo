import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { KeystrokeService } from '../../services/keystroke.service';
import { AuthenticationRequest } from '../../models/auth.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;

  // MFA
  showMfaChallenge = false;
  mfaCode = '';
  mfaEmail = '';
  mfaToken = '';
  mfaMessage = '';
  isValidatingMfa = false;
  appVersion: string = (environment as any).appVersion || '0.0.0';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private apiService: ApiService,
    private keystrokeService: KeystrokeService,
    private router: Router
  ) {}

  onPasswordFocus(): void {
    this.keystrokeService.iniciarCaptura();
  }

  onPasswordKeyDown(event: KeyboardEvent): void {
    this.keystrokeService.registrarKeyDown(event);
  }

  onPasswordKeyUp(event: KeyboardEvent): void {
    this.keystrokeService.registrarKeyUp(event);
  }

  ngOnInit(): void {
    this.initializeForm();

    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  private initializeForm(): void {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const loginData: AuthenticationRequest = {
        email: this.loginForm.value.email,
        password: this.loginForm.value.password
      };

      const enrichedLoginData = this.authService.enrichAuthRequest(loginData);

      this.authService.login(enrichedLoginData).subscribe({
        next: (response) => {
          this.isLoading = false;

          // Enviar keystroke data apos login bem-sucedido
          const keystrokeData = this.keystrokeService.finalizarCaptura();
          const userEmail = response.email || this.loginForm.value.email;
          if (keystrokeData.length > 0 && userEmail) {
            this.apiService.capturarKeystroke(userEmail, keystrokeData).subscribe({
              error: (err: any) => console.error('Erro ao enviar keystroke:', err)
            });
          }

          if (response.requiresMfa) {
            this.showMfaChallenge = true;
            this.mfaEmail = this.loginForm.value.email;
            this.mfaToken = response.mfaToken || '';
            this.mfaMessage = response.mfaMessage || 'Informe o código do Google Authenticator';
            return;
          }

          this.successMessage = `Bem-vindo, ${response.name}!`;
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Erro no login:', error);

          switch (error.status) {
            case 401:
              this.errorMessage = 'Email ou senha inválidos. Verifique suas credenciais.';
              break;
            case 423:
              this.errorMessage = 'Sua conta foi bloqueada. Entre em contato com o suporte.';
              break;
            case 403:
              if (error.error?.mfaMessage) {
                this.errorMessage = error.error.mfaMessage;
              } else {
                this.errorMessage = 'Acesso negado pela análise de segurança. Tente novamente.';
              }
              break;
            case 0:
              this.errorMessage = 'Erro de conexão. Verifique se o servidor está rodando.';
              break;
            default:
              this.errorMessage = 'Erro interno do servidor. Tente novamente em alguns instantes.';
          }
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  onSubmitMfa(): void {
    if (!this.mfaCode || this.mfaCode.length !== 6) {
      this.errorMessage = 'Informe o código de 6 dígitos do Google Authenticator.';
      return;
    }

    this.isValidatingMfa = true;
    this.errorMessage = '';

    this.authService.completeMfaLogin(this.mfaEmail, this.mfaCode, this.mfaToken).subscribe({
      next: (response) => {
        this.isValidatingMfa = false;
        this.successMessage = `Bem-vindo, ${response.name}!`;
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 1500);
      },
      error: (error) => {
        this.isValidatingMfa = false;
        console.error('Erro na validação MFA:', error);
        this.mfaCode = '';
        if (error.status === 401 && error.error?.error === 'Sessão de login expirada') {
          // desafio expirou ou excedeu as tentativas: recomeçar pelo e-mail e senha
          this.voltarLogin();
          this.errorMessage = 'Sua verificação expirou. Faça login novamente.';
          return;
        }
        this.errorMessage = 'Código MFA inválido. Tente novamente.';
      }
    });
  }

  voltarLogin(): void {
    this.showMfaChallenge = false;
    this.mfaCode = '';
    this.mfaEmail = '';
    this.mfaToken = '';
    this.errorMessage = '';
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  get f() {
    return this.loginForm.controls;
  }
}
