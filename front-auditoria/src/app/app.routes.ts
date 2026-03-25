import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { TrustScoreComponent } from './components/trust-score/trust-score.component';
import { AiAnalysisComponent } from './components/ai-analysis/ai-analysis.component';
import { BlockchainComponent } from './components/blockchain/blockchain.component';
import { AttackSimulationComponent } from './components/attack-simulation/attack-simulation.component';
import { GeoHeatmapComponent } from './components/geo-heatmap/geo-heatmap.component';
import { AiComparisonComponent } from './components/ai-comparison/ai-comparison.component';
import { DataGeneratorComponent } from './components/data-generator/data-generator.component';
import { KeystrokeProfileComponent } from './components/keystroke-profile/keystroke-profile.component';
import { ExplicabilidadeIAComponent } from './components/explicabilidade-ia/explicabilidade-ia.component';
import { DerivaComportamentalComponent } from './components/deriva-comportamental/deriva-comportamental.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'trust-score', component: TrustScoreComponent, canActivate: [authGuard] },
  { path: 'ai-analysis', component: AiAnalysisComponent, canActivate: [authGuard] },
  { path: 'blockchain', component: BlockchainComponent, canActivate: [authGuard] },
  { path: 'attack-simulation', component: AttackSimulationComponent, canActivate: [authGuard] },
  { path: 'geo-heatmap', component: GeoHeatmapComponent, canActivate: [authGuard] },
  { path: 'ai-comparison', component: AiComparisonComponent, canActivate: [authGuard] },
  { path: 'data-generator', component: DataGeneratorComponent, canActivate: [authGuard] },
  { path: 'keystroke-profile', component: KeystrokeProfileComponent, canActivate: [authGuard] },
  { path: 'explicabilidade-ia', component: ExplicabilidadeIAComponent, canActivate: [authGuard] },
  { path: 'deriva-comportamental', component: DerivaComportamentalComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' }
];
