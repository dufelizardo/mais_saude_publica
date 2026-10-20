import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AgravoDto,
  AgravoRequestDto,
  BoletimVigilanciaDto,
  CampoFichaDto,
  NotificacaoDto,
  NotificacaoNegativaDto,
  NotificacaoRequestDto,
  NotificacoesDto,
  SemanasEpidemiologicasDto,
  SugestaoNotificacaoDto,
  ContatoRequestDto,
  MapaVigilanciaDto,
} from '../models/vigilancia';

/** Vigilância epidemiológica, fatia V1 (ADR-0124). */
@Injectable({ providedIn: 'root' })
export class VigilanciaService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/vigilancia';

  agravos(): Observable<AgravoDto[]> {
    return this.http.get<AgravoDto[]>(`${this.api}/agravos`);
  }

  criarAgravo(dto: AgravoRequestDto): Observable<AgravoDto> {
    return this.http.post<AgravoDto>(`${this.api}/agravo`, dto);
  }

  atualizarAgravo(uuid: string, dto: AgravoRequestDto): Observable<AgravoDto> {
    return this.http.patch<AgravoDto>(`${this.api}/agravo/${uuid}`, dto);
  }

  novaFicha(uuid: string, campos: CampoFichaDto[]): Observable<AgravoDto> {
    return this.http.post<AgravoDto>(`${this.api}/agravo/${uuid}/ficha`, { campos });
  }

  notificar(dto: NotificacaoRequestDto): Observable<NotificacaoDto> {
    return this.http.post<NotificacaoDto>(`${this.api}/notificacao`, dto);
  }

  notificacoes(filtro: { unidadeId?: string; agravoId?: string; situacao?: string; atrasadas?: boolean } = {}): Observable<NotificacoesDto> {
    let params = new HttpParams();
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.agravoId) params = params.set('agravoId', filtro.agravoId);
    if (filtro.situacao) params = params.set('situacao', filtro.situacao);
    if (filtro.atrasadas) params = params.set('atrasadas', 'true');
    return this.http.get<NotificacoesDto>(`${this.api}/notificacoes`, { params });
  }

  notificacao(uuid: string): Observable<NotificacaoDto> {
    return this.http.get<NotificacaoDto>(`${this.api}/notificacao/${uuid}`);
  }

  investigar(uuid: string, dto: { exposicao?: string | null; localProvavelInfeccao?: string | null; medidas?: string | null; examesIds?: string[] }): Observable<NotificacaoDto> {
    return this.http.post<NotificacaoDto>(`${this.api}/notificacao/${uuid}/investigacao`, dto);
  }

  classificar(uuid: string, dto: { classificacao: string; criterio?: string | null; justificativa?: string | null }): Observable<NotificacaoDto> {
    return this.http.post<NotificacaoDto>(`${this.api}/notificacao/${uuid}/classificacao`, dto);
  }

  encerrar(uuid: string, evolucao: string): Observable<NotificacaoDto> {
    return this.http.post<NotificacaoDto>(`${this.api}/notificacao/${uuid}/encerramento`, { evolucao });
  }

  sugestoes(unidadeId?: string): Observable<SugestaoNotificacaoDto[]> {
    let params = new HttpParams();
    if (unidadeId) params = params.set('unidadeId', unidadeId);
    return this.http.get<SugestaoNotificacaoDto[]>(`${this.api}/sugestoes`, { params });
  }

  declararNegativa(unidadeId: string, ano?: number, semana?: number): Observable<NotificacaoNegativaDto> {
    return this.http.post<NotificacaoNegativaDto>(`${this.api}/notificacao-negativa`, { unidadeId, ano: ano ?? null, semana: semana ?? null });
  }

  semanas(unidadeId?: string): Observable<SemanasEpidemiologicasDto> {
    let params = new HttpParams();
    if (unidadeId) params = params.set('unidadeId', unidadeId);
    return this.http.get<SemanasEpidemiologicasDto>(`${this.api}/semanas`, { params });
  }

  boletim(unidadeId?: string): Observable<BoletimVigilanciaDto> {
    let params = new HttpParams();
    if (unidadeId) params = params.set('unidadeId', unidadeId);
    return this.http.get<BoletimVigilanciaDto>(`${this.api}/boletim`, { params });
  }

  // ── Contatos, local e mapa (ADR-0130) ─────────────────────────────────────────────────────────

  marcarLocal(uuid: string, latitude: number, longitude: number): Observable<NotificacaoDto> {
    return this.http.post<NotificacaoDto>(`${this.api}/notificacao/${uuid}/local`, { latitude, longitude });
  }

  adicionarContato(uuid: string, dto: ContatoRequestDto): Observable<NotificacaoDto> {
    return this.http.post<NotificacaoDto>(`${this.api}/notificacao/${uuid}/contato`, dto);
  }

  atualizarContato(uuid: string, dto: ContatoRequestDto): Observable<NotificacaoDto> {
    return this.http.patch<NotificacaoDto>(`${this.api}/contato/${uuid}`, dto);
  }

  mapa(filtro: { agravoId?: string; unidadeId?: string; de?: string; ate?: string; classificacao?: string } = {}): Observable<MapaVigilanciaDto> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(filtro)) if (v) params = params.set(k, v);
    return this.http.get<MapaVigilanciaDto>(`${this.api}/mapa`, { params });
  }
}
