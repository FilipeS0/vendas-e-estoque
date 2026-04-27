import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';
import { Cliente, ClienteExtrato, ClienteRequest, PageResponse } from '../../../shared/index';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/clientes';

  /**
   * Fetch all clients as a Promise (for use with toSignal)
   */
  async getClientes(): Promise<Cliente[]> {
    return firstValueFrom(this.http.get<Cliente[]>(this.apiUrl));
  }

  /**
   * Fetch paged clients for the list view
   */
  getClientesPage(nome?: string, page?: number, size?: number): Observable<PageResponse<Cliente>> {
    const params: Record<string, string> = {};
    if (nome) params['nome'] = nome;
    if (page != null) params['page'] = page.toString();
    if (size != null) params['size'] = size.toString();
    return this.http.get<PageResponse<Cliente>>(this.apiUrl, { params });
  }

  /**
   * Fetch a single client by ID
   */
  async getClienteById(id: string): Promise<Cliente> {
    return firstValueFrom(this.http.get<Cliente>(`${this.apiUrl}/${id}`));
  }

  /**
   * Create a new client
   */
  createCliente(req: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, req);
  }

  /**
   * Update an existing client
   */
  async updateCliente(id: string, cliente: Partial<Cliente>): Promise<Cliente> {
    return firstValueFrom(this.http.put<Cliente>(`${this.apiUrl}/${id}`, cliente));
  }

  /**
   * Delete a client
   */
  async deleteCliente(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.apiUrl}/${id}`));
  }

  /**
   * Fetch client statement (extrato)
   */
  async getClienteExtrato(id: string): Promise<ClienteExtrato> {
    return firstValueFrom(this.http.get<ClienteExtrato>(`${this.apiUrl}/${id}/extrato`));
  }
}
