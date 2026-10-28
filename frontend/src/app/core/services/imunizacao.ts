import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AtrasadoVacinacaoDto,
  CadernetaVacinacaoDto,
  EsquemaVacinalDto,
  ImunobiologicoDto,
  RegistroVacinacaoDto,
  RegistroVacinacaoRequestDto,
  VacinacaoDoDiaDto,
} from '../models/imunizacao';

/** Imunização (ADR-0147): catálogo, calendário, registro de vacinação, caderneta e sala de vacina. */
@Injectable({ providedIn: 'root' })
export class ImunizacaoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  imunobiologicos(): Observable<ImunobiologicoDto[]> {
    return this.http.get<ImunobiologicoDto[]>(`${this.api}/imunobiologicos`);
  }

  calendario(): Observable<EsquemaVacinalDto[]> {
    return this.http.get<EsquemaVacinalDto[]>(`${this.api}/calendario-vacinal`);
  }

  registrar(dto: RegistroVacinacaoRequestDto): Observable<RegistroVacinacaoDto> {
    return this.http.post<RegistroVacinacaoDto>(`${this.api}/vacinacao`, dto);
  }

  retificar(uuid: string, dto: RegistroVacinacaoRequestDto): Observable<RegistroVacinacaoDto> {
    return this.http.post<RegistroVacinacaoDto>(`${this.api}/vacinacao/${uuid}/retificacao`, dto);
  }

  anular(uuid: string, motivo: string): Observable<RegistroVacinacaoDto> {
    return this.http.post<RegistroVacinacaoDto>(`${this.api}/vacinacao/${uuid}/anulacao`, { motivo });
  }

  caderneta(pacienteId: string): Observable<CadernetaVacinacaoDto> {
    return this.http.get<CadernetaVacinacaoDto>(`${this.api}/paciente/${pacienteId}/vacinacao`);
  }

  doDia(unidadeId: string, data?: string): Observable<VacinacaoDoDiaDto> {
    let params = new HttpParams().set('unidadeId', unidadeId);
    if (data) {
      params = params.set('data', data);
    }
    return this.http.get<VacinacaoDoDiaDto>(`${this.api}/vacinacao/dia`, { params });
  }

  atrasados(unidadeId: string): Observable<AtrasadoVacinacaoDto[]> {
    return this.http.get<AtrasadoVacinacaoDto[]>(`${this.api}/vacinacao/atrasados`, { params: new HttpParams().set('unidadeId', unidadeId) });
  }
}
