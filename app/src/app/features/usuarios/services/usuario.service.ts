import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario, UsuarioRequest, Perfil, PageResponse } from '../../../shared/index';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/usuarios';

  getUsuarios(page: number = 0, size: number = 10): Observable<PageResponse<Usuario>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Usuario>>(this.apiUrl, { params });
  }

  getUsuarioById(id: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  createUsuario(req: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, req);
  }

  updateUsuario(id: string, req: UsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, req);
  }

  inativarUsuario(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getPerfis(): Observable<Perfil[]> {
    return this.http.get<Perfil[]>(`${this.apiUrl}/perfis`);
  }
}
