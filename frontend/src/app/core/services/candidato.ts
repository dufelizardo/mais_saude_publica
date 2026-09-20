import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CandidatoRequestDto, CandidatoResponseDto } from '../models/candidato';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class CandidatoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/candidato';

  listarPorVaga(vagaId: string): Observable<CandidatoResponseDto[]> {
    return this.http.get<CandidatoResponseDto[]>(`${this.baseUrl}/vaga/${vagaId}`);
  }

  criar(dto: CandidatoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
