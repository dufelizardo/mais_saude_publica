import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PrioridadeRegulacao } from '../models/regulacao';
import {
  CentralUrgenciaDto,
  DesfechoOcorrenciaUrgencia,
  DestinoUrgenciaDto,
  OcorrenciaUrgenciaDto,
  OcorrenciaUrgenciaRequestDto,
  PainelUrgenciaDto,
  RegulacaoUrgenciaRequestDto,
  SituacaoOcorrenciaUrgencia,
  TipoEventoUrgencia,
  AtendimentoAphDto,
  FichaAphDto,
  FichaAphRequestDto,
  RegistroAphRequestDto,
} from '../models/urgencia';

/** Regulação de urgência (ADR-0176): central do 192, regulação médica, deslocamentos e recebimento no destino. */
@Injectable({ providedIn: 'root' })
export class UrgenciaService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/urgencia';

  painel(centralId?: string | null): Observable<PainelUrgenciaDto> {
    return this.http.get<PainelUrgenciaDto>(`${this.api}/painel`, { params: centralId ? { centralId } : {} });
  }

  listar(filtro: { centralId?: string | null; situacao?: SituacaoOcorrenciaUrgencia | null; destinoId?: string | null; dias?: number }): Observable<OcorrenciaUrgenciaDto[]> {
    const params: Record<string, string> = {};
    if (filtro.centralId) params['centralId'] = filtro.centralId;
    if (filtro.situacao) params['situacao'] = filtro.situacao;
    if (filtro.destinoId) params['destinoId'] = filtro.destinoId;
    if (filtro.dias) params['dias'] = String(filtro.dias);
    return this.http.get<OcorrenciaUrgenciaDto[]>(`${this.api}/ocorrencia`, { params });
  }

  detalhe(uuid: string): Observable<OcorrenciaUrgenciaDto> {
    return this.http.get<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}`);
  }

  centrais(): Observable<CentralUrgenciaDto[]> {
    return this.http.get<CentralUrgenciaDto[]>(`${this.api}/centrais`);
  }

  destinos(): Observable<DestinoUrgenciaDto[]> {
    return this.http.get<DestinoUrgenciaDto[]>(`${this.api}/destinos`);
  }

  abrir(dto: OcorrenciaUrgenciaRequestDto): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia`, dto);
  }

  regular(uuid: string, dto: RegulacaoUrgenciaRequestDto): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/regulacao`, dto);
  }

  destino(uuid: string, dto: { profissionalMatricula: string; destinoId: string; vagaZero: boolean; justificativaVagaZero?: string | null }): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/destino`, dto);
  }

  reclassificar(uuid: string, dto: { profissionalMatricula: string; prioridade: PrioridadeRegulacao; motivo: string }): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/reclassificacao`, dto);
  }

  deslocamento(uuid: string, tipo: TipoEventoUrgencia, recursoIdentificacao?: string | null): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/deslocamento`, { tipo, recursoIdentificacao: recursoIdentificacao || null });
  }

  receber(uuid: string, observacao?: string | null): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/recebimento`, { observacao: observacao || null });
  }

  encerrar(uuid: string, dto: { profissionalMatricula: string; desfecho: DesfechoOcorrenciaUrgencia; observacao: string }): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/encerramento`, dto);
  }

  cancelar(uuid: string, motivo: string): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/cancelamento`, { motivo });
  }

  vincularPaciente(uuid: string, pacienteId: string): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`${this.api}/ocorrencia/${uuid}/paciente`, { pacienteId });
  }

  acionarSamu(criseId: string, dto: { centralId: string; motivo: string; endereco?: string | null; telefone?: string | null }): Observable<OcorrenciaUrgenciaDto> {
    return this.http.post<OcorrenciaUrgenciaDto>(`/api/v1/saude-mental/crise/${criseId}/acionamento-samu`, dto);
  }

  // ── Ficha do APH (ADR-0188) ──

  atendimentosAph(centralId?: string | null): Observable<AtendimentoAphDto[]> {
    return this.http.get<AtendimentoAphDto[]>('/api/v1/aph/atendimentos', { params: centralId ? { centralId } : {} });
  }

  fichaAph(ocorrenciaId: string): Observable<FichaAphDto> {
    return this.http.get<FichaAphDto>(`/api/v1/aph/ocorrencia/${ocorrenciaId}/ficha`);
  }

  salvarFichaAph(ocorrenciaId: string, dto: FichaAphRequestDto): Observable<FichaAphDto> {
    return this.http.put<FichaAphDto>(`/api/v1/aph/ocorrencia/${ocorrenciaId}/ficha`, dto);
  }

  registroAph(fichaId: string, rota: 'sinais-vitais' | 'medicamentos' | 'finalizacao' | 'adendo', dto: RegistroAphRequestDto): Observable<FichaAphDto> {
    return this.http.post<FichaAphDto>(`/api/v1/aph/ficha/${fichaId}/${rota}`, dto);
  }

  baixarFichaAph(fichaId: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`/api/v1/aph/ficha/${fichaId}/ficha.pdf`, { observe: 'response', responseType: 'blob' });
  }
}
