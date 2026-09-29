import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TriagemRequestDto, TriagemResponseDto } from '../models/triagem';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class TriagemService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/triagem';

  buscarPorId(uuid: string): Observable<TriagemResponseDto> {
    return this.http.get<TriagemResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: TriagemRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  /** Registro clínico não é editado: a retificação grava uma nova versão ligada à anterior (ADR-0062). */
  retificar(uuid: string, dto: TriagemRequestDto & { motivoRetificacao: string }): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/retificacao`, dto);
  }
}
