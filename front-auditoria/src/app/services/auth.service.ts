import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable, tap, of } from 'rxjs';
import { Router } from '@angular/router';
import { ApiService } from './api.service';
import { KeycloakService } from './keycloak.service';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest, User } from '../models/auth.model';
import { environment } from '../../environments/environment';

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
    private keycloakService: KeycloakService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    this.checkAuthStatus();
  }

  private async checkAuthStatus(): Promise<void> {
    if (!this.isBrowser) {
      return;
    }

    // Modo Keycloak
    if (environment.useKeycloak) {
      const authenticated = await this.keycloakService.init();
      if (authenticated) {
        const profile = this.keycloakService.getUserProfile();
        const user: User = {
          name: profile?.name || profile?.preferred_username || 'Usuário',
          email: profile?.email || '',
          role: this.keycloakService.isAdmin() ? 'ADMIN' : 'USER'
        };
        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
      }
      return;
    }

    // Modo JWT customizado (original)
    const token = localStorage.getItem('auth_token');
    const userStr = localStorage.getItem('current_user');

    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        console.log('Verificando status de autenticação:', {
          token: token.substring(0, 10) + '...',
          user
        });

        if (!user.role || user.role === 'USER') {
          user.role = this.determineUserRole(user.email, user);
          if (this.isBrowser) {
            localStorage.setItem('current_user', JSON.stringify(user));
          }
        }

        this.currentUserSubject.next(user);
        this.isAuthenticatedSubject.next(true);
        this.validateTokenWithBackend(token);
      } catch (error) {
        console.error('Erro ao processar dados do usuário:', error);
        this.logout();
      }
    } else {
      this.logout();
    }
  }

  private validateTokenWithBackend(token: string): void {
    this.apiService.validateToken(token).subscribe({
      next: (isValid) => {
        if (!isValid) {
          this.logout();
        }
      },
      error: () => {
        this.logout();
      }
    });
  }

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    // Se Keycloak está ativo, delegar para Keycloak
    if (environment.useKeycloak) {
      this.keycloakService.login();
      return of({} as AuthenticationResponse);
    }

    const enrichedCredentials = this.enrichAuthRequest(credentials);

    return this.apiService.login(enrichedCredentials).pipe(
      tap(response => {
        // Se MFA é requerido, não faz login ainda
        if (response && response.requiresMfa) {
          return;
        }

        if (response && response.token && response.token.trim() !== '') {
          this.processLoginResponse(response, credentials.email);
        } else {
          throw new Error('Token não fornecido pelo servidor');
        }
      })
    );
  }

  completeMfaLogin(email: string, codigo: string): Observable<any> {
    return this.apiService.validarMfa(email, codigo).pipe(
      tap(response => {
        if (response && response.token && response.token.trim() !== '') {
          this.processLoginResponse(response, email);
        } else {
          throw new Error('Token não fornecido após validação MFA');
        }
      })
    );
  }

  private processLoginResponse(response: AuthenticationResponse, email: string): void {
    const userRole = response.role || this.determineUserRole(response.email || email, response);

    const user: User = {
      name: response.name || 'Usuário',
      email: response.email || email,
      role: userRole
    };

    if (this.isBrowser) {
      localStorage.setItem('auth_token', response.token);
      localStorage.setItem('current_user', JSON.stringify(user));
    }

    this.currentUserSubject.next(user);
    this.isAuthenticatedSubject.next(true);
  }

  register(userDetails: RegisterRequest): Observable<AuthenticationResponse> {
    return this.apiService.register(userDetails).pipe(
      tap(response => {
        // Se tem QR code MFA, não faz login automático - precisa configurar MFA primeiro
        if (response && response.mfaQrCode) {
          return;
        }

        if (response && response.token && response.token.trim() !== '') {
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
        }
      })
    );
  }

  logout(): void {
    if (environment.useKeycloak && this.keycloakService.isLoggedIn()) {
      this.keycloakService.logout();
    }

    if (this.isBrowser) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('current_user');
    }

    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  getToken(): string | null {
    if (environment.useKeycloak) {
      return this.keycloakService.getToken() || null;
    }
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
    if (response && response.role) {
      return response.role;
    }

    const token = this.getToken();
    if (token) {
      try {
        const tokenData = JSON.parse(atob(token.split('.')[1]));
        if (tokenData && tokenData.role) {
          return tokenData.role;
        }
      } catch (error) {
        console.error('Erro ao decodificar token:', error);
      }
    }

    return 'USER';
  }

  isAdmin(): boolean {
    if (environment.useKeycloak) {
      return this.keycloakService.isAdmin();
    }
    const currentUser = this.getCurrentUser();
    return currentUser?.role === 'ADMIN';
  }

  hasRole(role: string): boolean {
    if (environment.useKeycloak) {
      return this.keycloakService.hasRole(role);
    }
    const currentUser = this.getCurrentUser();
    return currentUser?.role === role;
  }

  refreshToken(): Observable<any> {
    if (environment.useKeycloak) {
      return new Observable(subscriber => {
        this.keycloakService.updateToken(30).then(refreshed => {
          subscriber.next(refreshed);
          subscriber.complete();
        }).catch(err => {
          subscriber.error(err);
        });
      });
    }

    const token = this.getToken();
    if (!token) {
      return new Observable(subscriber => {
        subscriber.error('Nenhum token disponível');
      });
    }

    return this.apiService.refreshToken(token).pipe(
      tap(newToken => {
        if (newToken) {
          if (this.isBrowser) {
            localStorage.setItem('auth_token', newToken);
          }
        } else {
          this.logout();
        }
      })
    );
  }
}
