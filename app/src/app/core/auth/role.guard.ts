import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRoles = (route.data?.['roles'] as string[]) ?? [];
  const user = authService.currentUser();

  if (!requiredRoles.length) return true;
  if (!user) { router.navigate(['/403']); return false; }

  const hasRole = user.authorities?.some((a) => requiredRoles.includes(a.authority));
  if (hasRole) return true;
  router.navigate(['/403']);
  return false;
};
