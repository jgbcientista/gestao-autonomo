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
      const user = JSON.parse(userStr);
      this.currentUserSubject.next(user);
      this.isAuthenticatedSubject.next(true);
    }
  }

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    // Enriquecer requisição com informações do cliente
    const enrichedCredentials = this.enrichAuthRequest(credentials);
    
    return this.apiService.login(enrichedCredentials).pipe(
      tap(response => {
        console.log('Resposta do login:', response);
        
        if (response && response.token && response.token.trim() !== '') {
          const user: User = {
            name: response.name || 'Usuário',
            email: response.email || credentials.email
          };
          
          if (this.isBrowser) {
            localStorage.setItem('auth_token', response.token);
            localStorage.setItem('current_user', JSON.stringify(user));
          }
          
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
          
          console.log('Login realizado com sucesso!', user);
        } else {
          console.warn('Token vazio recebido do servidor:', response);
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
          const user: User = {
            name: response.name || userDetails.name,
            email: response.email || userDetails.email
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
    if (this.isBrowser) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('current_user');
    }
    
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    
    this.router.navigate(['/login']);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  getToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem('auth_token');
  }

  private getClientInfo(): { ipAddress?: string; userAgent?: string; location?: string } {
    return {
      userAgent: navigator.userAgent,
      location: 'São Paulo, BR' // Padrão conforme API
    };
  }

  enrichAuthRequest(request: AuthenticationRequest): AuthenticationRequest {
    const clientInfo = this.getClientInfo();
    return {
      ...request,
      ...clientInfo
    };
  }
}
