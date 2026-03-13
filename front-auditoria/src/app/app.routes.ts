import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { TrustScoreComponent } from './components/trust-score/trust-score.component';
import { AiAnalysisComponent } from './components/ai-analysis/ai-analysis.component';
import { BlockchainComponent } from './components/blockchain/blockchain.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'trust-score', component: TrustScoreComponent, canActivate: [authGuard] },
  { path: 'ai-analysis', component: AiAnalysisComponent, canActivate: [authGuard] },
  { path: 'blockchain', component: BlockchainComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' }
];
