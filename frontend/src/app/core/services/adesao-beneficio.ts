import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AdesaoBeneficioRequestDto, AdesaoBeneficioResponseDto } from '../models/adesao-beneficio';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AdesaoBeneficioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/adesao-beneficio';

  listarHistorico(matricula: string): Observable<AdesaoBeneficioResponseDto[]> {
    return this.http.get<AdesaoBeneficioResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: AdesaoBeneficioRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  encerrar(uuid: string, dataFim: string): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}/encerrar`, null, { params: { dataFim } });
  }
}
