import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  GrauDano, IncidenteDto, MinhaAcaoDto, NaoConformidadeDto, NaturezaIncidente, PainelNcDto, PainelSegurancaDto, ProtocoloIncidenteDto, SituacaoIncidente,
  SituacaoNaoConformidade, SugestaoNcDto, TipoIncidente, AcaoQualidadeDto,
} from '../models/qualidade';

/** Qualidade, fatia Q1: segurança do paciente (ADR-0203). */
@Injectable({ providedIn: 'root' })
export class QualidadeService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/qualidade';

  notificar(dto: { unidadeId: string; pacienteId: string | null; ocorridoEm: string; local: string; tipo: TipoIncidente; danoPercebido: GrauDano | null;
    descricao: string; acaoImediata: string | null; anonima: boolean }): Observable<ProtocoloIncidenteDto> {
    return this.http.post<ProtocoloIncidenteDto>(`${this.api}/incidente`, dto);
  }

  incidentes(situacao?: SituacaoIncidente): Observable<IncidenteDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : new HttpParams();
    return this.http.get<IncidenteDto[]>(`${this.api}/incidentes`, { params });
  }

  incidente(uuid: string): Observable<IncidenteDto> {
    return this.http.get<IncidenteDto>(`${this.api}/incidente/${uuid}`);
  }

  classificar(uuid: string, dto: { natureza: NaturezaIncidente; dano: GrauDano; tipo: TipoIncidente | null; neverEvent: boolean }): Observable<IncidenteDto> {
    return this.http.post<IncidenteDto>(`${this.api}/incidente/${uuid}/classificacao`, dto);
  }

  concluir(uuid: string, dto: { fatores: string[]; causaRaiz: string | null; licoes: string | null }): Observable<IncidenteDto> {
    return this.http.post<IncidenteDto>(`${this.api}/incidente/${uuid}/conclusao`, dto);
  }

  descartar(uuid: string, motivo: string): Observable<IncidenteDto> {
    return this.http.post<IncidenteDto>(`${this.api}/incidente/${uuid}/descarte`, { motivo });
  }

  notivisa(uuid: string, numero: string): Observable<IncidenteDto> {
    return this.http.post<IncidenteDto>(`${this.api}/incidente/${uuid}/notivisa`, { numero });
  }

  painel(): Observable<PainelSegurancaDto> {
    return this.http.get<PainelSegurancaDto>(`${this.api}/seguranca/painel`);
  }

  // ── Não conformidades (ADR-0204) ──

  naoConformidades(situacao?: SituacaoNaoConformidade): Observable<NaoConformidadeDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : new HttpParams();
    return this.http.get<NaoConformidadeDto[]>(`${this.api}/nao-conformidades`, { params });
  }

  naoConformidade(uuid: string): Observable<NaoConformidadeDto> {
    return this.http.get<NaoConformidadeDto>(`${this.api}/nao-conformidade/${uuid}`);
  }

  registrarNc(dto: object): Observable<NaoConformidadeDto> {
    return this.http.post<NaoConformidadeDto>(`${this.api}/nao-conformidade`, dto);
  }

  acaoNc(uuid: string, acao: 'analise' | 'acao' | 'verificacao' | 'cancelamento', corpo: object): Observable<NaoConformidadeDto> {
    return this.http.post<NaoConformidadeDto>(`${this.api}/nao-conformidade/${uuid}/${acao}`, corpo);
  }

  andamento(acaoId: string, corpo: { situacao: string; evidencia: string | null }): Observable<AcaoQualidadeDto> {
    return this.http.post<AcaoQualidadeDto>(`${this.api}/acao/${acaoId}/andamento`, corpo);
  }

  cancelarAcao(acaoId: string, motivo: string): Observable<NaoConformidadeDto> {
    return this.http.post<NaoConformidadeDto>(`${this.api}/acao/${acaoId}/cancelamento`, { motivo });
  }

  minhasAcoes(): Observable<MinhaAcaoDto[]> {
    return this.http.get<MinhaAcaoDto[]>(`${this.api}/minhas-acoes`);
  }

  sugestoesNc(): Observable<SugestaoNcDto[]> {
    return this.http.get<SugestaoNcDto[]>(`${this.api}/nao-conformidades/sugestoes`);
  }

  painelNc(): Observable<PainelNcDto> {
    return this.http.get<PainelNcDto>(`${this.api}/nao-conformidades/painel`);
  }
}
