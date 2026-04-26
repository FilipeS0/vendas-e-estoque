import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { DashboardStats, ProdutoRankingItem, VendaFormaPagItem, VendaPeriodoItem } from '../models';

@Injectable({ providedIn: 'root' })
export class ReportsService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/relatorios';

  /**
   * Fetch dashboard statistics
   */
  async getDashboardStats(): Promise<DashboardStats> {
    return firstValueFrom(this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`));
  }

  getVendasPorPeriodo(dataInicio: string, dataFim: string): Observable<VendaPeriodoItem[]> {
    return this.http.get<VendaPeriodoItem[]>(`${this.apiUrl}/vendas`, {
      params: { dataInicio, dataFim },
    });
  }

  getVendasPorFormaPagamento(dataInicio: string, dataFim: string): Observable<VendaFormaPagItem[]> {
    return this.http.get<VendaFormaPagItem[]>(`${this.apiUrl}/vendas/forma-pagamento`, {
      params: { dataInicio, dataFim },
    });
  }

  getRankingProdutos(
    dataInicio: string,
    dataFim: string,
    top: number,
  ): Observable<ProdutoRankingItem[]> {
    return this.http.get<ProdutoRankingItem[]>(`${this.apiUrl}/produtos/ranking`, {
      params: { dataInicio, dataFim, top: top.toString() },
    });
  }

  exportarPdf(tipo: 'vendas' | 'forma-pagamento' | 'ranking', params: object): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${tipo}/pdf`, {
      params: params as Record<string, string>,
      responseType: 'blob',
    });
  }
}
