import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface FornecedorItem {
  id: string;
  nome: string;
  cnpj?: string;
  telefone?: string;
  email?: string;
  ativo?: boolean;
}

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/fornecedores';

  getAll(): Observable<FornecedorItem[]> {
    return this.http.get<FornecedorItem[]>(this.apiUrl);
  }
}
