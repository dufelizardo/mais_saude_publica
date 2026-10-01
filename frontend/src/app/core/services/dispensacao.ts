import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DispensacaoRequestDto, DispensacaoResponseDto } from '../models/dispensacao';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class DispensacaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/dispensacao';

  listar(): Observable<DispensacaoResponseDto[]> {
    return this.http.get<DispensacaoResponseDto[]>(`${this.baseUrl}/`);
  }

  /** Responde 422 quando o lote não tem saldo — nada é gravado. */
  criar(dto: DispensacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
