import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { AuthenticationRequest } from '../../models/auth.model';

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
  mfaMessage = '';
  isValidatingMfa = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

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

          if (response.requiresMfa) {
            this.showMfaChallenge = true;
            this.mfaEmail = this.loginForm.value.email;
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
              this.errorMessage = 'Acesso negado pela análise de segurança. Tente novamente.';
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

    this.authService.completeMfaLogin(this.mfaEmail, this.mfaCode).subscribe({
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
        this.errorMessage = 'Código MFA inválido. Tente novamente.';
        this.mfaCode = '';
      }
    });
  }

  voltarLogin(): void {
    this.showMfaChallenge = false;
    this.mfaCode = '';
    this.mfaEmail = '';
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
