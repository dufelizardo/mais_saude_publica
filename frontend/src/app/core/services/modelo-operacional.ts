import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CapacidadeAssistencialDto,
  CapacidadeAssistencialRequestDto,
  LimiteLeitosDto,
  LocaisDeRealizacaoDto,
  ModeloOperacionalUnidadeDto,
  PerfilOperacionalDto,
  PerfilOperacionalRequestDto,
  PontoRequestDto,
  PontosDaUnidadeDto,
  RotaRequestDto,
  ServicoUnidadeRequestDto,
  ServicosDaUnidadeDto,
  TipoServicoDto,
  TipoServicoRequestDto,
} from '../models/modelo-operacional';

/** Modelo operacional (ADR-0117, ADR-0165): catálogos de perfil, capacidade e tipo de serviço, e o que cada unidade tem. */
@Injectable({ providedIn: 'root' })
export class ModeloOperacionalService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  perfis(): Observable<PerfilOperacionalDto[]> {
    return this.http.get<PerfilOperacionalDto[]>(`${this.api}/perfil-operacional/`);
  }

  criarPerfil(dto: PerfilOperacionalRequestDto): Observable<PerfilOperacionalDto> {
    return this.http.post<PerfilOperacionalDto>(`${this.api}/perfil-operacional/`, dto);
  }

  atualizarPerfil(uuid: string, dto: PerfilOperacionalRequestDto): Observable<PerfilOperacionalDto> {
    return this.http.patch<PerfilOperacionalDto>(`${this.api}/perfil-operacional/${uuid}`, dto);
  }

  capacidades(): Observable<CapacidadeAssistencialDto[]> {
    return this.http.get<CapacidadeAssistencialDto[]>(`${this.api}/capacidade-assistencial/`);
  }

  criarCapacidade(dto: CapacidadeAssistencialRequestDto): Observable<CapacidadeAssistencialDto> {
    return this.http.post<CapacidadeAssistencialDto>(`${this.api}/capacidade-assistencial/`, dto);
  }

  atualizarCapacidade(uuid: string, dto: CapacidadeAssistencialRequestDto): Observable<CapacidadeAssistencialDto> {
    return this.http.patch<CapacidadeAssistencialDto>(`${this.api}/capacidade-assistencial/${uuid}`, dto);
  }

  tiposServico(): Observable<TipoServicoDto[]> {
    return this.http.get<TipoServicoDto[]>(`${this.api}/tipo-servico/`);
  }

  criarTipoServico(dto: TipoServicoRequestDto): Observable<TipoServicoDto> {
    return this.http.post<TipoServicoDto>(`${this.api}/tipo-servico/`, dto);
  }

  atualizarTipoServico(uuid: string, dto: TipoServicoRequestDto): Observable<TipoServicoDto> {
    return this.http.patch<TipoServicoDto>(`${this.api}/tipo-servico/${uuid}`, dto);
  }

  servicosDaUnidade(unidadeId: string): Observable<ServicosDaUnidadeDto> {
    return this.http.get<ServicosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/servicos`);
  }

  incluirServico(unidadeId: string, dto: ServicoUnidadeRequestDto): Observable<ServicosDaUnidadeDto> {
    return this.http.post<ServicosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/servico`, dto);
  }

  atualizarServico(unidadeId: string, servicoId: string, dto: ServicoUnidadeRequestDto): Observable<ServicosDaUnidadeDto> {
    return this.http.patch<ServicosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/servico/${servicoId}`, dto);
  }

  encerrarServico(unidadeId: string, servicoId: string, motivo: string, fim?: string | null): Observable<ServicosDaUnidadeDto> {
    return this.http.post<ServicosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/servico/${servicoId}/encerramento`, { motivo, fim: fim || null });
  }

  pontosDaUnidade(unidadeId: string): Observable<PontosDaUnidadeDto> {
    return this.http.get<PontosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/pontos`);
  }

  /** Os pontos ativos da unidade e o domicílio do paciente, para o local de realização do atendimento (ADR-0166). */
  locaisDeRealizacao(unidadeId: string, pacienteId?: string | null): Observable<LocaisDeRealizacaoDto> {
    const params = pacienteId ? new HttpParams().set('pacienteId', pacienteId) : undefined;
    return this.http.get<LocaisDeRealizacaoDto>(`${this.api}/unidade-saude/id/${unidadeId}/locais-de-realizacao`, { params });
  }

  incluirPonto(unidadeId: string, dto: PontoRequestDto): Observable<PontosDaUnidadeDto> {
    return this.http.post<PontosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/ponto`, dto);
  }

  atualizarPonto(unidadeId: string, pontoId: string, dto: PontoRequestDto): Observable<PontosDaUnidadeDto> {
    return this.http.patch<PontosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/ponto/${pontoId}`, dto);
  }

  incluirRota(unidadeId: string, dto: RotaRequestDto): Observable<PontosDaUnidadeDto> {
    return this.http.post<PontosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/rota`, dto);
  }

  atualizarRota(unidadeId: string, rotaId: string, dto: RotaRequestDto): Observable<PontosDaUnidadeDto> {
    return this.http.patch<PontosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/rota/${rotaId}`, dto);
  }

  encerrarPontoOuRota(unidadeId: string, alvo: 'ponto' | 'rota', id: string, motivo: string, fim?: string | null): Observable<PontosDaUnidadeDto> {
    return this.http.post<PontosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/${alvo}/${id}/encerramento`, { motivo, fim: fim || null });
  }

  registrarPosicao(unidadeId: string, pontoId: string, desde?: string | null, observacao?: string | null): Observable<PontosDaUnidadeDto> {
    return this.http.post<PontosDaUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/posicao`, { pontoId, desde: desde || null, observacao: observacao || null });
  }

  limitesDeLeitos(unidadeId: string): Observable<LimiteLeitosDto[]> {
    return this.http.get<LimiteLeitosDto[]>(`${this.api}/unidade-saude/id/${unidadeId}/limites-de-leitos`);
  }

  alterarLimiteDeLeitos(unidadeId: string, capacidadeId: string, quantidade: number, motivo: string): Observable<LimiteLeitosDto[]> {
    return this.http.post<LimiteLeitosDto[]>(`${this.api}/unidade-saude/id/${unidadeId}/limites-de-leitos`, { capacidadeId, quantidade, motivo });
  }

  daUnidade(unidadeId: string): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.get<ModeloOperacionalUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional`);
  }

  incluirPerfil(unidadeId: string, perfilId: string, inicio?: string | null): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.post<ModeloOperacionalUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional/perfil`,
      { perfilId, inicio: inicio || null });
  }

  incluirCapacidade(unidadeId: string, capacidadeId: string, inicio?: string | null, observacao?: string | null): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.post<ModeloOperacionalUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional/capacidade`,
      { capacidadeId, inicio: inicio || null, observacao: observacao || null });
  }

  encerrar(unidadeId: string, tipo: 'perfil' | 'capacidade', associacaoId: string, motivo: string, fim?: string | null): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.post<ModeloOperacionalUnidadeDto>(
      `${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional/${tipo}/${associacaoId}/encerramento`, { motivo, fim: fim || null });
  }
}
