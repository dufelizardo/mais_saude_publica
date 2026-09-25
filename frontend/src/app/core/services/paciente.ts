import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PacienteRequestDto, PacienteResponseDto } from '../models/paciente';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class PacienteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/paciente';

  listar(): Observable<PacienteResponseDto[]> {
    return this.http.get<PacienteResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<PacienteResponseDto> {
    return this.http.get<PacienteResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  buscarPorCpf(cpf: string): Observable<PacienteResponseDto[]> {
    return this.http.get<PacienteResponseDto[]>(`${this.baseUrl}/cpf/${cpf}`);
  }

  criar(dto: PacienteRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: PacienteRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
