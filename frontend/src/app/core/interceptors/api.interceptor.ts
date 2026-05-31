import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { ApiErrorResponse } from '../models/api-response.model';

/**
 * Functional HTTP interceptor — applied globally via provideHttpClient(withInterceptors([...])).
 *
 * Responsibilities:
 *  - Attach JSON content/accept headers to every outbound request.
 *  - Catch HTTP errors and surface a user-visible toast notification.
 *  - Re-throw the original error so the calling component's error handler can also react.
 */
export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  const apiReq = req.clone({
    setHeaders: {
      'Content-Type': 'application/json',
      'Accept':       'application/json',
    },
  });

  return next(apiReq).pipe(
    catchError((err: HttpErrorResponse) => {
      toast.error(resolveMessage(err));
      return throwError(() => err);
    }),
  );
};

function resolveMessage(err: HttpErrorResponse): string {
  if (err.status === 0) {
    return 'Cannot reach the server. Please check your connection.';
  }

  const body = err.error as ApiErrorResponse | null;
  if (body?.message) {
    return body.message;
  }

  switch (err.status) {
    case 400: return 'Bad request — please check your input.';
    case 401: return 'Unauthorised. Please log in.';
    case 403: return 'Access forbidden.';
    case 404: return 'The requested resource was not found.';
    case 422: return 'The request could not be processed.';
    case 500: return 'A server error occurred. Please try again later.';
    default:  return `Unexpected error (HTTP ${err.status}).`;
  }
}
