import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AvaliacaoRequestDto,
  AvaliacaoResponseDto,
  CicloAvaliacaoRequestDto,
  CicloAvaliacaoResponseDto,
} from '../models/avaliacao';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AvaliacaoService {
  private readonly http = inject(HttpClient);

  listarCiclos(): Observable<CicloAvaliacaoResponseDto[]> {
    return this.http.get<CicloAvaliacaoResponseDto[]>('/api/v1/ciclo-avaliacao/');
  }

  criarCiclo(dto: CicloAvaliacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>('/api/v1/ciclo-avaliacao/', dto);
  }

  /** Edição do ciclo (ADR-0083). */
  atualizarCiclo(uuid: string, dto: CicloAvaliacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`/api/v1/ciclo-avaliacao/${uuid}`, dto);
  }

  listarPorProfissional(matricula: string): Observable<AvaliacaoResponseDto[]> {
    return this.http.get<AvaliacaoResponseDto[]>(`/api/v1/avaliacao/profissional/${matricula}`);
  }

  criar(dto: AvaliacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>('/api/v1/avaliacao/', dto);
  }
}
