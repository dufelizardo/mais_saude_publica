import { HttpClient, HttpParams } from '@angular/common/http';
import { ElegiveisDto } from '../models/adscricao';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AcaoProgramaticaDto,
  AcaoProgramaticaRequestDto,
  AcoesProgramaticasDto,
  AcompanhamentoDto,
  AcompanhamentosDoPacienteDto,
  CategoriaProgramaDto,
  InscritosProgramaDto,
  MotivoSaidaAcompanhamento,
  ProgramaDto,
  ProgramaPublicoDto,
  ProgramaRequestDto,
  ProgramaResumoDto,
  RealizacaoAcaoRequestDto,
  ResumoAcompanhamentosDto,
  SituacaoAcompanhamento,
} from '../models/programa';

/** Programas de saúde (ADR-0118): catálogo, categorias, participação das unidades e a consulta pública. */
@Injectable({ providedIn: 'root' })
export class ProgramaService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  categorias(): Observable<CategoriaProgramaDto[]> {
    return this.http.get<CategoriaProgramaDto[]>(`${this.api}/categoria-programa/`);
  }

  criarCategoria(dto: { nome: string; ordem?: number | null; ativa: boolean }): Observable<CategoriaProgramaDto> {
    return this.http.post<CategoriaProgramaDto>(`${this.api}/categoria-programa/`, dto);
  }

  atualizarCategoria(uuid: string, dto: { nome: string; ordem?: number | null; ativa: boolean }): Observable<CategoriaProgramaDto> {
    return this.http.patch<CategoriaProgramaDto>(`${this.api}/categoria-programa/${uuid}`, dto);
  }

  listar(filtro: { categoriaId?: string; unidadeId?: string; equipeId?: string } = {}): Observable<ProgramaResumoDto[]> {
    let params = new HttpParams();
    if (filtro.categoriaId) params = params.set('categoriaId', filtro.categoriaId);
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.equipeId) params = params.set('equipeId', filtro.equipeId);
    return this.http.get<ProgramaResumoDto[]>(`${this.api}/programa/`, { params });
  }

  buscar(uuid: string): Observable<ProgramaDto> {
    return this.http.get<ProgramaDto>(`${this.api}/programa/${uuid}`);
  }

  criar(dto: ProgramaRequestDto): Observable<ProgramaDto> {
    return this.http.post<ProgramaDto>(`${this.api}/programa/`, dto);
  }

  atualizar(uuid: string, dto: ProgramaRequestDto): Observable<ProgramaDto> {
    return this.http.patch<ProgramaDto>(`${this.api}/programa/${uuid}`, dto);
  }

  incluirParticipacao(programaId: string, unidadeId: string, equipeId?: string | null, inicio?: string | null): Observable<ProgramaDto> {
    return this.http.post<ProgramaDto>(`${this.api}/programa/${programaId}/participacao`,
      { unidadeId, equipeId: equipeId || null, inicio: inicio || null });
  }

  encerrarParticipacao(programaId: string, participacaoId: string, motivo: string, fim?: string | null): Observable<ProgramaDto> {
    return this.http.post<ProgramaDto>(`${this.api}/programa/${programaId}/participacao/${participacaoId}/encerramento`, { motivo, fim: fim || null });
  }

  // ── Acompanhamento programático (ADR-0119) ───────────────────────────────────────────────────

  inscrever(programaId: string, dto: { pacienteId: string; unidadeId: string; equipeId?: string | null; profissionalMatricula?: string | null; inicio?: string | null }): Observable<AcompanhamentoDto> {
    return this.http.post<AcompanhamentoDto>(`${this.api}/programa/${programaId}/acompanhamento`, dto);
  }

  inscritos(programaId: string, filtro: { unidadeId?: string; situacao?: SituacaoAcompanhamento } = {}): Observable<InscritosProgramaDto> {
    let params = new HttpParams();
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.situacao) params = params.set('situacao', filtro.situacao);
    return this.http.get<InscritosProgramaDto>(`${this.api}/programa/${programaId}/acompanhamentos`, { params });
  }

  mudarSituacao(acompanhamentoId: string, situacao: 'ATIVO' | 'FALTOSO', motivo: string): Observable<AcompanhamentoDto> {
    return this.http.post<AcompanhamentoDto>(`${this.api}/acompanhamento/${acompanhamentoId}/situacao`, { situacao, motivo });
  }

  encerrarAcompanhamento(acompanhamentoId: string, motivoSaida: MotivoSaidaAcompanhamento, observacao: string, fim?: string | null): Observable<AcompanhamentoDto> {
    return this.http.post<AcompanhamentoDto>(`${this.api}/acompanhamento/${acompanhamentoId}/encerramento`, { motivoSaida, observacao, fim: fim || null });
  }

  /** Elegíveis do programa entre os adscritos da equipe (ADR-0128). */
  elegiveis(programaId: string, equipeId: string, microareaId?: string | null): Observable<ElegiveisDto> {
    let params = new HttpParams().set('equipeId', equipeId);
    if (microareaId) params = params.set('microareaId', microareaId);
    return this.http.get<ElegiveisDto>(`${this.api}/programa/${programaId}/elegiveis`, { params });
  }

  doPaciente(pacienteId: string): Observable<AcompanhamentosDoPacienteDto> {
    return this.http.get<AcompanhamentosDoPacienteDto>(`${this.api}/paciente/${pacienteId}/programas`);
  }

  resumoAcompanhamentos(): Observable<ResumoAcompanhamentosDto> {
    return this.http.get<ResumoAcompanhamentosDto>(`${this.api}/acompanhamento/resumo`);
  }

  // ── Ações programáticas (ADR-0132) ─────────────────────────────────────────────────────────────

  acoes(filtro: { programaId?: string; unidadeId?: string; equipeId?: string; responsavelMatricula?: string; de?: string; ate?: string;
    situacao?: string } = {}): Observable<AcoesProgramaticasDto> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(filtro)) if (v) params = params.set(k, v);
    return this.http.get<AcoesProgramaticasDto>(`${this.api}/acoes-programaticas`, { params });
  }

  preverAcao(dto: AcaoProgramaticaRequestDto): Observable<AcaoProgramaticaDto> {
    return this.http.post<AcaoProgramaticaDto>(`${this.api}/acao-programatica`, dto);
  }

  buscaAtiva(acompanhamentoId: string, dto: { responsavelMatricula?: string | null; dataPrevista?: string | null; descricao?: string | null } = {}):
    Observable<AcaoProgramaticaDto> {
    return this.http.post<AcaoProgramaticaDto>(`${this.api}/acompanhamento/${acompanhamentoId}/busca-ativa`, dto);
  }

  realizarAcao(uuid: string, dto: RealizacaoAcaoRequestDto): Observable<AcaoProgramaticaDto> {
    return this.http.post<AcaoProgramaticaDto>(`${this.api}/acao-programatica/${uuid}/realizacao`, dto);
  }

  mudarSituacaoAcao(uuid: string, situacao: 'NAO_REALIZADA' | 'CANCELADA', motivo: string): Observable<AcaoProgramaticaDto> {
    return this.http.post<AcaoProgramaticaDto>(`${this.api}/acao-programatica/${uuid}/situacao`, { situacao, motivo });
  }

  acoesDoPaciente(pacienteId: string): Observable<AcaoProgramaticaDto[]> {
    return this.http.get<AcaoProgramaticaDto[]>(`${this.api}/paciente/${pacienteId}/acoes-programaticas`);
  }

  publico(): Observable<ProgramaPublicoDto[]> {
    return this.http.get<ProgramaPublicoDto[]>(`${this.api}/publico/programa`);
  }
}
