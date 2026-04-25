import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { ReportsService, DashboardStats } from '../../services/reports.service';
import { DecimalPipe } from '@angular/common';
import { LineChartComponent } from '../../components/line-chart/line-chart.component';

@Component({
  selector: 'app-dashboard',
  imports: [MatCardModule, MatButtonModule, MatIconModule, DecimalPipe, LineChartComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardPageComponent {
  public authService = inject(AuthService);
  public router = inject(Router);

  private reportsService = inject(ReportsService);
  public stats = signal<DashboardStats | null>(null);

  ngOnInit() {
    if (!this.authService.currentUser()) {
      this.authService.loadProfile().subscribe();
    }
    this.loadStats();
  }

  loadStats() {
    this.reportsService.getDashboardStats().subscribe({
      next: (s) => this.stats.set(s),
      error: () => this.stats.set(null),
    });
  }

  trackByRole(_index: number, role: { authority: string } | null) {
    return role?.authority;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
