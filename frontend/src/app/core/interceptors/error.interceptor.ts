import { HttpInterceptorFn } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { inject } from '@angular/core';
import { UIToastService } from '../services/ui-toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(UIToastService);
  return next(req).pipe(
    catchError((err: any) => {
      try {
        // Prefer structured backend error
        const body = err?.error;
        const message = body?.message || body?.error || err?.message || 'An unexpected error occurred';
        // For 500 show a generic message
        const status = err?.status;
        if (status === 500) {
          toast.error('An unexpected error occurred. Please try again later.');
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