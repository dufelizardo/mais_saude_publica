import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TabelaSalarialRequestDto, TabelaSalarialResponseDto } from '../models/tabela-salarial';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class TabelaSalarialService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/tabela-salarial';

  listarPorCargo(cargoId: string): Observable<TabelaSalarialResponseDto[]> {
    return this.http.get<TabelaSalarialResponseDto[]>(`${this.baseUrl}/cargo/${cargoId}`);
  }

  buscarVigente(cargoId: string): Observable<TabelaSalarialResponseDto> {
    return this.http.get<TabelaSalarialResponseDto>(`${this.baseUrl}/cargo/${cargoId}/vigente`);
  }

  criar(dto: TabelaSalarialRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
