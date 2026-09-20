import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LotacaoRequestDto } from '../models/lotacao';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class LotacaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/lotacao';

  criar(dto: LotacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
