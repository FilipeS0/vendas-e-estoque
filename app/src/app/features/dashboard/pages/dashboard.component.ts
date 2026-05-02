import { Component, inject, signal, computed } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { ReportsService } from '../../relatorios/services/reports.service';
import { DashboardStats, VendasPorDia } from '../../../shared/index';
import { DecimalPipe, CurrencyPipe } from '@angular/common';
import { ChartComponent } from '../../../shared/components/chart/chart.component';

@Component({
  selector: 'app-dashboard',
  imports: [MatCardModule, MatButtonModule, MatIconModule, DecimalPipe, CurrencyPipe, ChartComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardPageComponent {
  public authService = inject(AuthService);
  public router = inject(Router);
  private reportsService = inject(ReportsService);
  public stats = signal<DashboardStats | null>(null);

  // Computed data for charts
  salesChartData = computed(() => {
    const s = this.stats();
    if (!s) return null;
    return {
      labels: s.vendasRecentemente.map((v: VendasPorDia) => v.data),
      datasets: [{
        label: 'Faturamento (R$)',
        data: s.vendasRecentemente.map((v: VendasPorDia) => v.total),
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        fill: true,
        tension: 0.4
      }]
    };
  });

  paymentsChartData = computed(() => {
    const s = this.stats();
    if (!s) return null;
    const labels = Object.keys(s.faturamentoPorFormaPagamento);
    const data = Object.values(s.faturamentoPorFormaPagamento);
    return {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: [
          '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'
        ]
      }]
    };
  });

  ngOnInit() {
    if (!this.authService.currentUser()) this.authService.loadProfile().subscribe();
    this.loadStats();
  }

  loadStats() {
    this.reportsService.getDashboardStats()
      .then((s) => this.stats.set(s))
      .catch(() => this.stats.set(null));
  }

  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}
