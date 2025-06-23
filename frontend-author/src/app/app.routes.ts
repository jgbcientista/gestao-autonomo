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

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'system-monitor', component: SystemMonitorComponent },
  { path: 'trust-score', component: TrustScoreComponent },
  { path: 'ai-analysis', component: AiAnalysisComponent },
  { path: 'security-analytics', component: SecurityAnalyticsComponent },
  { path: 'session-manager', component: SessionManagerComponent },
  { path: 'settings', component: SettingsComponent },
  { path: '**', redirectTo: '/login' }
];
