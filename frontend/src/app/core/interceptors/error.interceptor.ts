import { HttpInterceptorFn } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { UIToastService } from '../services/ui-toast.service';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(UIToastService);
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err: any) => {
      try {
        const body = err?.error;
        const status = err?.status;
        const code = body?.code;

        // Handle banned user - force logout and redirect
        if (status === 403 && code === 'ACCOUNT_BANNED') {
          authService.logout();
          router.navigate(['/login']);
          toast.error('Your account has been banned.');
          return throwError(() => err);
        }

        // Handle invalidated token - force re-login
        if (status === 401 && code === 'TOKEN_INVALIDATED') {
          authService.logout();
          router.navigate(['/login']);
          toast.error('Session expired. Please log in again.');
          return throwError(() => err);
        }

        // Prefer structured backend error
        const message = body?.message || body?.error || err?.message || 'An unexpected error occurred';

        // For 500 show a generic message
        if (status === 500) {
          toast.error('An unexpected error occurred. Please try again later.');
        } else if (status === 401) {
          // Don't show toast for 401 on auth endpoints
          if (!req.url.includes('/auth/')) {
            toast.error('Please log in to continue.');
          }
        } else {
          toast.error(message);
        }
      } catch (e) {
        toast.error('An unexpected error occurred.');
      }
      return throwError(() => err);
    })
  );
};
