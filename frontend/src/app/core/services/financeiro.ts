import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DotacaoDto, EmpenhoDto, FonteRecursoSaude, TipoAlteracaoDotacao, TipoEmpenho, TipoMovimentoEmpenho } from '../models/financeiro';

/** Financeiro, fatia F1 (ADR-0218). */
@Injectable({ providedIn: 'root' })
export class FinanceiroService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/financeiro';

  dotacoes(exercicio?: number): Observable<DotacaoDto[]> {
    return this.http.get<DotacaoDto[]>(`${this.api}/dotacoes`, { params: exercicio ? { exercicio } : {} });
  }

  criarDotacao(dto: { exercicio: number; programaTrabalho: string; descricao: string; naturezaDespesa: string; fonte: FonteRecursoSaude;
    fonteCodigo: string | null; valorInicial: number }): Observable<DotacaoDto> {
    return this.http.post<DotacaoDto>(`${this.api}/dotacao`, dto);
  }

  alterarDotacao(uuid: string, dto: { tipo: TipoAlteracaoDotacao; valor: number; ato: string; data: string; motivo: string | null }): Observable<DotacaoDto> {
    return this.http.post<DotacaoDto>(`${this.api}/dotacao/${uuid}/alteracao`, dto);
  }

  empenhos(exercicio?: number): Observable<EmpenhoDto[]> {
    return this.http.get<EmpenhoDto[]>(`${this.api}/empenhos`, { params: exercicio ? { exercicio } : {} });
  }

  empenho(uuid: string): Observable<EmpenhoDto> {
    return this.http.get<EmpenhoDto>(`${this.api}/empenho/${uuid}`);
  }

  disponiveis(instrumentoId: string): Observable<EmpenhoDto[]> {
    return this.http.get<EmpenhoDto[]>(`${this.api}/empenhos-disponiveis`, { params: { instrumentoId } });
  }

  empenhar(dto: { numero: string; tipo: TipoEmpenho; dotacaoId: string; fornecedorId: string; instrumentoId: string | null; valor: number; data: string;
    historico: string | null }): Observable<EmpenhoDto> {
    return this.http.post<EmpenhoDto>(`${this.api}/empenho`, dto);
  }

  movimentar(uuid: string, dto: { tipo: TipoMovimentoEmpenho; valor: number; data: string; motivo: string }): Observable<EmpenhoDto> {
    return this.http.post<EmpenhoDto>(`${this.api}/empenho/${uuid}/movimento`, dto);
  }
}
