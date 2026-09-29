import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProcedimentoRequestDto, ProcedimentoResponseDto } from '../models/procedimento';
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

  /** Registro clínico não é editado: a retificação grava uma nova versão ligada à anterior (ADR-0062). */
  retificar(uuid: string, dto: ProcedimentoRequestDto & { motivoRetificacao: string }): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/retificacao`, dto);
  }
}
