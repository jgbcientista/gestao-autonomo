import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { ApiService } from './api.service';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest, User } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private isBrowser: boolean;

  constructor(
    private apiService: ApiService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    this.checkAuthStatus();
  }

  private checkAuthStatus(): void {
    if (!this.isBrowser) {
      return;
    }

    const token = localStorage.getItem('auth_token');
    const userStr = localStorage.getItem('current_user');
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        console.log('🔐 Verificando status de autenticação:', {
          token: token.substring(0, 10) + '...',
          user
        });
        
        // Se o usuário não tem role definido ou é USER, re-determinar baseado no email/nome
        if (!user.role || user.role === 'USER') {
          user.role = this.determineUserRole(user.email, user);
          console.log('👤 Role redeterminada:', user.role);
          
          // Atualizar localStorage com novo role
          if (this.isBrowser) {
            localStorage.setItem('current_user', JSON.stringify(user));
          }
        }
        
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
        
        // Validar token no backend
        this.validateTokenWithBackend(token);
      } catch (error) {
        console.error('❌ Erro ao processar dados do usuário:', error);
        this.logout();
      }
    } else {
      console.warn('⚠️ Nenhum token ou usuário encontrado');
      this.logout();
    }
  }

  private validateTokenWithBackend(token: string): void {
    this.apiService.validateToken(token).subscribe({
      next: (isValid) => {
        console.log('🔒 Token validado:', isValid);
        if (!isValid) {
          console.warn('⚠️ Token inválido, fazendo logout');
          this.logout();
        }
      },
      error: (err) => {
        console.error('❌ Erro ao validar token:', err);
        this.logout();
      }
    });
  }

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    console.log('🔑 Iniciando login para:', credentials.email);
    
    // Enriquecer requisição com informações do cliente
    const enrichedCredentials = this.enrichAuthRequest(credentials);
    
    return this.apiService.login(enrichedCredentials).pipe(
      tap(response => {
        console.log('✅ Resposta do login:', {
          token: response.token ? 'presente' : 'ausente',
          name: response.name,
          email: response.email,
          role: response.role
        });
        
        if (response && response.token && response.token.trim() !== '') {
          // Usar role da resposta da API
          const userRole = response.role || this.determineUserRole(response.email || credentials.email, response);
          console.log('👤 Role:', userRole);
          
          const user: User = {
            name: response.name || 'Usuário',
            email: response.email || credentials.email,
            role: userRole
          };
          
          if (this.isBrowser) {
            localStorage.setItem('auth_token', response.token);
            localStorage.setItem('current_user', JSON.stringify(user));
          }
          
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
          
          console.log('✨ Login realizado com sucesso!', user);
        } else {
          console.warn('⚠️ Token vazio recebido do servidor:', response);
          throw new Error('Token não fornecido pelo servidor');
        }
      })
    );
  }

  register(userDetails: RegisterRequest): Observable<AuthenticationResponse> {
    return this.apiService.register(userDetails).pipe(
      tap(response => {
        console.log('Resposta do registro:', response);
        
        if (response && response.token && response.token.trim() !== '') {
          // Determinar role do usuário
          const userRole = this.determineUserRole(response.email || userDetails.email, response);
          
          const user: User = {
            name: response.name || userDetails.name,
            email: response.email || userDetails.email,
            role: userRole
          };
          
          if (this.isBrowser) {
            localStorage.setItem('auth_token', response.token);
            localStorage.setItem('current_user', JSON.stringify(user));
          }
          
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
          
          console.log('Registro realizado com sucesso!', user);
        } else {
          console.warn('Token vazio recebido do servidor no registro:', response);
          throw new Error('Token não fornecido pelo servidor durante o registro');
        }
      })
    );
  }

  logout(): void {
    console.log('🚪 Realizando logout');
    
    if (this.isBrowser) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('current_user');
    }
    
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    
    console.log('✅ Logout concluído');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem('auth_token') : null;
  }

  private getDeviceInfo(): string {
    return navigator.userAgent;
  }

  enrichAuthRequest(request: any): any {
    return {
      ...request,
      deviceInfo: this.getDeviceInfo(),
      timestamp: new Date().toISOString()
    };
  }

  private determineUserRole(email: string, response: any): string {
    console.log('🔍 Determinando role para:', email);
    
    // Se a resposta da API contém role, usar ela
    if (response && response.role) {
      console.log('✅ Role encontrada na resposta da API:', response.role);
      return response.role;
    }
    
    // Se não tem role na resposta, usar role do token JWT
    const token = this.getToken();
    if (token) {
      try {
        const tokenData = JSON.parse(atob(token.split('.')[1]));
        if (tokenData && tokenData.role) {
          console.log('✅ Role encontrada no token JWT:', tokenData.role);
          return tokenData.role;
        }
      } catch (error) {
        console.error('❌ Erro ao decodificar token:', error);
      }
    }
    
    // Se não encontrou role, assumir USER
    console.warn('⚠️ Role não encontrada, assumindo USER');
    return 'USER';
  }

  isAdmin(): boolean {
    const currentUser = this.getCurrentUser();
    return currentUser?.role === 'ADMIN';
  }

  hasRole(role: string): boolean {
    const currentUser = this.getCurrentUser();
    return currentUser?.role === role;
  }

  refreshToken(): Observable<any> {
    const token = this.getToken();
    if (!token) {
      console.warn('⚠️ Nenhum token para renovar');
      return new Observable(subscriber => {
        subscriber.error('Nenhum token disponível');
      });
    }

    return this.apiService.refreshToken(token).pipe(
      tap(newToken => {
        if (newToken) {
          console.log('🔄 Token renovado');
          if (this.isBrowser) {
            localStorage.setItem('auth_token', newToken);
          }
        } else {
          console.warn('⚠️ Renovação de token falhou');
          this.logout();
        }
      })
    );
  }
}
