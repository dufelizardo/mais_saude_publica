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
  CrisesSaudeMentalDto,
  EventoCriseDto,
  PapelRedeApoio,
  PendenciaSaudeMental,
  ProjetoTerapeuticoRequestDto,
  SituacaoIntervencaoSaudeMental,
  TipoIntervencaoSaudeMentalDto,
  TransferenciaCuidadoDto,
  TransferenciasCuidadoDto,
} from '../models/saude-mental';

/** Saúde Mental, fatia SM1 (ADR-0120): acolhimento, caso, referência, avaliação e saída. */
@Injectable({ providedIn: 'root' })
export class SaudeMentalService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/saude-mental';

  casos(filtro: { unidadeId?: string; pacienteId?: string; escopo?: 'TECNICO' | 'EQUIPE' | ''; situacao?: SituacaoAcompanhamento; pendencia?: PendenciaSaudeMental } = {}): Observable<CasosSaudeMentalDto> {
    let params = new HttpParams();
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.pacienteId) params = params.set('pacienteId', filtro.pacienteId);
    if (filtro.escopo) params = params.set('escopo', filtro.escopo);
    if (filtro.situacao) params = params.set('situacao', filtro.situacao);
    if (filtro.pendencia) params = params.set('pendencia', filtro.pendencia);
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

  // ── SM2 (ADR-0121) ─────────────────────────────────────────────────────────────────────────

  tiposIntervencao(): Observable<TipoIntervencaoSaudeMentalDto[]> {
    return this.http.get<TipoIntervencaoSaudeMentalDto[]>(`${this.api}/tipos-intervencao`);
  }

  salvarPts(uuid: string, dto: ProjetoTerapeuticoRequestDto): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/pts`, dto);
  }

  registrarIntervencao(uuid: string, dto: { tipoId: string; situacao: SituacaoIntervencaoSaudeMental; resumo: string; realizadaEm?: string | null;
    compartilhadoComMatricula?: string | null; previstaId?: string | null; equipeMatriciadaId?: string | null }): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/intervencao`, dto);
  }

  incluirNaRede(uuid: string, dto: { nome: string; papel: PapelRedeApoio; vinculo?: string | null; telefone?: string | null; observacao?: string | null }): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/rede-apoio`, dto);
  }

  encerrarNaRede(uuid: string, pessoaId: string, motivo: string): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/rede-apoio/${pessoaId}/encerramento`, { motivo });
  }

  // ── SM4 (ADR-0122) ─────────────────────────────────────────────────────────────────────────

  encaminhar(uuid: string, dto: { solicitacaoId?: string | null; procedimentoId?: string | null; cid?: string | null; justificativa?: string | null;
    prioridade?: string | null; profissionalMatricula?: string | null }): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/encaminhamento`, dto);
  }

  compartilhar(uuid: string, equipeId: string, objetivo: string): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/compartilhamento`, { equipeId, objetivo });
  }

  encerrarCompartilhamento(uuid: string, compartilhamentoId: string, motivo: string): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/compartilhamento/${compartilhamentoId}/encerramento`, { motivo });
  }

  proporTransferencia(uuid: string, unidadeDestinoId: string, resumo: string): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/transferencia`, { unidadeDestinoId, resumo });
  }

  aceitarTransferencia(uuid: string, equipeId: string, tecnicoMatricula: string): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/transferencia/${uuid}/aceite`, { equipeId, tecnicoMatricula });
  }

  recusarTransferencia(uuid: string, motivo: string): Observable<TransferenciaCuidadoDto> {
    return this.http.post<TransferenciaCuidadoDto>(`${this.api}/transferencia/${uuid}/recusa`, { motivo });
  }

  cancelarTransferencia(uuid: string, motivo: string): Observable<TransferenciaCuidadoDto> {
    return this.http.post<TransferenciaCuidadoDto>(`${this.api}/transferencia/${uuid}/cancelamento`, { motivo });
  }

  transferencias(unidadeId?: string): Observable<TransferenciasCuidadoDto> {
    let params = new HttpParams();
    if (unidadeId) params = params.set('unidadeId', unidadeId);
    return this.http.get<TransferenciasCuidadoDto>(`${this.api}/transferencias`, { params });
  }

  // ── SM3 (ADR-0123) ─────────────────────────────────────────────────────────────────────────

  avaliarRisco(uuid: string, dto: { tipo: string; nivel: string; fatores?: string | null; medidas?: string | null; planoSeguranca?: boolean;
    frequenciaContato?: string | null; reavaliarEm?: string | null }): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/acompanhamento/${uuid}/risco`, dto);
  }

  registrarCrise(dto: { pacienteId: string; unidadeId: string; tipo: string; descricao: string; localOcorrencia?: string | null }): Observable<EventoCriseDto> {
    return this.http.post<EventoCriseDto>(`${this.api}/crise`, dto);
  }

  crises(unidadeId?: string, situacao?: string): Observable<CrisesSaudeMentalDto> {
    let params = new HttpParams();
    if (unidadeId) params = params.set('unidadeId', unidadeId);
    if (situacao) params = params.set('situacao', situacao);
    return this.http.get<CrisesSaudeMentalDto>(`${this.api}/crises`, { params });
  }

  crise(uuid: string): Observable<EventoCriseDto> {
    return this.http.get<EventoCriseDto>(`${this.api}/crise/${uuid}`);
  }

  etapaCrise(uuid: string, etapa: string, descricao: string): Observable<EventoCriseDto> {
    return this.http.post<EventoCriseDto>(`${this.api}/crise/${uuid}/etapa`, { etapa, descricao });
  }

  encerrarCrise(uuid: string, desfecho: string): Observable<EventoCriseDto> {
    return this.http.post<EventoCriseDto>(`${this.api}/crise/${uuid}/encerramento`, { desfecho });
  }

  notificarCrise(uuid: string, numero?: string | null): Observable<EventoCriseDto> {
    return this.http.post<EventoCriseDto>(`${this.api}/crise/${uuid}/notificacao`, { numero: numero || null });
  }
}
