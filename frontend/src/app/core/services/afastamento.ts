import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AfastamentoRequestDto, AfastamentoResponseDto } from '../models/afastamento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AfastamentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/afastamento';

  listarPorProfissional(matricula: string): Observable<AfastamentoResponseDto[]> {
    return this.http.get<AfastamentoResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: AfastamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
