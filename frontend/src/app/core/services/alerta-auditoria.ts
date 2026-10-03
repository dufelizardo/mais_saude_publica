import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import {
  AlertaAuditoriaResponseDto,
  AnaliseAlertaAuditoriaRequestDto,
  ResumoAlertasAuditoriaDto,
  SeveridadeAlertaAuditoria,
  StatusAlertaAuditoria,
  TipoAlertaAuditoria,
} from '../models/alerta-auditoria';

/**
 * Alertas da auditoria (ADR-0096 e 0097). Guarda o número de alertas abertos, que o menu mostra e a aba Alertas
 * atualiza depois de cada análise.
 */
@Injectable({ providedIn: 'root' })
export class AlertaAuditoriaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auditoria/alerta';

  /** Alertas abertos no escopo de quem está logado; null antes da primeira leitura ou sem acesso. */
  readonly abertos = signal<number | null>(null);

  listar(filtro: { status?: StatusAlertaAuditoria | ''; tipo?: TipoAlertaAuditoria | ''; severidade?: SeveridadeAlertaAuditoria | '' }): Observable<AlertaAuditoriaResponseDto[]> {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor) params = params.set(chave, valor);
    }
    return this.http.get<AlertaAuditoriaResponseDto[]>(`${this.baseUrl}/`, { params });
  }

  resumo(): Observable<ResumoAlertasAuditoriaDto> {
    return this.http.get<ResumoAlertasAuditoriaDto>(`${this.baseUrl}/resumo`);
  }

  /** Detalhe; a leitura entra na trilha. */
  buscar(uuid: string): Observable<AlertaAuditoriaResponseDto> {
    return this.http.get<AlertaAuditoriaResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  analisar(uuid: string, dto: AnaliseAlertaAuditoriaRequestDto): Observable<AlertaAuditoriaResponseDto> {
    return this.http.post<AlertaAuditoriaResponseDto>(`${this.baseUrl}/${uuid}/analise`, dto);
  }

  /** Roda a detecção na hora. */
  detectar(): Observable<{ novos: number }> {
    return this.http.post<{ novos: number }>(`${this.baseUrl}/deteccao`, {});
  }

  /** Relê o contador do menu. Sem acesso ou com erro, o contador some. */
  atualizarContador(): void {
    this.resumo().pipe(catchError(() => of(null))).subscribe((r) => this.abertos.set(r?.abertos ?? null));
  }
}
