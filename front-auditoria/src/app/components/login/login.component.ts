import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { AuthenticationRequest } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
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

      // Enriquece a requisição com informações do cliente
      const enrichedLoginData = this.authService.enrichAuthRequest(loginData);

      this.authService.login(enrichedLoginData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = `Bem-vindo, ${response.name}!`;
          
          // Redireciona para o dashboard após sucesso
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Erro no login:', error);
          
          // Mapeia os códigos de erro HTTP para mensagens amigáveis
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

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  // Método para facilitar acesso aos controles do form no template
  get f() {
    return this.loginForm.controls;
  }
}
