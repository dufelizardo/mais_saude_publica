import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AvaliacaoImpactoRequestDto,
  EmergenciaDto,
  EventoEmergencialDto,
  FonteEventoEmergencial,
  NivelEmergenciaDto,
  SituacaoEmergencia,
  SituacaoEventoEmergencial,
  SugestaoImpactoDto,
  TipoDecretoEmergencia,
  TipoEmergenciaDto,
} from '../models/emergencia';

/** Emergências e Desastres, fatia E1 (ADR-0156). */
@Injectable({ providedIn: 'root' })
export class EmergenciaService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  tipos(): Observable<TipoEmergenciaDto[]> {
    return this.http.get<TipoEmergenciaDto[]>(`${this.api}/emergencias/tipos`);
  }

  niveis(): Observable<NivelEmergenciaDto[]> {
    return this.http.get<NivelEmergenciaDto[]>(`${this.api}/emergencias/niveis`);
  }

  eventos(situacao?: SituacaoEventoEmergencial): Observable<EventoEmergencialDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : undefined;
    return this.http.get<EventoEmergencialDto[]>(`${this.api}/eventos-emergenciais`, { params });
  }

  registrarEvento(dto: { tipoId: string; fonte: FonteEventoEmergencial; descricao: string; local?: string | null; ocorridoEm?: string | null; geometria?: string | null; surtoId?: string | null }): Observable<EventoEmergencialDto> {
    return this.http.post<EventoEmergencialDto>(`${this.api}/evento-emergencial`, dto);
  }

  situacaoDoEvento(uuid: string, situacao: 'MONITORADO' | 'DESCARTADO', motivo: string | null): Observable<EventoEmergencialDto> {
    return this.http.post<EventoEmergencialDto>(`${this.api}/evento-emergencial/${uuid}/situacao`, { situacao, motivo });
  }

  converter(eventoId: string, dto: { nome: string; nivelId: string; justificativa: string; responsavelMatricula?: string | null }): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/evento-emergencial/${eventoId}/emergencia`, dto);
  }

  emergencias(situacao?: SituacaoEmergencia): Observable<EmergenciaDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : undefined;
    return this.http.get<EmergenciaDto[]>(`${this.api}/emergencias`, { params });
  }

  emergencia(uuid: string): Observable<EmergenciaDto> {
    return this.http.get<EmergenciaDto>(`${this.api}/emergencia/${uuid}`);
  }

  mudarNivel(uuid: string, nivelId: string, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/nivel`, { nivelId, justificativa });
  }

  territorio(uuid: string, geometria: string | null): Observable<EmergenciaDto> {
    return this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/territorio`, { geometria, microareaIds: null });
  }

  referencias(uuid: string, dto: { espinNumero: string | null; espinData: string | null; decretoTipo: TipoDecretoEmergencia | null; decretoNumero: string | null; decretoData: string | null }): Observable<EmergenciaDto> {
    return this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/referencias-legais`, dto);
  }

  encerrar(uuid: string, motivo: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/encerramento`, { motivo });
  }

  sugestao(uuid: string): Observable<SugestaoImpactoDto> {
    return this.http.get<SugestaoImpactoDto>(`${this.api}/emergencia/${uuid}/sugestao-impacto`);
  }

  avaliar(uuid: string, dto: AvaliacaoImpactoRequestDto): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/avaliacao-impacto`, dto);
  }
}
