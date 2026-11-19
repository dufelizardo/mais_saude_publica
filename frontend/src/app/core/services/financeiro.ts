import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  BeneficiarioDiariaTfd, DiariaTfdDto, DotacaoDto, EmpenhoDto, ExecucaoDto, FonteRecursoSaude, LiquidacaoDto, OrigemLiquidacao, PendenciaLiquidacaoDto,
  RestoAPagarDto, TipoAlteracaoDotacao, TipoEmpenho, TipoMovimentoEmpenho,
} from '../models/financeiro';

/** Financeiro: F1 dotação e empenho (ADR-0218) e F2 liquidação, pagamento, diárias do TFD e execução (ADR-0219). */
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

  // ── F2 (ADR-0219) ──

  aLiquidar(): Observable<PendenciaLiquidacaoDto[]> {
    return this.http.get<PendenciaLiquidacaoDto[]>(`${this.api}/a-liquidar`);
  }

  liquidacoes(aPagar?: boolean): Observable<LiquidacaoDto[]> {
    return this.http.get<LiquidacaoDto[]>(`${this.api}/liquidacoes`, { params: aPagar ? { aPagar: true } : {} });
  }

  liquidar(dto: { origem: OrigemLiquidacao; origemId: string | null; empenhoId: string; notaFiscal: string | null; notaEmitidaEm: string | null;
    valorBruto: number; iss: number | null; irrf: number | null; inss: number | null; outrasRetencoes: number | null; vencimento: string | null;
    justificativa: string | null; atestadoPorMatricula: string | null }): Observable<LiquidacaoDto> {
    return this.http.post<LiquidacaoDto>(`${this.api}/liquidacao`, dto);
  }

  pagar(uuid: string, dto: { data: string; ordemBancaria: string; justificativaForaDeOrdem: string | null }): Observable<LiquidacaoDto> {
    return this.http.post<LiquidacaoDto>(`${this.api}/liquidacao/${uuid}/pagamento`, dto);
  }

  diarias(solicitacaoId?: string): Observable<DiariaTfdDto[]> {
    return this.http.get<DiariaTfdDto[]>(`${this.api}/diarias-tfd`, { params: solicitacaoId ? { solicitacaoId } : {} });
  }

  concederDiaria(dto: { solicitacaoId: string; beneficiario: BeneficiarioDiariaTfd; credorId: string | null; credorCpf: string | null; dias: number;
    valorDiaria: number; observacao: string | null }): Observable<DiariaTfdDto> {
    return this.http.post<DiariaTfdDto>(`${this.api}/diaria-tfd`, dto);
  }

  restosAPagar(): Observable<RestoAPagarDto[]> {
    return this.http.get<RestoAPagarDto[]>(`${this.api}/restos-a-pagar`);
  }

  execucao(exercicio?: number): Observable<ExecucaoDto> {
    return this.http.get<ExecucaoDto>(`${this.api}/execucao`, { params: exercicio ? { exercicio } : {} });
  }
}
