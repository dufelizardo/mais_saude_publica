import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FiltroRelatorio, RelatorioCatalogoDto, RelatorioDto } from '../models/relatorio';

function params(filtro: FiltroRelatorio): HttpParams {
  let p = new HttpParams();
  for (const [chave, valor] of Object.entries(filtro)) if (valor) p = p.set(chave, valor);
  return p;
}

/** Relatórios (BI4, ADR-0167): o catálogo, a prévia e o CSV. */
@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/relatorios';

  catalogo(): Observable<RelatorioCatalogoDto[]> {
    return this.http.get<RelatorioCatalogoDto[]>(this.api);
  }

  gerar(codigo: string, filtro: FiltroRelatorio): Observable<RelatorioDto> {
    return this.http.get<RelatorioDto>(`${this.api}/${codigo}`, { params: params(filtro) });
  }

  exportar(codigo: string, filtro: FiltroRelatorio): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.api}/${codigo}/exportacao`, { params: params(filtro), responseType: 'blob', observe: 'response' });
  }
}
