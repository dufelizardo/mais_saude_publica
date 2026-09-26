import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of, shareReplay, tap } from 'rxjs';
import { LoginRequestDto, LoginResponseDto, SecurityStatusResponseDto } from '../models/auth';

const TOKEN_KEY = 'msp_token';

/**
 * Autenticação (ADR-0055) — login, armazenamento do token e a fonte única de verdade
 * (`GET /auth/status`) que diz se o toggle `app.security.enabled` está ligado neste ambiente.
 * Sem RBAC ainda: só "autenticado ou não" (ver ADR-0054 para o modelo completo, ainda não
 * implementado).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auth';

  private securityEnabled$?: Observable<boolean>;

  login(dto: LoginRequestDto): Observable<LoginResponseDto> {
    return this.http.post<LoginResponseDto>(`${this.baseUrl}/login`, dto).pipe(
      tap((resposta) => this.armazenarToken(resposta.token, dto.manterConectado)),
    );
  }

  logout(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(TOKEN_KEY);
    } catch {
      // Storage indisponível (modo privado, etc.) — não há token para limpar de qualquer forma.
    }
  }

  obterToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY) ?? sessionStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }

  estaAutenticado(): boolean {
    return this.obterToken() !== null;
  }

  /**
   * Cacheada (`shareReplay(1)`) — o valor não muda durante a vida da aba, então uma única
   * chamada HTTP basta para todas as consultas do guard/app. Se a chamada falhar (backend fora do
   * ar, por exemplo em `ng serve` sem o Spring Boot local rodando), assume desligado em vez de
   * travar a navegação — mesma experiência "login desligado localmente" que o toggle já dá quando
   * o backend responde `false` de propósito.
   */
  securityEnabled(): Observable<boolean> {
    if (!this.securityEnabled$) {
      this.securityEnabled$ = this.http.get<SecurityStatusResponseDto>(`${this.baseUrl}/status`).pipe(
        map((resposta) => resposta.securityEnabled),
        catchError(() => of(false)),
        shareReplay(1),
      );
    }
    return this.securityEnabled$;
  }

  private armazenarToken(token: string, manterConectado: boolean): void {
    this.logout();
    try {
      if (manterConectado) {
        localStorage.setItem(TOKEN_KEY, token);
      } else {
        sessionStorage.setItem(TOKEN_KEY, token);
      }
    } catch {
      // Storage indisponível — login funciona só durante o ciclo de vida da página atual.
    }
  }
}
