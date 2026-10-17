import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CategoriaProgramaDto, ProgramaDto, ProgramaPublicoDto, ProgramaRequestDto, ProgramaResumoDto } from '../models/programa';

/** Programas de saúde (ADR-0118): catálogo, categorias, participação das unidades e a consulta pública. */
@Injectable({ providedIn: 'root' })
export class ProgramaService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  categorias(): Observable<CategoriaProgramaDto[]> {
    return this.http.get<CategoriaProgramaDto[]>(`${this.api}/categoria-programa/`);
  }

  criarCategoria(dto: { nome: string; ordem?: number | null; ativa: boolean }): Observable<CategoriaProgramaDto> {
    return this.http.post<CategoriaProgramaDto>(`${this.api}/categoria-programa/`, dto);
  }

  atualizarCategoria(uuid: string, dto: { nome: string; ordem?: number | null; ativa: boolean }): Observable<CategoriaProgramaDto> {
    return this.http.patch<CategoriaProgramaDto>(`${this.api}/categoria-programa/${uuid}`, dto);
  }

  listar(filtro: { categoriaId?: string; unidadeId?: string } = {}): Observable<ProgramaResumoDto[]> {
    let params = new HttpParams();
    if (filtro.categoriaId) params = params.set('categoriaId', filtro.categoriaId);
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    return this.http.get<ProgramaResumoDto[]>(`${this.api}/programa/`, { params });
  }

  buscar(uuid: string): Observable<ProgramaDto> {
    return this.http.get<ProgramaDto>(`${this.api}/programa/${uuid}`);
  }

  criar(dto: ProgramaRequestDto): Observable<ProgramaDto> {
    return this.http.post<ProgramaDto>(`${this.api}/programa/`, dto);
  }

  atualizar(uuid: string, dto: ProgramaRequestDto): Observable<ProgramaDto> {
    return this.http.patch<ProgramaDto>(`${this.api}/programa/${uuid}`, dto);
  }

  incluirParticipacao(programaId: string, unidadeId: string, equipeId?: string | null, inicio?: string | null): Observable<ProgramaDto> {
    return this.http.post<ProgramaDto>(`${this.api}/programa/${programaId}/participacao`,
      { unidadeId, equipeId: equipeId || null, inicio: inicio || null });
  }

  encerrarParticipacao(programaId: string, participacaoId: string, motivo: string, fim?: string | null): Observable<ProgramaDto> {
    return this.http.post<ProgramaDto>(`${this.api}/programa/${programaId}/participacao/${participacaoId}/encerramento`, { motivo, fim: fim || null });
  }

  publico(): Observable<ProgramaPublicoDto[]> {
    return this.http.get<ProgramaPublicoDto[]>(`${this.api}/publico/programa`);
  }
}
