import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {
  private keycloak: any = null;
  private initialized = false;

  async init(): Promise<boolean> {
    if (!environment.useKeycloak) {
      return false;
    }

    try {
      // @ts-ignore
      const keycloakModule = await import(/* webpackIgnore: true */ 'keycloak-js');
      const KeycloakConstructor = keycloakModule.default || keycloakModule;
      this.keycloak = new KeycloakConstructor({
        url: environment.keycloak.url,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId,
      });

      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        pkceMethod: 'S256',
      });

      this.initialized = true;
      console.log('Keycloak inicializado. Autenticado:', authenticated);
      return authenticated;
    } catch (error) {
      console.error('Erro ao inicializar Keycloak:', error);
      return false;
    }
  }

  login(): Promise<void> {
    if (!this.keycloak) return Promise.resolve();
    return this.keycloak.login();
  }

  logout(): Promise<void> {
    if (!this.keycloak) return Promise.resolve();
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }

  getToken(): string | undefined {
    return this.keycloak?.token;
  }

  isLoggedIn(): boolean {
    return this.keycloak?.authenticated ?? false;
  }

  getUserProfile(): any {
    return this.keycloak?.tokenParsed;
  }

  getUserRoles(): string[] {
    const token = this.keycloak?.tokenParsed;
    if (token?.realm_access?.roles) {
      return token.realm_access.roles;
    }
    return [];
  }

  hasRole(role: string): boolean {
    return this.getUserRoles().includes(role);
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  async updateToken(minValidity: number = 30): Promise<boolean> {
    if (!this.keycloak) return false;
    try {
      return await this.keycloak.updateToken(minValidity);
    } catch {
      return false;
    }
  }

  isInitialized(): boolean {
    return this.initialized;
  }
}
