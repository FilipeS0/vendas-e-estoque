import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  let clonedReq = req;
  if (token) {
    clonedReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Don't refresh on login errors or already retried requests
      if (error.status === 401 && !req.url.includes('/api/v1/auth/login')) {
        const refreshToken = authService.getRefreshToken();
        if (refreshToken) {
          return authService.refreshAccessToken(refreshToken).pipe(
            switchMap((newToken) => {
              const retryReq = req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } });
              return next(retryReq);
            }),
            catchError((err) => {
              authService.logout();
              router.navigate(['/login']);
              return throwError(() => err);
            })
          );
        } else {
          authService.logout();
          router.navigate(['/login']);
        }
      }
      return throwError(() => error);
    })
  );
};
