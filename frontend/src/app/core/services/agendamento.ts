import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AgendamentoRequestDto, AgendamentoResponseDto } from '../models/agendamento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AgendamentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/agendamento';

  listar(): Observable<AgendamentoResponseDto[]> {
    return this.http.get<AgendamentoResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<AgendamentoResponseDto> {
    return this.http.get<AgendamentoResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: AgendamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: AgendamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
