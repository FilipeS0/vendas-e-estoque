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
import { ReportsService, FluxoCaixaResponse } from '../services/reports.service';
import { ProdutoRankingItem, VendaFormaPagItem, VendaPeriodoItem } from '../../../shared/index';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';

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
    DatePipe,
  ],
  templateUrl: './relatorios.component.html',
  styleUrls: ['./relatorios.component.css'],
})
export class RelatoriosComponent {
  private reportsService = inject(ReportsService);

  dataInicioControl = new FormControl<Date | null>(new Date(new Date().setDate(new Date().getDate() - 30)));
  dataFimControl = new FormControl<Date | null>(new Date());

  vendasPeriodo = signal<VendaPeriodoItem[]>([]);
  vendasFormaPagamento = signal<VendaFormaPagItem[]>([]);
  rankingProdutos = signal<ProdutoRankingItem[]>([]);
  fluxoCaixa = signal<FluxoCaixaResponse | null>(null);
  isLoading = signal(false);

  displayedColumnsPeriodo = ['data', 'quantidade', 'total'];
  displayedColumnsFormaPagamento = ['formaPagamento', 'vendas', 'total'];
  displayedColumnsRanking = ['posicao', 'produto', 'quantidade', 'total'];
  displayedColumnsFluxo = ['data', 'entradas', 'saidas', 'saldo'];

  private formatDate(date: Date | null): string {
    return date ? date.toISOString().slice(0, 10) : '';
  }

  ngOnInit() {
    this.loadRelatorios();
  }

  loadRelatorios() {
    const dataInicio = this.formatDate(this.dataInicioControl.value);
    const dataFim = this.formatDate(this.dataFimControl.value);
    if (!dataInicio || !dataFim) {
      return;
    }

    this.isLoading.set(true);

    forkJoin({
      periodo: this.reportsService.getVendasPorPeriodo(dataInicio, dataFim),
      formaPag: this.reportsService.getVendasPorFormaPagamento(dataInicio, dataFim),
      ranking: this.reportsService.getRankingProdutos(dataInicio, dataFim, 10),
      fluxo: this.reportsService.getFluxoCaixa(dataInicio, dataFim),
    }).subscribe({
      next: ({ periodo, formaPag, ranking, fluxo }) => {
        this.vendasPeriodo.set(periodo);
        this.vendasFormaPagamento.set(formaPag);
        this.rankingProdutos.set(ranking);
        this.fluxoCaixa.set(fluxo);
      },
      error: () => {},
      complete: () => this.isLoading.set(false),
    });
  }

  exportPdf(tipo: 'vendas' | 'forma-pagamento' | 'ranking' | 'fluxo-caixa') {
    const dataInicio = this.formatDate(this.dataInicioControl.value);
    const dataFim = this.formatDate(this.dataFimControl.value);
    if (!dataInicio || !dataFim) {
      return;
    }

    const params: any = { dataInicio, dataFim };
    if (tipo === 'ranking') params.top = '10';
    if (tipo === 'fluxo-caixa') {
        params.inicio = dataInicio;
        params.fim = dataFim;
    }

    this.reportsService.exportarPdf(tipo, params).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
    });
  }
}
