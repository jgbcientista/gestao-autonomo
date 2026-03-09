import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';
import { KeycloakService } from '../services/keycloak.service';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  if (environment.useKeycloak) {
    const keycloakService = inject(KeycloakService);
    if (keycloakService.isLoggedIn()) {
      return true;
    }
    keycloakService.login();
    return false;
  }

  const authService = inject(AuthService);
  if (authService.isAuthenticated()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
