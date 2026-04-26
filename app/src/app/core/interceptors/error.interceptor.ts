import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      const status = error?.status;

      if (status === 401) {
        authService.logout();
        router.navigate(['/login']);
      } else if (status === 403) {
        snackBar.open('Acesso negado.', 'OK', { duration: 3000 });
      } else if (status === 0 || status === 504) {
        snackBar.open('Erro de conexão. Verifique o servidor.', 'OK', { duration: 3000 });
      } else {
        snackBar.open(error.error?.message || 'Ocorreu um erro inesperado.', 'OK', {
          duration: 3000,
        });
      }

      return throwError(() => error);
    }),
  );
};
