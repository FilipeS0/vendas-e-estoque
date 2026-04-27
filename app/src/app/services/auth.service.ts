import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, switchMap, tap } from 'rxjs';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export interface UserProfile {
  id: string;
  nome: string;
  email: string;
  authorities: { authority: string }[];
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = '/api/v1/auth';
  private tokenKey = 'jwt_token';
  private refreshTokenKey = 'refresh_token';
  private http = inject(HttpClient);

  public currentUser = signal<UserProfile | null>(null);
  public isAuthenticated = signal<boolean>(false);

  constructor() {
    this.checkToken();
  }

  login(email: string, password: string): Observable<UserProfile> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.accessToken);
        localStorage.setItem(this.refreshTokenKey, response.refreshToken);
        this.isAuthenticated.set(true);
      }),
      // Immediately load the user profile so roleGuard has data
      switchMap(() => this.loadProfile()),
    );
  }

  /**
   * Chama POST /auth/refresh e salva o novo accessToken.
   * Retorna o novo token como string para o interceptor reutilizar na requisição original.
   */
  refreshAccessToken(refreshToken: string): Observable<string> {
    return this.http.post<{ accessToken: string }>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.accessToken);
      }),
      map((response) => response.accessToken),
    );
  }

  logout() {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      // Best-effort: revoke on server; ignore errors
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({ error: () => {} });
    }
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  loadProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`).pipe(
      tap((profile) => {
        this.currentUser.set(profile);
        this.isAuthenticated.set(true);
      }),
    );
  }

  checkToken() {
    if (this.getToken()) {
      this.isAuthenticated.set(true);
      // Restore user profile on page refresh
      this.loadProfile().subscribe({ error: () => this.logout() });
    }
  }
}
