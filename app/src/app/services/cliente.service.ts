import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Cliente, ClienteExtrato } from '../models';

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
   * Fetch a single client by ID
   */
  async getClienteById(id: string): Promise<Cliente> {
    return firstValueFrom(this.http.get<Cliente>(`${this.apiUrl}/${id}`));
  }

  /**
   * Create a new client
   */
  async createCliente(cliente: Omit<Cliente, 'id'>): Promise<Cliente> {
    return firstValueFrom(this.http.post<Cliente>(this.apiUrl, cliente));
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
