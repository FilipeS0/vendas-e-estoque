import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../../../shared/index';

export interface FornecedorItem {
  id: string;
  nome: string;
  cnpj?: string;
  telefone?: string;
  email?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  complemento?: string;
  ativo?: boolean;
}

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/fornecedores';

  getAll(page: number = 0, size: number = 20): Observable<PageResponse<FornecedorItem>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<FornecedorItem>>(this.apiUrl, { params });
  }

  create(fornecedor: FornecedorItem): Observable<FornecedorItem> {
    return this.http.post<FornecedorItem>(this.apiUrl, fornecedor);
  }

  update(id: string, fornecedor: FornecedorItem): Observable<FornecedorItem> {
    return this.http.put<FornecedorItem>(`${this.apiUrl}/${id}`, fornecedor);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
