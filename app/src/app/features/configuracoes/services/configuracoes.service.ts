import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';

export interface ConfiguracaoRequest {
  razaoSocial: string;
  cnpj: string;
  inscricaoEstadual?: string;
  endereco?: string;
  regimeTributario: string;
  ambienteSefaz: string;
  serieNfce: number;
  numeroSequencialNfce: number;
  impressoraTermicaIp?: string;
  impressoraTermicaPorta?: number;
  alertaEstoqueMinimoGlobal: number;
}

export interface ConfiguracaoResponse extends ConfiguracaoRequest {
  id: string;
}

@Injectable({ providedIn: 'root' })
export class ConfiguracoesService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/configuracoes';

  async getConfiguracao(): Promise<ConfiguracaoResponse | null> {
    try {
        const response = await firstValueFrom(this.http.get<ConfiguracaoResponse>(this.apiUrl, { observe: 'response' }));
        return response.status === 204 ? null : response.body;
    } catch {
        return null;
    }
  }

  salvarConfiguracao(req: ConfiguracaoRequest): Observable<ConfiguracaoResponse> {
    return this.http.post<ConfiguracaoResponse>(this.apiUrl, req);
  }
}
