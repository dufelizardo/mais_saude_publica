import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth';

/**
 * Anexa o token nas chamadas `/api/**` quando existir (não faz diferença quando o toggle está
 * desligado — o backend ignora, ver ADR-0055). Em 401, limpa o token e manda pra tela de login:
 * cobre tanto o caso "token expirou" quanto "toggle foi ligado enquanto a aba estava aberta".
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.obterToken();
  const requisicao = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(requisicao).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401 && !router.url.startsWith('/login')) {
        authService.logout();
        router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
      }
      return throwError(() => erro);
    }),
  );
};
