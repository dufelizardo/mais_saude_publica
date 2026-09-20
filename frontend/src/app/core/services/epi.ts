import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EpiRequestDto, EpiResponseDto } from '../models/epi';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class EpiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/epi';

  listarPorProfissional(matricula: string): Observable<EpiResponseDto[]> {
    return this.http.get<EpiResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: EpiRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
