import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TipoBeneficioRequestDto, TipoBeneficioResponseDto } from '../models/tipo-beneficio';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class TipoBeneficioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/tipo-beneficio';

  listar(): Observable<TipoBeneficioResponseDto[]> {
    return this.http.get<TipoBeneficioResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<TipoBeneficioResponseDto> {
    return this.http.get<TipoBeneficioResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: TipoBeneficioRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
