import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  EsperaAtendimentoDto,
  FiltroProducao,
  GranularidadeSerie,
  OcorrenciaEscalaDto,
  OcupacaoAgendaDto,
  ProducaoAssistencialDto,
  ProducaoUnidadeDto,
  SerieProducaoDto,
  SituacaoAgoraDto,
} from '../models/producao';

/** Sala de Situação: produção assistencial (BI1, ADR-0143) e situação operacional (BI2, ADR-0144). */
@Injectable({ providedIn: 'root' })
export class ProducaoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/producao';

  producao(filtro: FiltroProducao = {}): Observable<ProducaoAssistencialDto> {
    return this.http.get<ProducaoAssistencialDto>(this.api, { params: params(filtro) });
  }

  serie(granularidade: GranularidadeSerie, unidadeId?: string): Observable<SerieProducaoDto> {
    return this.http.get<SerieProducaoDto>(`${this.api}/serie`, { params: params({ granularidade, unidadeId }) });
  }

  unidades(filtro: FiltroProducao = {}): Observable<ProducaoUnidadeDto[]> {
    return this.http.get<ProducaoUnidadeDto[]>(`${this.api}/unidades`, { params: params({ de: filtro.de, ate: filtro.ate }) });
  }

  exportar(filtro: FiltroProducao = {}): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.api}/exportacao`, { params: params(filtro), responseType: 'blob', observe: 'response' });
  }

  // ── Situação operacional (BI2, ADR-0144) ──

  situacao(unidadeId?: string): Observable<SituacaoAgoraDto> {
    return this.http.get<SituacaoAgoraDto>('/api/v1/situacao', { params: params({ unidadeId }) });
  }

  espera(filtro: FiltroProducao = {}): Observable<EsperaAtendimentoDto> {
    return this.http.get<EsperaAtendimentoDto>('/api/v1/situacao/espera', { params: params(filtro) });
  }

  ocupacao(unidadeId?: string): Observable<OcupacaoAgendaDto> {
    return this.http.get<OcupacaoAgendaDto>('/api/v1/situacao/ocupacao', { params: params({ unidadeId }) });
  }

  escala(unidadeId?: string): Observable<OcorrenciaEscalaDto[]> {
    return this.http.get<OcorrenciaEscalaDto[]>('/api/v1/situacao/escala', { params: params({ unidadeId }) });
  }
}

function params(f: object): HttpParams {
  let p = new HttpParams();
  for (const [k, v] of Object.entries(f) as [string, string | undefined][]) {
    if (v) {
      p = p.set(k, v);
    }
  }
  return p;
}
