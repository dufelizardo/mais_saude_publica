import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  LinhaCuidadoDto,
  LinhaCuidadoRequestDto,
  PainelLinhasCuidadoDto,
  PercursoLinhaDto,
  PercursoLinhaRequestDto,
  SituacaoPercursoLinha,
} from '../models/programa';

/** Linhas de cuidado (ADR-0152): o catálogo com as etapas, o percurso do paciente e o painel. */
@Injectable({ providedIn: 'root' })
export class LinhaCuidadoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  linhas(): Observable<LinhaCuidadoDto[]> {
    return this.http.get<LinhaCuidadoDto[]>(`${this.api}/linhas-de-cuidado`);
  }

  criar(dto: LinhaCuidadoRequestDto): Observable<LinhaCuidadoDto> {
    return this.http.post<LinhaCuidadoDto>(`${this.api}/linha-de-cuidado`, dto);
  }

  atualizar(uuid: string, dto: LinhaCuidadoRequestDto): Observable<LinhaCuidadoDto> {
    return this.http.put<LinhaCuidadoDto>(`${this.api}/linha-de-cuidado/${uuid}`, dto);
  }

  abrir(dto: PercursoLinhaRequestDto): Observable<PercursoLinhaDto> {
    return this.http.post<PercursoLinhaDto>(`${this.api}/percurso-linha-cuidado`, dto);
  }

  percurso(uuid: string): Observable<PercursoLinhaDto> {
    return this.http.get<PercursoLinhaDto>(`${this.api}/percurso-linha-cuidado/${uuid}`);
  }

  registrarEtapa(uuid: string, etapaId: string, tipo: 'CUMPRIDA' | 'DISPENSADA', observacao: string | null): Observable<PercursoLinhaDto> {
    return this.http.post<PercursoLinhaDto>(`${this.api}/percurso-linha-cuidado/${uuid}/etapa`, { etapaId, tipo, observacao });
  }

  encerrar(uuid: string, situacao: SituacaoPercursoLinha, motivo: string | null): Observable<PercursoLinhaDto> {
    return this.http.post<PercursoLinhaDto>(`${this.api}/percurso-linha-cuidado/${uuid}/encerramento`, { situacao, motivo });
  }

  doPaciente(pacienteId: string): Observable<PercursoLinhaDto[]> {
    return this.http.get<PercursoLinhaDto[]>(`${this.api}/paciente/${pacienteId}/linhas-de-cuidado`);
  }

  painel(filtro: { linhaId?: string; unidadeId?: string; atrasados?: boolean } = {}): Observable<PainelLinhasCuidadoDto> {
    let params = new HttpParams();
    if (filtro.linhaId) params = params.set('linhaId', filtro.linhaId);
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.atrasados) params = params.set('atrasados', 'true');
    return this.http.get<PainelLinhasCuidadoDto>(`${this.api}/linhas-de-cuidado/painel`, { params });
  }
}
