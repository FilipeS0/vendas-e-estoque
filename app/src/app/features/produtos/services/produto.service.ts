import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProdutoRequest {
  codigoInterno: string;
  codigoBarras: string;
  nome: string;
  descricao?: string;
  unidadeMedida: string;
  categoriaId: string;
  fornecedorId: string;
  precoCusto: number;
  precoVenda: number;
  ncm: string;
  cest?: string;
  cfop: string;
  situacaoTributaria?: string;
  aliquotaIcms?: number;
  aliquotaPis?: number;
  aliquotaCofins?: number;
}

export interface ProdutoResponse {
  id: string;
  codigoInterno: string;
  codigoBarras: string;
  nome: string;
  categoriaNome: string;
  fornecedorNome: string;
  precoVenda: number;
  unidadeMedida?: string;
  imagemUrl?: string;
  ativo: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ProdutoUpdateRequest {
  nome: string;
  descricao?: string;
  unidadeMedida: string;
  categoriaId: string;
  fornecedorId: string;
  precoCusto: number;
  precoVenda: number;
  ncm: string;
  cest?: string;
  cfop: string;
  situacaoTributaria?: string;
  aliquotaIcms?: number;
  aliquotaPis?: number;
  aliquotaCofins?: number;
}

export interface ProdutoDetalheResponse {
  id: string;
  codigoInterno: string;
  codigoBarras: string;
  nome: string;
  descricao?: string;
  unidadeMedida: string;
  categoriaId: string;
  fornecedorId: string;
  precoCusto: number;
  precoVenda: number;
  ncm: string;
  cest?: string;
  cfop: string;
  situacaoTributaria?: string;
  aliquotaIcms?: number;
  aliquotaPis?: number;
  aliquotaCofins?: number;
  imagemUrl?: string;
  ativo: boolean;
}

export interface Categoria {
  id: string;
  nome: string;
}

export interface Fornecedor {
  id: string;
  nome: string;
}

export interface HistoricoPreco {
  id: string;
  precoCusto: number;
  precoVenda: number;
  dataAlteracao: string;
  motivo: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1';

  get baseUrl(): string {
    return ''; // Relative to the same host
  }

  getProdutos(nome?: string, page: number = 0, size: number = 10): Observable<PageResponse<ProdutoResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    if (nome) {
      params = params.set('nome', nome);
    }
    
    return this.http.get<PageResponse<ProdutoResponse>>(`${this.apiUrl}/produtos`, { params });
  }

  getProdutoById(id: string): Observable<ProdutoDetalheResponse> {
    return this.http.get<ProdutoDetalheResponse>(`${this.apiUrl}/produtos/${id}`);
  }

  create(produto: ProdutoRequest): Observable<ProdutoResponse> {
    return this.http.post<ProdutoResponse>(`${this.apiUrl}/produtos`, produto);
  }

  update(id: string, produto: ProdutoUpdateRequest): Observable<ProdutoResponse> {
    return this.http.put<ProdutoResponse>(`${this.apiUrl}/produtos/${id}`, produto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/produtos/${id}`);
  }

  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.apiUrl}/categorias`);
  }

  getFornecedores(): Observable<Fornecedor[]> {
    return this.http.get<Fornecedor[]>(`${this.apiUrl}/fornecedores`);
  }

  uploadImagem(id: string, file: File): Observable<void> {
    const formData = new FormData();
    formData.append('imagem', file);
    return this.http.post<void>(`${this.apiUrl}/produtos/${id}/imagem`, formData);
  }

  getHistoricoPrecos(id: string): Observable<HistoricoPreco[]> {
    return this.http.get<HistoricoPreco[]>(`${this.apiUrl}/produtos/${id}/historico-precos`);
  }
}
