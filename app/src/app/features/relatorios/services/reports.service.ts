import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { DashboardStats, ProdutoRankingItem, VendaFormaPagItem, VendaPeriodoItem } from '../../../shared/index';

export interface FluxoCaixaResponse {
  dias: FluxoDiario[];
  totalEntradas: number;
  totalSaidas: number;
  saldoFinal: number;
}

export interface FluxoDiario {
  data: string;
  entradas: number;
  saidas: number;
  saldo: number;
}

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

  getVendasPorPeriodo(inicio: string, fim: string): Observable<VendaPeriodoItem[]> {
    return this.http.get<VendaPeriodoItem[]>(`${this.apiUrl}/vendas`, {
      params: { inicio: `${inicio}T00:00:00`, fim: `${fim}T23:59:59` },
    });
  }

  getVendasPorFormaPagamento(inicio: string, fim: string): Observable<VendaFormaPagItem[]> {
    return this.http.get<VendaFormaPagItem[]>(`${this.apiUrl}/vendas/forma-pagamento`, {
      params: { inicio: `${inicio}T00:00:00`, fim: `${fim}T23:59:59` },
    });
  }

  getRankingProdutos(
    inicio: string,
    fim: string,
    top: number,
  ): Observable<ProdutoRankingItem[]> {
    return this.http.get<ProdutoRankingItem[]>(`${this.apiUrl}/produtos/ranking`, {
      params: { inicio: `${inicio}T00:00:00`, fim: `${fim}T23:59:59`, top: top.toString() },
    });
  }

  getFluxoCaixa(inicio: string, fim: string): Observable<FluxoCaixaResponse> {
    return this.http.get<FluxoCaixaResponse>(`${this.apiUrl}/fluxo-caixa`, {
      params: { inicio, fim }
    });
  }

  exportarPdf(tipo: string, params: any): Observable<Blob> {
    const standardizedParams: any = {};
    if (params.dataInicio) standardizedParams.inicio = `${params.dataInicio}T00:00:00`;
    if (params.dataFim) standardizedParams.fim = `${params.dataFim}T23:59:59`;
    if (params.inicio && !standardizedParams.inicio) standardizedParams.inicio = params.inicio;
    if (params.fim && !standardizedParams.fim) standardizedParams.fim = params.fim;

    return this.http.get(`${this.apiUrl}/${tipo}/pdf`, {
      params: standardizedParams,
      responseType: 'blob',
    });
  }
}
