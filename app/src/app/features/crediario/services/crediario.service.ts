import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../../../shared/index';

export type StatusParcela = 'PENDENTE' | 'PAGO' | 'PAGO_PARCIAL' | 'ATRASADO' | 'CANCELADO';

export interface ParcelaResponse {
  id: string;
  numeroParcela: number;
  valor: number;
  dataVencimento: string;
  dataPagamento?: string;
  valorPago?: number;
  status: StatusParcela;
  clienteNome?: string;
  clienteId?: string;
}

export interface LiquidarParcelaRequest {
  valorPago: number;
  caixaId: string;
}

@Injectable({ providedIn: 'root' })
export class CrediarioService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/crediarios';

  getParcelas(
    clienteId?: string,
    status?: StatusParcela,
    page = 0,
    size = 10,
  ): Observable<PageResponse<ParcelaResponse>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (clienteId) params = params.set('clienteId', clienteId);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<ParcelaResponse>>(`${this.apiUrl}/parcelas`, { params });
  }

  liquidarParcela(id: string, request: LiquidarParcelaRequest): Observable<ParcelaResponse> {
    return this.http.post<ParcelaResponse>(`${this.apiUrl}/parcelas/${id}/liquidar`, request);
  }
}
