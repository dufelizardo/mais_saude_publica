import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CalculoRescisaoRequestDto, CalculoRescisaoResponseDto } from '../models/calculo-rescisao';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class CalculoRescisaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/calculo-rescisao';

  listarPorProfissional(matricula: string): Observable<CalculoRescisaoResponseDto[]> {
    return this.http.get<CalculoRescisaoResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: CalculoRescisaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
