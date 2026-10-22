import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ProcedimentoCatalogoDto,
  ProcedimentoCatalogoRequestDto,
  ProcedimentoRequestDto,
  ProcedimentoResponseDto,
  StatusProcedimentoRequestDto,
} from '../models/procedimento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class ProcedimentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/procedimento';

  listar(): Observable<ProcedimentoResponseDto[]> {
    return this.http.get<ProcedimentoResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<ProcedimentoResponseDto> {
    return this.http.get<ProcedimentoResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: ProcedimentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  /** Desfecho de um procedimento agendado — uma vez, de AGENDADO para REALIZADO ou CANCELADO (ADR-0062). */
  alterarStatus(uuid: string, dto: StatusProcedimentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/status`, dto);
  }

  /** Registro clínico não é editado: a retificação grava uma nova versão ligada à anterior (ADR-0062). */
  retificar(uuid: string, dto: ProcedimentoRequestDto & { motivoRetificacao: string }): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/retificacao`, dto);
  }

  /** Catálogo geral de procedimentos (ADR-0136), ativos e inativos, por nome. */
  catalogo(): Observable<ProcedimentoCatalogoDto[]> {
    return this.http.get<ProcedimentoCatalogoDto[]>('/api/v1/procedimentos-catalogo');
  }

  criarItemCatalogo(dto: ProcedimentoCatalogoRequestDto): Observable<ProcedimentoCatalogoDto> {
    return this.http.post<ProcedimentoCatalogoDto>('/api/v1/procedimento-catalogo', dto);
  }

  atualizarItemCatalogo(uuid: string, dto: ProcedimentoCatalogoRequestDto): Observable<ProcedimentoCatalogoDto> {
    return this.http.put<ProcedimentoCatalogoDto>(`/api/v1/procedimento-catalogo/${uuid}`, dto);
  }
}
