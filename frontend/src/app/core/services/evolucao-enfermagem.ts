import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EvolucaoEnfermagemRequestDto, EvolucaoEnfermagemResponseDto } from '../models/evolucao-enfermagem';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class EvolucaoEnfermagemService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/evolucao-enfermagem';

  buscarPorId(uuid: string): Observable<EvolucaoEnfermagemResponseDto> {
    return this.http.get<EvolucaoEnfermagemResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: EvolucaoEnfermagemRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  /** Registro clínico não é editado: a retificação grava uma nova versão ligada à anterior (ADR-0062). */
  retificar(uuid: string, dto: EvolucaoEnfermagemRequestDto & { motivoRetificacao: string }): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/retificacao`, dto);
  }
}
