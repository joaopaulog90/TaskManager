import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const interceptadorAutenticacao: HttpInterceptorFn = (req, next) => {
  const tokenJwt = localStorage.getItem('token');
  const roteador = inject(Router);

  const requisicaoClonada = tokenJwt
    ? req.clone({ setHeaders: { Authorization: `Bearer ${tokenJwt}` } })
    : req;

  return next(requisicaoClonada).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401 && !req.url.includes('/api/auth/')) {
        localStorage.clear();
        roteador.navigate(['/login']);
      }
      return throwError(() => erro);
    })
  );
};
