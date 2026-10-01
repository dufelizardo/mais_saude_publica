import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SuccessResponseDto } from '../models/profissional';
import {
  AutorizacaoRegulacaoRequestDto,
  ComplementoRegulacaoRequestDto,
  MotivoRegulacaoRequestDto,
  ProcedimentoReguladoRequestDto,
  ProcedimentoReguladoResponseDto,
  ReclassificacaoRegulacaoRequestDto,
  SolicitacaoRegulacaoRequestDto,
  SolicitacaoRegulacaoResponseDto,
  SolicitacaoRegulacaoResumoDto,
} from '../models/regulacao';

/** Regulação do acesso (ADR-0087): catálogo de procedimentos regulados e solicitações. */
@Injectable({ providedIn: 'root' })
export class RegulacaoService {
  private readonly http = inject(HttpClient);
  private readonly procedimentoUrl = '/api/v1/procedimento-regulado';
  private readonly solicitacaoUrl = '/api/v1/solicitacao-regulacao';

  // ── Catálogo ──

  listarProcedimentos(): Observable<ProcedimentoReguladoResponseDto[]> {
    return this.http.get<ProcedimentoReguladoResponseDto[]>(`${this.procedimentoUrl}/`);
  }

  criarProcedimento(dto: ProcedimentoReguladoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.procedimentoUrl}/`, dto);
  }

  atualizarProcedimento(uuid: string, dto: ProcedimentoReguladoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.procedimentoUrl}/${uuid}`, dto);
  }

  // ── Solicitações ──

  /** Sem dado clínico, da mais recente para a mais antiga, das unidades no escopo. */
  listar(): Observable<SolicitacaoRegulacaoResumoDto[]> {
    return this.http.get<SolicitacaoRegulacaoResumoDto[]>(`${this.solicitacaoUrl}/`);
  }

  /** A fila do regulador; sem REGULACAO.REGULAR, a API responde 403. */
  fila(): Observable<SolicitacaoRegulacaoResumoDto[]> {
    return this.http.get<SolicitacaoRegulacaoResumoDto[]>(`${this.solicitacaoUrl}/fila`);
  }

  /** Detalhe clínico com eventos; leitura auditada. */
  buscar(uuid: string): Observable<SolicitacaoRegulacaoResponseDto> {
    return this.http.get<SolicitacaoRegulacaoResponseDto>(`${this.solicitacaoUrl}/${uuid}`);
  }

  /** 409 quando o paciente já tem uma solicitação do procedimento em andamento. */
  solicitar(dto: SolicitacaoRegulacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.solicitacaoUrl}/`, dto);
  }

  complementar(uuid: string, dto: ComplementoRegulacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.solicitacaoUrl}/${uuid}/complemento`, dto);
  }

  reclassificar(uuid: string, dto: ReclassificacaoRegulacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.solicitacaoUrl}/${uuid}/reclassificacao`, dto);
  }

  /** 422 quando quem autoriza é quem solicitou, ou a data da vaga está no passado. */
  autorizar(uuid: string, dto: AutorizacaoRegulacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.solicitacaoUrl}/${uuid}/autorizacao`, dto);
  }

  devolver(uuid: string, dto: MotivoRegulacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.solicitacaoUrl}/${uuid}/devolucao`, dto);
  }

  negar(uuid: string, dto: MotivoRegulacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.solicitacaoUrl}/${uuid}/negativa`, dto);
  }

  cancelar(uuid: string, dto: MotivoRegulacaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.solicitacaoUrl}/${uuid}/cancelamento`, dto);
  }
}
