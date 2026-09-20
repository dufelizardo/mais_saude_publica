import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AcidenteTrabalhoRequestDto, AcidenteTrabalhoResponseDto } from '../models/acidente-trabalho';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AcidenteTrabalhoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/acidente-trabalho';

  listarPorProfissional(matricula: string): Observable<AcidenteTrabalhoResponseDto[]> {
    return this.http.get<AcidenteTrabalhoResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: AcidenteTrabalhoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
