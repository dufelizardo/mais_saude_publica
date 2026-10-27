import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  EsperaAtendimentoDto,
  FiltroProducao,
  GranularidadeSerie,
  OcorrenciaEscalaDto,
  OcupacaoAgendaDto,
  ProgramaPorUnidadeDto,
  ProgramasNaRedeDto,
  ProducaoAssistencialDto,
  ProducaoUnidadeDto,
  SerieProducaoDto,
  SituacaoAgoraDto,
  VigilanciaNaRedeDto,
} from '../models/producao';

/** Sala de Situação: produção (BI1, ADR-0143), situação operacional (BI2, ADR-0144) e indicadores da rede (BI3, ADR-0146). */
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

  // ── Indicadores da rede (BI3, ADR-0146) ──

  programasNaRede(filtro: FiltroProducao = {}): Observable<ProgramasNaRedeDto> {
    return this.http.get<ProgramasNaRedeDto>('/api/v1/indicadores-rede/programas', { params: params(filtro) });
  }

  programaPorUnidade(programaId: string, filtro: FiltroProducao = {}): Observable<ProgramaPorUnidadeDto> {
    return this.http.get<ProgramaPorUnidadeDto>(`/api/v1/indicadores-rede/programa/${programaId}/unidades`, { params: params({ de: filtro.de, ate: filtro.ate }) });
  }

  vigilanciaNaRede(filtro: FiltroProducao = {}): Observable<VigilanciaNaRedeDto> {
    return this.http.get<VigilanciaNaRedeDto>('/api/v1/indicadores-rede/vigilancia', { params: params(filtro) });
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
