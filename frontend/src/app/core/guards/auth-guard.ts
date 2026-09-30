import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, of, switchMap } from 'rxjs';
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
    switchMap((habilitado) => {
      if (!habilitado) {
        return of(true);
      }
      if (!authService.estaAutenticado()) {
        return of(router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } }));
      }
      // Senha provisória (ADR-0069): nenhuma tela interna antes da troca — a API também só atende /auth.
      return authService.usuarioAtual().pipe(
        map((u) => (u?.trocarSenha
          ? router.createUrlTree(['/trocar-senha'], { queryParams: { returnUrl: state.url } })
          : true)),
      );
    }),
  );
};

/**
 * Tela de troca de senha: exige login. Com o login desligado não há senha a trocar, e a tela manda para
 * o início.
 */
export const trocaDeSenhaGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.securityEnabled().pipe(
    map((habilitado) => {
      if (habilitado && authService.estaAutenticado()) return true;
      return router.createUrlTree([habilitado ? '/login' : '/profissionais']);
    }),
  );
};
