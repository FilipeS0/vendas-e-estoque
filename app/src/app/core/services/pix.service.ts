import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PixResponse {
  payload: string;
  qrCode: string;
}

@Injectable({
  providedIn: 'root'
})
export class PixService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/pix';

  generate(valor: number): Observable<PixResponse> {
    return this.http.get<PixResponse>(`${this.apiUrl}/generate`, {
      params: new HttpParams().set('valor', valor.toString())
    });
  }
}
