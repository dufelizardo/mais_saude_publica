import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FiltroProducao, GranularidadeSerie, ProducaoAssistencialDto, ProducaoUnidadeDto, SerieProducaoDto } from '../models/producao';

/** Produção assistencial por unidade (BI1, ADR-0143): resumo, série, ranking e exportação. */
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
