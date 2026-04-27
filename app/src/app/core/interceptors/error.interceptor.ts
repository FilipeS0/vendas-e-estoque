import { HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

let isRefreshing = false;

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      const status = error?.status;

      if (status === 401) {
        if (req.url.includes('/auth/refresh') || req.url.includes('/auth/login')) {
          doLogout(authService, router); return throwError(() => error);
        }
        if (!isRefreshing) {
          isRefreshing = true;
          const refreshToken = authService.getRefreshToken();
          if (!refreshToken) { isRefreshing = false; doLogout(authService, router); return throwError(() => error); }
          return authService.refreshAccessToken(refreshToken).pipe(
            switchMap((newToken) => { isRefreshing = false; return next(addToken(req, newToken)); }),
            catchError((e) => { isRefreshing = false; doLogout(authService, router); return throwError(() => e); }),
          );
        }
      }
      if (status === 403) snackBar.open('Acesso negado.', 'OK', { duration: 3000 });
      if (status === 0 || status === 504) snackBar.open('Erro de conexão.', 'OK', { duration: 3000 });
      if (status && status !== 401 && status !== 403 && status !== 0 && status !== 504)
        snackBar.open(error.error?.message || 'Erro inesperado.', 'OK', { duration: 3000 });
      return throwError(() => error);
    }),
  );
};

function addToken(req: HttpRequest<unknown>, token: string) {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}
function doLogout(authService: AuthService, router: Router) {
  authService.logout(); router.navigate(['/login']);
}
