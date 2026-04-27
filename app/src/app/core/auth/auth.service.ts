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

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/v1/auth';
  private tokenKey = 'jwt_token';
  private refreshTokenKey = 'refresh_token';
  private http = inject(HttpClient);

  public currentUser = signal<UserProfile | null>(null);
  public isAuthenticated = signal<boolean>(false);

  constructor() { this.checkToken(); }

  login(email: string, password: string): Observable<UserProfile> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap((res) => {
        localStorage.setItem(this.tokenKey, res.accessToken);
        localStorage.setItem(this.refreshTokenKey, res.refreshToken);
        this.isAuthenticated.set(true);
      }),
      switchMap(() => this.loadProfile()),
    );
  }

  refreshAccessToken(refreshToken: string): Observable<string> {
    return this.http.post<{ accessToken: string }>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap((res) => localStorage.setItem(this.tokenKey, res.accessToken)),
      map((res) => res.accessToken),
    );
  }

  logout() {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({ error: () => {} });
    }
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
  }

  getToken(): string | null { return localStorage.getItem(this.tokenKey); }
  getRefreshToken(): string | null { return localStorage.getItem(this.refreshTokenKey); }

  loadProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`).pipe(
      tap((profile) => { this.currentUser.set(profile); this.isAuthenticated.set(true); }),
    );
  }

  checkToken() {
    if (this.getToken()) {
      this.isAuthenticated.set(true);
      this.loadProfile().subscribe({ error: () => this.logout() });
    }
  }
}
