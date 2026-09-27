import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth';

/**
 * Aplicado na rota-wrapper do AppShell (ver app.routes.ts) — protege todas as telas internas de
 * uma vez só. Se o toggle `app.security.enabled` estiver desligado (local/CI, e qualquer ambiente
 * ainda não ligado — ver ADR-0055), libera sempre: é exatamente o comportamento "login desligado
 * localmente" pedido. Se estiver ligado, exige um token já armazenado, senão redireciona para
 * /login levando a tela pedida em `returnUrl`, para voltar a ela depois de entrar.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.securityEnabled().pipe(
    map((habilitado) => {
      if (!habilitado || authService.estaAutenticado()) {
        return true;
      }
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }),
  );
};
