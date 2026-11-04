import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AcaoControleVetorialDto,
  AcaoControleVetorialRequestDto,
  BloqueioPendenteDto,
  FocoVetorialDto,
  ImovelVetorialDto,
  IndicesVetoriaisDto,
  ResultadoLaboratorioFoco,
  SituacaoAcaoControleVetorial,
  SituacaoFoco,
  VistoriaVetorialDto,
  VistoriaVetorialRequestDto,
} from '../models/controle-vetorial';

/** Controle de vetores, fatia V4a da Vigilância (ADR-0162). */
@Injectable({ providedIn: 'root' })
export class ControleVetorialService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/vetores';

  imoveis(areaId: string, busca?: string): Observable<ImovelVetorialDto[]> {
    let params = new HttpParams().set('areaId', areaId);
    if (busca) params = params.set('busca', busca);
    return this.http.get<ImovelVetorialDto[]>(`${this.api}/imoveis`, { params });
  }

  pendencias(areaId?: string | null): Observable<ImovelVetorialDto[]> {
    const params = areaId ? new HttpParams().set('areaId', areaId) : undefined;
    return this.http.get<ImovelVetorialDto[]>(`${this.api}/pendencias`, { params });
  }

  registrarVistoria(dto: VistoriaVetorialRequestDto): Observable<VistoriaVetorialDto> {
    return this.http.post<VistoriaVetorialDto>(`${this.api}/vistoria`, dto);
  }

  vistorias(filtro: { ano?: number; ciclo?: number; areaId?: string | null } = {}): Observable<VistoriaVetorialDto[]> {
    let params = new HttpParams();
    if (filtro.ano) params = params.set('ano', filtro.ano);
    if (filtro.ciclo) params = params.set('ciclo', filtro.ciclo);
    if (filtro.areaId) params = params.set('areaId', filtro.areaId);
    return this.http.get<VistoriaVetorialDto[]>(`${this.api}/vistorias`, { params });
  }

  focos(situacao?: SituacaoFoco | null, areaId?: string | null): Observable<FocoVetorialDto[]> {
    let params = new HttpParams();
    if (situacao) params = params.set('situacao', situacao);
    if (areaId) params = params.set('areaId', areaId);
    return this.http.get<FocoVetorialDto[]>(`${this.api}/focos`, { params });
  }

  laboratorio(focoId: string, resultado: ResultadoLaboratorioFoco): Observable<FocoVetorialDto> {
    return this.http.post<FocoVetorialDto>(`${this.api}/foco/${focoId}/laboratorio`, { resultado });
  }

  situacaoDoFoco(focoId: string, situacao: Exclude<SituacaoFoco, 'ATIVO'>, observacao: string | null): Observable<FocoVetorialDto> {
    return this.http.post<FocoVetorialDto>(`${this.api}/foco/${focoId}/situacao`, { situacao, observacao });
  }

  indices(ano?: number, ciclo?: number): Observable<IndicesVetoriaisDto> {
    let params = new HttpParams();
    if (ano) params = params.set('ano', ano);
    if (ciclo) params = params.set('ciclo', ciclo);
    return this.http.get<IndicesVetoriaisDto>(`${this.api}/indices`, { params });
  }

  acoes(situacao?: SituacaoAcaoControleVetorial | null): Observable<AcaoControleVetorialDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : undefined;
    return this.http.get<AcaoControleVetorialDto[]>(`${this.api}/acoes`, { params });
  }

  planejar(dto: AcaoControleVetorialRequestDto): Observable<AcaoControleVetorialDto> {
    return this.http.post<AcaoControleVetorialDto>(`${this.api}/acao`, dto);
  }

  realizar(id: string, dto: { data: string | null; imoveisTrabalhados: number | null; resultado: string | null }): Observable<AcaoControleVetorialDto> {
    return this.http.post<AcaoControleVetorialDto>(`${this.api}/acao/${id}/realizacao`, dto);
  }

  cancelar(id: string, motivo: string): Observable<AcaoControleVetorialDto> {
    return this.http.post<AcaoControleVetorialDto>(`${this.api}/acao/${id}/cancelamento`, { motivo });
  }

  bloqueiosPendentes(): Observable<BloqueioPendenteDto[]> {
    return this.http.get<BloqueioPendenteDto[]>(`${this.api}/bloqueios-pendentes`);
  }
}
