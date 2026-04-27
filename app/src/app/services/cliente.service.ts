import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { Cliente, ClienteExtrato, ClienteRequest, PageResponse } from '../../../shared/index';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/clientes';

  /**
   * Fetch clients with server-side pagination and optional name filter.
   */
  getClientes(nome?: string, page = 0, size = 10): Observable<PageResponse<Cliente>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (nome?.trim()) params = params.set('nome', nome.trim());
    return this.http.get<PageResponse<Cliente>>(this.apiUrl, { params });
  }

  /**
   * Fetch all clients (used by PDV autocomplete — small list).
   */
  async getClientesAll(): Promise<Cliente[]> {
    return firstValueFrom(
      this.http.get<PageResponse<Cliente>>(this.apiUrl, {
        params: new HttpParams().set('page', '0').set('size', '1000'),
      }),
    ).then((res) => res.content);
  }

  async getClienteById(id: string): Promise<Cliente> {
    return firstValueFrom(this.http.get<Cliente>(`${this.apiUrl}/${id}`));
  }

  createCliente(req: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, req);
  }

  async updateCliente(id: string, cliente: Partial<Cliente>): Promise<Cliente> {
    return firstValueFrom(this.http.put<Cliente>(`${this.apiUrl}/${id}`, cliente));
  }

  async deleteCliente(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.apiUrl}/${id}`));
  }

  async getClienteExtrato(id: string): Promise<ClienteExtrato> {
    return firstValueFrom(this.http.get<ClienteExtrato>(`${this.apiUrl}/${id}/extrato`));
  }
}
