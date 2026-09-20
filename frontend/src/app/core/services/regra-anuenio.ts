import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RegraAnuenioRequestDto, RegraAnuenioResponseDto } from '../models/regra-anuenio';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class RegraAnuenioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/regra-anuenio';

  listar(): Observable<RegraAnuenioResponseDto[]> {
    return this.http.get<RegraAnuenioResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: RegraAnuenioRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: RegraAnuenioRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
