import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../../produtos/services/produto.service';

export interface EstoqueAtual {
  id: string;
  produtoId: string;
  produtoNome: string;
  codigoInterno: string;
  quantidadeAtual: number;
  quantidadeMinima: number;
  updatedAt: string;
}

export interface MovimentacaoRequest {
  tipo: 'ENTRADA_COMPRA' | 'ENTRADA_DEVOLUCAO' | 'SAIDA_PERDA' | 'AJUSTE_INVENTARIO';
  quantidade: number;
  motivo: string;
}

export interface MovimentacaoEstoque {
  id: string;
  produtoNome: string;
  tipo: string;
  quantidade: number;
  quantidadeAnterior: number;
  quantidadeResultante: number;
  motivo: string;
  dataHora: string;
}

@Injectable({
  providedIn: 'root',
})
export class EstoqueService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/estoque';

  getEstoque(
    nome: string | undefined,
    page: number,
    size: number,
  ): Observable<PageResponse<EstoqueAtual>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (nome) {
      params = params.set('nome', nome);
    }

    return this.http.get<PageResponse<EstoqueAtual>>(this.apiUrl, { params });
  }

  getMovimentacoes(
    produtoId: string,
    page: number,
    size: number,
  ): Observable<PageResponse<MovimentacaoEstoque>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    return this.http.get<PageResponse<MovimentacaoEstoque>>(
      `${this.apiUrl}/${produtoId}/movimentacoes`,
      { params },
    );
  }

  registrarMovimentacao(produtoId: string, req: MovimentacaoRequest): Observable<EstoqueAtual> {
    return this.http.post<EstoqueAtual>(`${this.apiUrl}/${produtoId}/movimentacao`, req);
  }
}
