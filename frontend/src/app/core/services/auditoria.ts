import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FiltroAuditoria, PaginaAuditoriaResponseDto } from '../models/auditoria';

/** Consulta da trilha de auditoria (ADR-0071). */
@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auditoria';

  consultar(filtro: FiltroAuditoria): Observable<PaginaAuditoriaResponseDto> {
    return this.http.get<PaginaAuditoriaResponseDto>(`${this.baseUrl}/`, { params: this.params(filtro) });
  }

  /** A trilha filtrada em CSV, com o nome do arquivo no Content-Disposition (ADR-0082). */
  exportar(filtro: FiltroAuditoria): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}/exportacao`, { params: this.params(filtro), responseType: 'blob', observe: 'response' });
  }

  /** Prazo de guarda da trilha (ADR-0082). */
  politica(): Observable<{ retencaoAnos: number; guardadosDesde?: string }> {
    return this.http.get<{ retencaoAnos: number; guardadosDesde?: string }>(`${this.baseUrl}/politica`);
  }

  private params(filtro: FiltroAuditoria): HttpParams {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor !== undefined && valor !== null && valor !== '') params = params.set(chave, String(valor));
    }
    return params;
  }
}
