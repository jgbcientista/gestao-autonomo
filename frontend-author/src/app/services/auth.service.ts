import { Injectable } from '@angular/core';
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

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {
    this.checkAuthStatus();
  }

  private checkAuthStatus(): void {
    const token = localStorage.getItem('auth_token');
    const userStr = localStorage.getItem('current_user');
    
    if (token && userStr) {
      const user = JSON.parse(userStr);
      this.currentUserSubject.next(user);
      this.isAuthenticatedSubject.next(true);
    }
  }

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.apiService.login(credentials).pipe(
      tap(response => {
        if (response.token) {
          const user: User = {
            name: response.name,
            email: response.email
          };
          
          localStorage.setItem('auth_token', response.token);
          localStorage.setItem('current_user', JSON.stringify(user));
          
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
        }
      })
    );
  }

  register(userDetails: RegisterRequest): Observable<AuthenticationResponse> {
    return this.apiService.register(userDetails).pipe(
      tap(response => {
        if (response.token) {
          const user: User = {
            name: response.name,
            email: response.email
          };
          
          localStorage.setItem('auth_token', response.token);
          localStorage.setItem('current_user', JSON.stringify(user));
          
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
    
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
