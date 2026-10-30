import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AvaliacaoImpactoRequestDto,
  BoletimEstruturaRequestDto,
  EstruturaTemporariaRequestDto,
  EventoMassaRequestDto,
  ExposicaoAmbientalRequestDto,
  PainelEpidemiaDto,
  PerfilTipoEmergencia,
  SituacaoEstruturaTemporaria,
  CapacidadeEmergencialDto,
  PrioridadeAcaoEmergencial,
  RelatorioPosEventoDto,
  RecursoCapacidade,
  SituacaoAcaoEmergencial,
  SituacaoMobilizacao,
  TipoMobilizacao,
  EmergenciaDto,
  EventoEmergencialDto,
  FonteEventoEmergencial,
  NivelEmergenciaDto,
  PlanoContingenciaDto,
  PlanoContingenciaRequestDto,
  SituacaoEmergencia,
  SituacaoEventoEmergencial,
  SugestaoImpactoDto,
  TipoDecretoEmergencia,
  TipoEmergenciaDto,
} from '../models/emergencia';

/** Emergências e Desastres, fatia E1 (ADR-0156). */
@Injectable({ providedIn: 'root' })
export class EmergenciaService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  tipos(): Observable<TipoEmergenciaDto[]> {
    return this.http.get<TipoEmergenciaDto[]>(`${this.api}/emergencias/tipos`);
  }

  niveis(): Observable<NivelEmergenciaDto[]> {
    return this.http.get<NivelEmergenciaDto[]>(`${this.api}/emergencias/niveis`);
  }

  eventos(situacao?: SituacaoEventoEmergencial): Observable<EventoEmergencialDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : undefined;
    return this.http.get<EventoEmergencialDto[]>(`${this.api}/eventos-emergenciais`, { params });
  }

  registrarEvento(dto: { tipoId: string; fonte: FonteEventoEmergencial; descricao: string; local?: string | null; ocorridoEm?: string | null; geometria?: string | null; surtoId?: string | null }): Observable<EventoEmergencialDto> {
    return this.http.post<EventoEmergencialDto>(`${this.api}/evento-emergencial`, dto);
  }

  situacaoDoEvento(uuid: string, situacao: 'MONITORADO' | 'DESCARTADO', motivo: string | null): Observable<EventoEmergencialDto> {
    return this.http.post<EventoEmergencialDto>(`${this.api}/evento-emergencial/${uuid}/situacao`, { situacao, motivo });
  }

  converter(eventoId: string, dto: { nome: string; nivelId: string; justificativa: string; responsavelMatricula?: string | null; planejada?: boolean }): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/evento-emergencial/${eventoId}/emergencia`, dto);
  }

  emergencias(situacao?: SituacaoEmergencia): Observable<EmergenciaDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : undefined;
    return this.http.get<EmergenciaDto[]>(`${this.api}/emergencias`, { params });
  }

  emergencia(uuid: string): Observable<EmergenciaDto> {
    return this.http.get<EmergenciaDto>(`${this.api}/emergencia/${uuid}`);
  }

  mudarNivel(uuid: string, nivelId: string, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/nivel`, { nivelId, justificativa });
  }

  territorio(uuid: string, geometria: string | null): Observable<EmergenciaDto> {
    return this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/territorio`, { geometria, microareaIds: null });
  }

  referencias(uuid: string, dto: { espinNumero: string | null; espinData: string | null; decretoTipo: TipoDecretoEmergencia | null; decretoNumero: string | null; decretoData: string | null }): Observable<EmergenciaDto> {
    return this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/referencias-legais`, dto);
  }

  encerrar(uuid: string, motivo: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/encerramento`, { motivo });
  }

  sugestao(uuid: string): Observable<SugestaoImpactoDto> {
    return this.http.get<SugestaoImpactoDto>(`${this.api}/emergencia/${uuid}/sugestao-impacto`);
  }

  avaliar(uuid: string, dto: AvaliacaoImpactoRequestDto): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/avaliacao-impacto`, dto);
  }

  // ── Catálogos e planos (E2, ADR-0157) ──

  salvarTipo(uuid: string | null, dto: { codigo: string; nome: string; categoria: 'DESASTRE' | 'SAUDE'; cobrade: string | null; perfil: PerfilTipoEmergencia; ativo: boolean | null }): Observable<TipoEmergenciaDto> {
    return uuid ? this.http.put<TipoEmergenciaDto>(`${this.api}/emergencias/tipo/${uuid}`, dto) : this.http.post<TipoEmergenciaDto>(`${this.api}/emergencias/tipo`, dto);
  }

  salvarNivel(uuid: string | null, dto: { ordem: number; nome: string; descricao: string | null; ativo: boolean | null }): Observable<NivelEmergenciaDto> {
    return uuid ? this.http.put<NivelEmergenciaDto>(`${this.api}/emergencias/nivel/${uuid}`, dto) : this.http.post<NivelEmergenciaDto>(`${this.api}/emergencias/nivel`, dto);
  }

  planos(filtro: { vigentes?: boolean; tipoId?: string } = {}): Observable<PlanoContingenciaDto[]> {
    let params = new HttpParams();
    if (filtro.vigentes) params = params.set('vigentes', 'true');
    if (filtro.tipoId) params = params.set('tipoId', filtro.tipoId);
    return this.http.get<PlanoContingenciaDto[]>(`${this.api}/emergencias/planos`, { params });
  }

  plano(uuid: string): Observable<PlanoContingenciaDto> {
    return this.http.get<PlanoContingenciaDto>(`${this.api}/emergencias/plano/${uuid}`);
  }

  salvarPlano(uuid: string | null, dto: PlanoContingenciaRequestDto): Observable<PlanoContingenciaDto> {
    return uuid ? this.http.put<PlanoContingenciaDto>(`${this.api}/emergencias/plano/${uuid}`, dto) : this.http.post<PlanoContingenciaDto>(`${this.api}/emergencias/plano`, dto);
  }

  revisarPlano(uuid: string): Observable<PlanoContingenciaDto> {
    return this.http.post<PlanoContingenciaDto>(`${this.api}/emergencias/plano/${uuid}/revisao`, {});
  }

  publicarPlano(uuid: string): Observable<PlanoContingenciaDto> {
    return this.http.post<PlanoContingenciaDto>(`${this.api}/emergencias/plano/${uuid}/publicacao`, {});
  }

  revogarPlano(uuid: string, motivo: string): Observable<PlanoContingenciaDto> {
    return this.http.post<PlanoContingenciaDto>(`${this.api}/emergencias/plano/${uuid}/revogacao`, { motivo });
  }

  ativarPlano(emergenciaId: string, planoId: string, autoridade: string, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${emergenciaId}/plano-ativo`, { planoId, autoridade, justificativa });
  }

  desativarPlano(emergenciaId: string, ativacaoId: string, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${emergenciaId}/plano-ativo/${ativacaoId}/desativacao`, { justificativa });
  }

  // ── Coordenação (E3, ADR-0158) ──

  novaAcao(uuid: string, dto: { descricao: string; responsavel: string; unidadeId: string | null; prioridade: PrioridadeAcaoEmergencial; prazo: string | null }): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/acao`, dto);
  }

  gerarAcoesDoPlano(uuid: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/acoes-do-plano`, {});
  }

  situacaoDaAcao(uuid: string, acaoId: string, situacao: SituacaoAcaoEmergencial, observacao: string | null, motivo: string | null): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/acao/${acaoId}/situacao`, { situacao, observacao, motivo });
  }

  mobilizar(uuid: string, dto: { tipo: TipoMobilizacao; origemId: string | null; destinoId: string; profissionalMatricula: string | null; recurso: string | null; quantidade: number | null; responsavel: string | null; inicio: string | null; fim: string | null }): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/mobilizacao`, dto);
  }

  situacaoDaMobilizacao(uuid: string, mobilizacaoId: string, situacao: SituacaoMobilizacao, motivo: string | null): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/mobilizacao/${mobilizacaoId}/situacao`, { situacao, motivo });
  }

  ligarTransferencia(uuid: string, mobilizacaoId: string, transferenciaId: string): Observable<EmergenciaDto> {
    return this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/mobilizacao/${mobilizacaoId}/transferencia`, { transferenciaId });
  }

  capacidades(unidadeId?: string): Observable<CapacidadeEmergencialDto[]> {
    const params = unidadeId ? new HttpParams().set('unidadeId', unidadeId) : undefined;
    return this.http.get<CapacidadeEmergencialDto[]>(`${this.api}/emergencias/capacidades`, { params });
  }

  salvarCapacidade(uuid: string | null, dto: { unidadeId: string; recurso: RecursoCapacidade; normal: number; emergencial: number; observacao: string | null; ativa: boolean | null }): Observable<CapacidadeEmergencialDto> {
    return uuid ? this.http.put<CapacidadeEmergencialDto>(`${this.api}/emergencias/capacidade/${uuid}`, dto)
      : this.http.post<CapacidadeEmergencialDto>(`${this.api}/emergencias/capacidade`, dto);
  }

  ativarCapacidade(uuid: string, capacidadeId: string, quantidade: number, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/capacidade-ativa`, { capacidadeId, quantidade, justificativa });
  }

  desativarCapacidade(uuid: string, ativacaoId: string, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/capacidade-ativa/${ativacaoId}/desativacao`, { justificativa });
  }

  // ── Desmobilização e relatório pós-evento (E5, ADR-0159) ──

  desmobilizar(uuid: string, responsavel: string, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/desmobilizacao`, { responsavel, justificativa });
  }

  cancelarAcoesRestantes(uuid: string, motivo: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/acoes-restantes/cancelamento`, { motivo });
  }

  relatorio(uuid: string): Observable<RelatorioPosEventoDto> {
    return this.http.get<RelatorioPosEventoDto>(`${this.api}/emergencia/${uuid}/relatorio`);
  }

  salvarRelatorio(uuid: string, dto: { resumo: string | null; problemas: string | null; licoes: string | null; recomendacoes: { descricao: string; planoCodigo: string | null }[] }): Observable<RelatorioPosEventoDto> {
    return this.http.put<RelatorioPosEventoDto>(`${this.api}/emergencia/${uuid}/relatorio`, dto);
  }

  finalizarRelatorio(uuid: string): Observable<RelatorioPosEventoDto> {
    return this.http.post<RelatorioPosEventoDto>(`${this.api}/emergencia/${uuid}/relatorio/finalizacao`, {});
  }

  revisarPlanoDaRecomendacao(uuid: string, recomendacaoId: string): Observable<RelatorioPosEventoDto> {
    return this.http.post<RelatorioPosEventoDto>(`${this.api}/emergencia/${uuid}/relatorio/recomendacao/${recomendacaoId}/revisao-plano`, {});
  }

  relatorioCsv(uuid: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.api}/emergencia/${uuid}/relatorio.csv`, { responseType: 'blob', observe: 'response' });
  }

  // ── Eventos especializados (E6a, ADR-0160) ──

  salvarEventoMassa(uuid: string, dto: EventoMassaRequestDto): Observable<EmergenciaDto> {
    return this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/evento-massa`, dto);
  }

  iniciarOperacao(uuid: string, justificativa: string): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/operacao`, { justificativa });
  }

  salvarEstrutura(uuid: string, estruturaId: string | null, dto: EstruturaTemporariaRequestDto): Observable<EmergenciaDto> {
    return estruturaId
      ? this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/estrutura/${estruturaId}`, dto)
      : this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/estrutura`, dto);
  }

  situacaoDaEstrutura(uuid: string, estruturaId: string, situacao: Exclude<SituacaoEstruturaTemporaria, 'PLANEJADA'>, motivo: string | null): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/estrutura/${estruturaId}/situacao`, { situacao, motivo });
  }

  boletim(uuid: string, estruturaId: string, dto: BoletimEstruturaRequestDto): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/estrutura/${estruturaId}/boletim`, dto);
  }

  // ── Epidemia e desastre ambiental (E6b, ADR-0161) ──

  definirAgravo(uuid: string, agravoId: string): Observable<EmergenciaDto> {
    return this.http.put<EmergenciaDto>(`${this.api}/emergencia/${uuid}/agravo`, { agravoId });
  }

  painelEpidemia(uuid: string): Observable<PainelEpidemiaDto> {
    return this.http.get<PainelEpidemiaDto>(`${this.api}/emergencia/${uuid}/painel-epidemia`);
  }

  registrarExposicao(uuid: string, dto: ExposicaoAmbientalRequestDto): Observable<EmergenciaDto> {
    return this.http.post<EmergenciaDto>(`${this.api}/emergencia/${uuid}/exposicao-ambiental`, dto);
  }
}
