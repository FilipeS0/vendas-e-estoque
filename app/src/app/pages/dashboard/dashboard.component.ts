import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/auth/auth.service';
import { ReportsService } from '../../features/relatorios/services/reports.service';
import { DashboardStats } from '../../shared';
import { LineChartComponent } from '../../shared/components/line-chart/line-chart.component';

@Component({
  selector: 'app-dashboard',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTableModule,
    MatTooltipModule,
    CurrencyPipe,
    DecimalPipe,
    LineChartComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardPageComponent {
  public authService = inject(AuthService);
  public router = inject(Router);
  private reportsService = inject(ReportsService);

  stats = signal<DashboardStats | null>(null);
  isLoading = signal(true);
  hasError = signal(false);

  topProdutosColumns = ['posicao', 'nome', 'quantidade', 'total'];

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.isLoading.set(true);
    this.hasError.set(false);

    this.reportsService.getDashboardStats().then(
      (s) => {
        this.stats.set(s);
        this.isLoading.set(false);
      },
      () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    );
  }

  get chartData() {
    return (this.stats()?.vendasRecentemente ?? []).map((v) => ({
      date: v.data,
      value: v.total,
    }));
  }

  get formaPagamentoEntries() {
    const map = this.stats()?.faturamentoPorFormaPagamento ?? {};
    return Object.entries(map).map(([forma, valor]) => ({ forma, valor }));
  }

  formaPagLabel(key: string): string {
    const labels: Record<string, string> = {
      DINHEIRO: 'Dinheiro',
      CARTAO_DEBITO: 'Débito',
      CARTAO_CREDITO: 'Crédito',
      PIX: 'PIX',
      CREDIARIO: 'Crediário',
    };
    return labels[key] ?? key;
  }

  formaPagIcon(key: string): string {
    const icons: Record<string, string> = {
      DINHEIRO: 'payments',
      CARTAO_DEBITO: 'credit_card',
      CARTAO_CREDITO: 'credit_score',
      PIX: 'pix',
      CREDIARIO: 'account_balance',
    };
    return icons[key] ?? 'attach_money';
  }
}
