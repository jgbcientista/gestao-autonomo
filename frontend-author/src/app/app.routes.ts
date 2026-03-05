import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SystemMonitorComponent } from './components/system-monitor/system-monitor.component';
import { TrustScoreComponent } from './components/trust-score/trust-score.component';
import { AiAnalysisComponent } from './components/ai-analysis/ai-analysis.component';
import { SecurityAnalyticsComponent } from './components/security-analytics/security-analytics.component';
import { SessionManagerComponent } from './components/session-manager/session-manager.component';
import { SettingsComponent } from './components/settings/settings.component';
import { SessionMonitorComponent } from './components/session-monitor/session-monitor.component';
import { RelatoriosComponent } from './components/relatorios/relatorios.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'system-monitor', component: SystemMonitorComponent, canActivate: [authGuard] },
  { path: 'trust-score', component: TrustScoreComponent, canActivate: [authGuard] },
  { path: 'ai-analysis', component: AiAnalysisComponent, canActivate: [authGuard] },
  { path: 'security-analytics', component: SecurityAnalyticsComponent, canActivate: [authGuard] },
  { path: 'session-manager', component: SessionManagerComponent, canActivate: [authGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [authGuard] },
  { path: 'session-monitor', component: SessionMonitorComponent, canActivate: [authGuard] },
  { path: 'relatorios', component: RelatoriosComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' }
];
