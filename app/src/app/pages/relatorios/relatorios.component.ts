import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { ReportsService } from '../../services/reports.service';
import { ProdutoRankingItem, VendaFormaPagItem, VendaPeriodoItem } from '../../models';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-relatorios',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatNativeDateModule,
    MatTabsModule,
    MatTableModule,
    CurrencyPipe,
  ],
  templateUrl: './relatorios.component.html',
  styleUrls: ['./relatorios.component.css'],
})
export class RelatoriosComponent {
  private reportsService = inject(ReportsService);

  dataInicioControl = new FormControl<Date | null>(null);
  dataFimControl = new FormControl<Date | null>(null);

  vendasPeriodo = signal<VendaPeriodoItem[]>([]);
  vendasFormaPagamento = signal<VendaFormaPagItem[]>([]);
  rankingProdutos = signal<ProdutoRankingItem[]>([]);
  isLoading = signal(false);

  displayedColumnsPeriodo = ['data', 'quantidade', 'total'];
  displayedColumnsFormaPagamento = ['formaPagamento', 'vendas', 'total'];
  displayedColumnsRanking = ['posicao', 'produto', 'quantidade', 'total'];

  private formatDate(date: Date | null): string {
    return date ? date.toISOString().slice(0, 10) : '';
  }

  loadRelatorios() {
    const dataInicio = this.formatDate(this.dataInicioControl.value);
    const dataFim = this.formatDate(this.dataFimControl.value);
    if (!dataInicio || !dataFim) {
      return;
    }

    this.isLoading.set(true);
    this.reportsService.getVendasPorPeriodo(dataInicio, dataFim).subscribe({
      next: (items) => this.vendasPeriodo.set(items),
      error: () => this.vendasPeriodo.set([]),
    });

    this.reportsService.getVendasPorFormaPagamento(dataInicio, dataFim).subscribe({
      next: (items) => this.vendasFormaPagamento.set(items),
      error: () => this.vendasFormaPagamento.set([]),
    });

    this.reportsService.getRankingProdutos(dataInicio, dataFim, 10).subscribe({
      next: (items) => this.rankingProdutos.set(items),
      error: () => this.rankingProdutos.set([]),
      complete: () => this.isLoading.set(false),
    });
  }

  exportPdf(tipo: 'vendas' | 'forma-pagamento' | 'ranking') {
    const dataInicio = this.formatDate(this.dataInicioControl.value);
    const dataFim = this.formatDate(this.dataFimControl.value);
    if (!dataInicio || !dataFim) {
      return;
    }

    const params = { dataInicio, dataFim, top: '10' };
    this.reportsService.exportarPdf(tipo, params).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
    });
  }
}
