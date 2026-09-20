import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LotacaoRequestDto, LotacaoResponseDto } from '../models/lotacao';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class LotacaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/lotacao';

  criar(dto: LotacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  listarHistorico(matricula: string): Observable<LotacaoResponseDto[]> {
    return this.http.get<LotacaoResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  buscarVigente(matricula: string): Observable<LotacaoResponseDto> {
    return this.http.get<LotacaoResponseDto>(`${this.baseUrl}/profissional/${matricula}/atual`);
  }
}
