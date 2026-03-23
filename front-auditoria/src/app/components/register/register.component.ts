import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { RegisterRequest } from '../../models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  // MFA Setup
  showMfaSetup = false;
  mfaQrCode = '';
  mfaSecret = '';
  mfaCode = '';
  mfaEmail = '';
  isVerifyingMfa = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    
    // Redireciona se já estiver logado
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  private initializeForm(): void {
    this.registerForm = this.formBuilder.group({
      name: ['', [
        Validators.required, 
        Validators.minLength(2), 
        Validators.maxLength(100)
      ]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { 
      validators: this.passwordMatchValidator 
    });
  }

  // Validador customizado para verificar se as senhas coincidem
  private passwordMatchValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { 'mismatch': true };
    }
    
    return null;
  }

  // Métodos para análise da força da senha
  getPasswordStrengthClass(): string {
    const password = this.registerForm.get('password')?.value || '';
    const strength = this.calculatePasswordStrength(password);
    
    if (strength < 30) return 'weak';
    if (strength < 70) return 'medium';
    return 'strong';
  }

  getPasswordStrengthText(): string {
    const password = this.registerForm.get('password')?.value || '';
    const strength = this.calculatePasswordStrength(password);
    
    if (strength < 30) return 'Fraca';
    if (strength < 70) return 'Média';
    return 'Forte';
  }

  getPasswordStrengthPercentage(): number {
    const password = this.registerForm.get('password')?.value || '';
    return this.calculatePasswordStrength(password);
  }

  private calculatePasswordStrength(password: string): number {
    let strength = 0;
    
    // Comprimento
    if (password.length >= 8) strength += 25;
    else if (password.length >= 6) strength += 15;
    
    // Letras minúsculas
    if (/[a-z]/.test(password)) strength += 15;
    
    // Letras maiúsculas
    if (/[A-Z]/.test(password)) strength += 15;
    
    // Números
    if (/[0-9]/.test(password)) strength += 15;
    
    // Símbolos
    if (/[^A-Za-z0-9]/.test(password)) strength += 15;
    
    // Variedade de caracteres
    const uniqueChars = new Set(password).size;
    if (uniqueChars >= 6) strength += 15;
    else if (uniqueChars >= 4) strength += 10;
    else if (uniqueChars >= 2) strength += 5;
    
    return Math.min(100, strength);
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const registerData: RegisterRequest = {
        name: this.registerForm.value.name,
        email: this.registerForm.value.email,
        password: this.registerForm.value.password
      };

      this.authService.register(registerData).subscribe({
        next: (response) => {
          this.isLoading = false;

          // Se o backend retornou QR code MFA, mostrar tela de configuração
          if (response.mfaQrCode) {
            this.showMfaSetup = true;
            this.mfaQrCode = response.mfaQrCode;
            this.mfaSecret = response.mfaSecret || '';
            this.mfaEmail = registerData.email;
            this.successMessage = 'Conta criada! Configure a autenticação em duas etapas.';
            return;
          }

          this.successMessage = `Conta criada com sucesso! Bem-vindo, ${response.name}!`;
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 2000);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Erro no registro:', error);

          switch (error.status) {
            case 400:
              if (error.error?.message?.includes('email')) {
                this.errorMessage = 'Dados inválidos. Verifique o formato do email.';
              } else {
                this.errorMessage = 'Dados inválidos. Verifique os campos preenchidos.';
              }
              break;
            case 409:
              this.errorMessage = 'Este email já está cadastrado. Tente fazer login ou use outro email.';
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

  private markFormGroupTouched(): void {
    Object.keys(this.registerForm.controls).forEach(key => {
      const control = this.registerForm.get(key);
      control?.markAsTouched();
    });
  }

  onVerifyMfa(): void {
    if (!this.mfaCode || this.mfaCode.length !== 6) {
      this.errorMessage = 'Informe o código de 6 dígitos do Google Authenticator.';
      return;
    }

    this.isVerifyingMfa = true;
    this.errorMessage = '';

    this.apiService.verificarConfiguracaoMfa(this.mfaEmail, this.mfaCode).subscribe({
      next: (response) => {
        this.isVerifyingMfa = false;
        this.successMessage = 'MFA configurado com sucesso! Redirecionando para o login...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2500);
      },
      error: (error) => {
        this.isVerifyingMfa = false;
        console.error('Erro na verificação MFA:', error);
        this.errorMessage = 'Código inválido. Verifique o Google Authenticator e tente novamente.';
        this.mfaCode = '';
      }
    });
  }

  get f() {
    return this.registerForm.controls;
  }
}
