import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token');
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    
    return headers;
  }

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.baseUrl}/api/v1/autenticacao/entrar`, 
      credentials, 
      { headers: this.getHeaders() }
    );
  }

  register(userDetails: RegisterRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.baseUrl}/api/v1/autenticacao/registrar`, 
      userDetails, 
      { headers: this.getHeaders() }
    );
  }

  validateToken(token: string): Observable<boolean> {
    return this.http.post<boolean>(
      `${this.baseUrl}/api/v1/autenticacao/validar-token`, 
      null,
      { 
        headers: this.getHeaders(),
        params: { token }
      }
    );
  }

  refreshToken(expiredToken: string): Observable<string> {
    return this.http.post<string>(
      `${this.baseUrl}/api/v1/autenticacao/renovar-token`, 
      null,
      { 
        headers: this.getHeaders(),
        params: { tokenExpirado: expiredToken }
      }
    );
  }

  getStatus(): Observable<string> {
    return this.http.get<string>(
      `${this.baseUrl}/api/v1/autenticacao/status`, 
      { headers: this.getHeaders() }
    );
  }
}
