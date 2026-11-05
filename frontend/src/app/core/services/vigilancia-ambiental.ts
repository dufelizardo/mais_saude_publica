import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AmostraAguaDto,
  PontoColetaAguaDto,
  RiscoAmbientalDto,
  SituacaoRiscoAmbiental,
  TipoAbastecimentoAgua,
  TipoRiscoAmbiental,
} from '../models/vigilancia-ambiental';

/** Vigilância ambiental, fatia V4c da Vigilância (ADR-0164). */
@Injectable({ providedIn: 'root' })
export class VigilanciaAmbientalService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/ambiental';

  pontos(): Observable<PontoColetaAguaDto[]> {
    return this.http.get<PontoColetaAguaDto[]>(`${this.api}/pontos-agua`);
  }

  criarPonto(dto: { nome: string; tipo: TipoAbastecimentoAgua; local: string; latitude: number | null; longitude: number | null; microareaId: string | null }): Observable<PontoColetaAguaDto> {
    return this.http.post<PontoColetaAguaDto>(`${this.api}/ponto-agua`, dto);
  }

  amostras(filtro: { pontoId?: string; naoConformes?: boolean; desde?: string } = {}): Observable<AmostraAguaDto[]> {
    let params = new HttpParams();
    if (filtro.pontoId) params = params.set('pontoId', filtro.pontoId);
    if (filtro.naoConformes != null) params = params.set('naoConformes', filtro.naoConformes);
    if (filtro.desde) params = params.set('desde', filtro.desde);
    return this.http.get<AmostraAguaDto[]>(`${this.api}/amostras-agua`, { params });
  }

  registrarAmostra(dto: { pontoId: string; dataColeta: string | null; coliformesTotais: boolean | null; ecoli: boolean | null; cloroResidual: number | null; turbidez: number | null; fluor: number | null }): Observable<AmostraAguaDto> {
    return this.http.post<AmostraAguaDto>(`${this.api}/amostra-agua`, dto);
  }

  registrarMedida(id: string, medida: string): Observable<AmostraAguaDto> {
    return this.http.post<AmostraAguaDto>(`${this.api}/amostra-agua/${id}/medida`, { medida });
  }

  riscos(situacao?: SituacaoRiscoAmbiental): Observable<RiscoAmbientalDto[]> {
    const params = situacao ? new HttpParams().set('situacao', situacao) : undefined;
    return this.http.get<RiscoAmbientalDto[]>(`${this.api}/riscos`, { params });
  }

  registrarRisco(dto: { tipo: TipoRiscoAmbiental; descricao: string; local: string; populacaoExposta: number | null; latitude: number | null; longitude: number | null; microareaId: string | null }): Observable<RiscoAmbientalDto> {
    return this.http.post<RiscoAmbientalDto>(`${this.api}/risco`, dto);
  }

  controlar(id: string, motivo: string): Observable<RiscoAmbientalDto> {
    return this.http.post<RiscoAmbientalDto>(`${this.api}/risco/${id}/controle`, { motivo });
  }
}
