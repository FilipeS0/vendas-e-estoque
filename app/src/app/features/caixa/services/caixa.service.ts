import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Caixa } from '../../../shared/index';

export interface Lancamento {
  tipo: 'ENTRADA' | 'SAIDA';
  formaPagamento?: string;
  valor: number;
  descricao?: string;
}

export interface Suprimento extends Lancamento {
  caixaId: string;
}

export interface Sangria extends Lancamento {
  caixaId: string;
}

@Injectable({ providedIn: 'root' })
export class CaixaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/caixa';

  /**
   * Fetch all cash registers
   */
  async getCaixas(): Promise<Caixa[]> {
    return firstValueFrom(this.http.get<Caixa[]>(this.apiUrl));
  }

  /**
   * Fetch a specific cash register
   */
  async getCaixaById(id: string): Promise<Caixa> {
    return firstValueFrom(this.http.get<Caixa>(`${this.apiUrl}/${id}`));
  }

  /**
   * Open a cash register
   */
  async abrirCaixa(valorAbertura: number): Promise<Caixa> {
    return firstValueFrom(this.http.post<Caixa>(`${this.apiUrl}/abrir`, { valorAbertura }));
  }

  /**
   * Close a cash register
   */
  async fecharCaixa(caixaId: string, valorFechamentoFis: number): Promise<Caixa> {
    return firstValueFrom(
      this.http.post<Caixa>(`${this.apiUrl}/${caixaId}/fechar`, {
        valorFechamentoFis,
      }),
    );
  }

  /**
   * Record a cash withdrawal (Sangria)
   */
  async registrarSangria(caixaId: string, valor: number, descricao?: string): Promise<any> {
    return firstValueFrom(
      this.http.post(`${this.apiUrl}/${caixaId}/sangria`, {
        valor,
        descricao,
      }),
    );
  }

  /**
   * Record a cash supply (Suprimento)
   */
  async registrarSuprimento(caixaId: string, valor: number, descricao?: string): Promise<any> {
    return firstValueFrom(
      this.http.post(`${this.apiUrl}/${caixaId}/suprimento`, {
        valor,
        descricao,
      }),
    );
  }

  /**
   * Fetch cash entries for a specific register
   */
  async getLancamentos(caixaId: string): Promise<Lancamento[]> {
    return firstValueFrom(this.http.get<Lancamento[]>(`${this.apiUrl}/${caixaId}/lancamentos`));
  }
}
