import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CepResult {
  cep: string;
  state: string;
  city: string;
  neighborhood: string;
  street: string;
  service: string;
}

export interface CnpjResult {
  cnpj: string;
  razao_social: string;
  nome_fantasia: string;
  logradouro: string;
  numero: string;
  bairro: string;
  municipio: string;
  uf: string;
  cep: string;
  telefone: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class BrasilApiService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/external';

  consultarCep(cep: string): Observable<CepResult> {
    return this.http.get<CepResult>(`${this.apiUrl}/cep/${cep}`);
  }

  consultarCnpj(cnpj: string): Observable<CnpjResult> {
    return this.http.get<CnpjResult>(`${this.apiUrl}/cnpj/${cnpj}`);
  }
}
