import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MotivoSaidaAcompanhamento, SituacaoAcompanhamento } from '../models/programa';
import {
  AcolhimentoSaudeMentalDto,
  AcolhimentoSaudeMentalRequestDto,
  AcompanhamentoSaudeMentalDto,
  AvaliacaoSaudeMentalRequestDto,
  CasosSaudeMentalDto,
} from '../models/saude-mental';

/** Saúde Mental, fatia SM1 (ADR-0120): acolhimento, caso, referência, avaliação e saída. */
@Injectable({ providedIn: 'root' })
export class SaudeMentalService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/saude-mental';

  casos(filtro: { unidadeId?: string; pacienteId?: string; escopo?: 'TECNICO' | 'EQUIPE' | ''; situacao?: SituacaoAcompanhamento } = {}): Observable<CasosSaudeMentalDto> {
    let params = new HttpParams();
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.pacienteId) params = params.set('pacienteId', filtro.pacienteId);
    if (filtro.escopo) params = params.set('escopo', filtro.escopo);
    if (filtro.situacao) params = params.set('situacao', filtro.situacao);
    return this.http.get<CasosSaudeMentalDto>(`${this.api}/acompanhamentos`, { params });
  }

  caso(uuid: string): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.get<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}`);
  }

  acolher(dto: AcolhimentoSaudeMentalRequestDto): Observable<AcolhimentoSaudeMentalDto> {
    return this.http.post<AcolhimentoSaudeMentalDto>(`${this.api}/acolhimento`, dto);
  }

  trocarReferencia(uuid: string, dto: { equipeId: string; tecnicoMatricula: string; motivo: string }): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/referencia`, dto);
  }

  avaliar(uuid: string, dto: AvaliacaoSaudeMentalRequestDto): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/avaliacao`, dto);
  }

  encerrar(uuid: string, motivoSaida: MotivoSaidaAcompanhamento, observacao: string, fim?: string | null): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/encerramento`, { motivoSaida, observacao, fim: fim || null });
  }
}
