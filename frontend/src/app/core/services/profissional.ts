import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProfissionalContatoRequestDto, ProfissionalRequestDto, ProfissionalResponseDto, SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class ProfissionalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/profissional';

  listar(): Observable<ProfissionalResponseDto[]> {
    return this.http.get<ProfissionalResponseDto[]>(`${this.baseUrl}/`);
  }

  create(dto: ProfissionalRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  buscarPorCpf(cpf: string): Observable<ProfissionalResponseDto> {
    return this.http.get<ProfissionalResponseDto>(`${this.baseUrl}/${cpf}`);
  }

  buscarPorMatricula(matricula: string): Observable<ProfissionalResponseDto> {
    return this.http.get<ProfissionalResponseDto>(`${this.baseUrl}/matricula/${matricula}`);
  }

  atualizarContato(cpf: string, dto: ProfissionalContatoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/contato/${cpf}`, dto);
  }

  desligar(cpf: string, dataDesligamento: string): Observable<SuccessResponseDto> {
    return this.http.delete<SuccessResponseDto>(`${this.baseUrl}/des-habilitar/${cpf}`, {
      params: { dataDesligamento },
    });
  }
}
