import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LoteAtualizacaoRequestDto, LoteRequestDto, LoteResponseDto } from '../models/lote';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class LoteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/lote';

  listar(): Observable<LoteResponseDto[]> {
    return this.http.get<LoteResponseDto[]>(`${this.baseUrl}/`);
  }

  /** Registra a ENTRADA no livro de movimentação junto com o lote. */
  criar(dto: LoteRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  /** Corrige só número e validade (ADR-0057). */
  corrigir(uuid: string, dto: LoteAtualizacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
