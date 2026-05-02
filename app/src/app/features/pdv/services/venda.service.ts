import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ItemVendaRequest {
  produtoId: string;
  quantidade: number;
  desconto?: number;
}

export interface PagamentoRequest {
  formaPagamento: 'DINHEIRO' | 'CARTAO_DEBITO' | 'CARTAO_CREDITO' | 'PIX' | 'CREDIARIO';
  valor: number;
  numeroParcelas?: number;
}

export interface VendaRequest {
  caixaId: string;
  clienteId?: string;
  itens: ItemVendaRequest[];
  pagamentos: PagamentoRequest[];
  valorDesconto?: number;
}

export interface VendaResponse {
  id: string;
  numero: number;
  valorTotal: number;
  status: string;
  dataHora: string;
}

@Injectable({
  providedIn: 'root',
})
export class VendaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/vendas';

  criarVenda(req: VendaRequest): Observable<VendaResponse> {
    return this.http.post<VendaResponse>(this.apiUrl, req);
  }

  cancelarVenda(id: string, justificativa: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      body: { justificativa },
    });
  }

  getVendasDoTurno(): Observable<VendaResponse[]> {
    return this.http.get<VendaResponse[]>(`${this.apiUrl}/turno-atual`);
  }
}
