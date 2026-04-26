import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginResponse {
  token: string;
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
  private http = inject(HttpClient);

  public currentUser = signal<UserProfile | null>(null);
  public isAuthenticated = signal<boolean>(false);

  constructor() {
    this.checkToken();
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap((response) => {
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token);
          this.isAuthenticated.set(true);
        }
      }),
    );
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
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
    }
  }
}
