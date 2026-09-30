import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, combineLatest, map, of, shareReplay, tap } from 'rxjs';
import {
  AcessoDaInterface,
  LoginRequestDto,
  LoginResponseDto,
  SecurityStatusResponseDto,
  TrocaSenhaRequestDto,
  UsuarioAtualResponseDto,
} from '../models/auth';

const TOKEN_KEY = 'msp_token';

/**
 * Autenticação (ADR-0055) — login, armazenamento do token e a fonte única de verdade
 * (`GET /auth/status`) que diz se o toggle `app.security.enabled` está ligado neste ambiente — e, com a
 * autorização ligada (ADR-0067), as permissões que decidem o que a interface mostra (ADR-0068).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auth';

  private status$?: Observable<SecurityStatusResponseDto>;
  private usuarioAtual$?: Observable<UsuarioAtualResponseDto | null>;

  login(dto: LoginRequestDto): Observable<LoginResponseDto> {
    return this.http.post<LoginResponseDto>(`${this.baseUrl}/login`, dto).pipe(
      tap((resposta) => {
        this.armazenarToken(resposta.token, dto.manterConectado);
        this.usuarioAtual$ = undefined;
      }),
    );
  }

  /**
   * Troca a própria senha (ADR-0069). O token novo — sem a restrição de senha provisória — fica no mesmo
   * lugar do anterior, mantendo a escolha "manter conectado" do login.
   */
  trocarSenha(senhaAtual: string, novaSenha: string): Observable<LoginResponseDto> {
    const manterConectado = this.tokenPersistente();
    const dto: TrocaSenhaRequestDto = { senhaAtual, novaSenha, manterConectado };
    return this.http.post<LoginResponseDto>(`${this.baseUrl}/senha`, dto).pipe(
      tap((resposta) => this.armazenarToken(resposta.token, manterConectado)),
    );
  }

  private tokenPersistente(): boolean {
    try {
      return localStorage.getItem(TOKEN_KEY) !== null;
    } catch {
      return false;
    }
  }

  logout(): void {
    this.usuarioAtual$ = undefined;
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
    return this.status().pipe(map((s) => s.securityEnabled));
  }

  private status(): Observable<SecurityStatusResponseDto> {
    if (!this.status$) {
      this.status$ = this.http.get<SecurityStatusResponseDto>(`${this.baseUrl}/status`).pipe(
        catchError(() => of({ securityEnabled: false, authorizationEnabled: false })),
        shareReplay(1),
      );
    }
    return this.status$;
  }

  /**
   * O que a interface mostra (ADR-0068). Com a autorização desligada — local, CI e ambientes que ainda não
   * ligaram — nada é escondido; ligada, só o que as permissões de `/auth/eu` liberam. Quem decide continua
   * sendo a API: esconder só evita oferecer o que responderia 403.
   */
  acessoDaInterface(): Observable<AcessoDaInterface> {
    return combineLatest([this.status(), this.usuarioAtual()]).pipe(
      map(([status, usuario]) => ({
        restrito: !!status.securityEnabled && !!status.authorizationEnabled,
        permissoes: new Set(usuario?.permissoes ?? []),
      })),
    );
  }

  /**
   * Quem está logado (ADR-0065), com a matrícula do profissional de mesmo CPF — as telas usam para
   * preencher o profissional dos registros. Nulo sem login (inclusive com o toggle desligado, em que o
   * backend responde 401). Cacheado até o próximo login/logout.
   */
  usuarioAtual(): Observable<UsuarioAtualResponseDto | null> {
    if (!this.usuarioAtual$) {
      this.usuarioAtual$ = this.estaAutenticado()
        ? this.http.get<UsuarioAtualResponseDto>(`${this.baseUrl}/eu`).pipe(
            catchError(() => of(null)),
            shareReplay(1),
          )
        : of(null);
    }
    return this.usuarioAtual$;
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
