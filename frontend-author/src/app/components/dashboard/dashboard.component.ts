import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  systemStatus: string = '';
  lastLogin: string = '';
  showAlert: boolean = true;
  private subscription = new Subscription();

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeComponent();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private initializeComponent(): void {
    // Verifica se o usuário está autenticado
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    // Carrega informações do usuário
    this.loadUserInfo();
    
    // Carrega status do sistema
    this.checkSystemStatus();
    
    // Define última sessão
    this.lastLogin = new Date().toLocaleString('pt-BR');
  }

  private loadUserInfo(): void {
    const userSub = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
    this.subscription.add(userSub);
  }

  checkSystemStatus(): void {
    const statusSub = this.apiService.getStatus().subscribe({
      next: (status) => {
        this.systemStatus = status;
      },
      error: (error) => {
        console.error('Erro ao verificar status:', error);
        this.systemStatus = 'Erro ao verificar status';
      }
    });
    this.subscription.add(statusSub);
  }

  logout(): void {
    this.authService.logout();
  }
}
