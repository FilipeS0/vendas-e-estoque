import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CategoriaItem {
  id: string;
  nome: string;
  descricao?: string;
  ativo?: boolean;
}

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/categorias';

  getAll(): Observable<CategoriaItem[]> {
    return this.http.get<CategoriaItem[]>(this.apiUrl);
  }
}
