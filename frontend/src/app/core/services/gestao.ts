import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AreaGestaoDto, AreaGestaoRequestDto, PapelTitularidade, UnidadeDaAreaDto } from '../models/gestao';

/** Estrutura de gestão (ADR-0175): gerências, coordenações, supervisões e quem responde. */
@Injectable({ providedIn: 'root' })
export class GestaoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/gestao';

  areas(): Observable<AreaGestaoDto[]> {
    return this.http.get<AreaGestaoDto[]>(`${this.api}/area`);
  }

  detalhe(uuid: string): Observable<AreaGestaoDto> {
    return this.http.get<AreaGestaoDto>(`${this.api}/area/${uuid}`);
  }

  regionais(): Observable<UnidadeDaAreaDto[]> {
    return this.http.get<UnidadeDaAreaDto[]>(`${this.api}/regional`);
  }

  criar(dto: AreaGestaoRequestDto): Observable<AreaGestaoDto> {
    return this.http.post<AreaGestaoDto>(`${this.api}/area`, dto);
  }

  atualizar(uuid: string, dto: AreaGestaoRequestDto): Observable<AreaGestaoDto> {
    return this.http.patch<AreaGestaoDto>(`${this.api}/area/${uuid}`, dto);
  }

  designar(uuid: string, dto: { matriculaProfissional: string; papel: PapelTitularidade; inicio: string; ato?: string | null }): Observable<AreaGestaoDto> {
    return this.http.post<AreaGestaoDto>(`${this.api}/area/${uuid}/titularidade`, dto);
  }

  encerrar(titularidadeId: string, fim: string): Observable<AreaGestaoDto> {
    return this.http.post<AreaGestaoDto>(`${this.api}/titularidade/${titularidadeId}/encerramento`, { fim });
  }
}
