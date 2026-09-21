import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ParticipacaoTreinamentoRequestDto,
  ParticipacaoTreinamentoResponseDto,
  TreinamentoRequestDto,
  TreinamentoResponseDto,
} from '../models/treinamento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class TreinamentoService {
  private readonly http = inject(HttpClient);

  listarCatalogo(): Observable<TreinamentoResponseDto[]> {
    return this.http.get<TreinamentoResponseDto[]>('/api/v1/treinamento/');
  }

  criar(dto: TreinamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>('/api/v1/treinamento/', dto);
  }

  listarParticipacoesPorProfissional(matricula: string): Observable<ParticipacaoTreinamentoResponseDto[]> {
    return this.http.get<ParticipacaoTreinamentoResponseDto[]>(`/api/v1/participacao-treinamento/profissional/${matricula}`);
  }

  registrarParticipacao(dto: ParticipacaoTreinamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>('/api/v1/participacao-treinamento/', dto);
  }
}
