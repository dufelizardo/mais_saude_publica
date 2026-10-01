import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FiltroAuditoria, PaginaAuditoriaResponseDto } from '../models/auditoria';

/** Consulta da trilha de auditoria (ADR-0071). */
@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auditoria';

  consultar(filtro: FiltroAuditoria): Observable<PaginaAuditoriaResponseDto> {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor !== undefined && valor !== null && valor !== '') params = params.set(chave, String(valor));
    }
    return this.http.get<PaginaAuditoriaResponseDto>(`${this.baseUrl}/`, { params });
  }
}
