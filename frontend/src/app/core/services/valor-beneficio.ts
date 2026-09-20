import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ValorBeneficioRequestDto, ValorBeneficioResponseDto } from '../models/valor-beneficio';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class ValorBeneficioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/valor-beneficio';

  listarPorTipo(tipoBeneficioId: string): Observable<ValorBeneficioResponseDto[]> {
    return this.http.get<ValorBeneficioResponseDto[]>(`${this.baseUrl}/tipo/${tipoBeneficioId}`);
  }

  buscarVigente(tipoBeneficioId: string): Observable<ValorBeneficioResponseDto> {
    return this.http.get<ValorBeneficioResponseDto>(`${this.baseUrl}/tipo/${tipoBeneficioId}/vigente`);
  }

  criar(dto: ValorBeneficioRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
