import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AtendimentoRequestDto, AtendimentoResponseDto } from '../models/atendimento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AtendimentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/atendimento';

  listar(): Observable<AtendimentoResponseDto[]> {
    return this.http.get<AtendimentoResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<AtendimentoResponseDto> {
    return this.http.get<AtendimentoResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: AtendimentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: AtendimentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
