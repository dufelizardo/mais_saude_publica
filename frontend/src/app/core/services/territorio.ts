import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AreaDetalheDto,
  AreaRequestDto,
  ArvoreTerritorialDto,
  MicroareaRequestDto,
  SegmentoDto,
  SegmentoRequestDto,
  TerritorioNoDto,
  TerritorioRequestDto,
  UnidadeNoMapaDto,
} from '../models/territorio';

/** Território (ADR-0125): a árvore, o cadastro, a equipe da área, o ACS da microárea e as coordenadas da unidade. */
@Injectable({ providedIn: 'root' })
export class TerritorioService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/territorio';

  arvore(): Observable<ArvoreTerritorialDto> {
    return this.http.get<ArvoreTerritorialDto>(`${this.api}/arvore`);
  }

  area(uuid: string): Observable<AreaDetalheDto> {
    return this.http.get<AreaDetalheDto>(`${this.api}/area/${uuid}`);
  }

  unidades(): Observable<UnidadeNoMapaDto[]> {
    return this.http.get<UnidadeNoMapaDto[]>(`${this.api}/unidades`);
  }

  salvarSegmento(uuid: string | null, dto: SegmentoRequestDto): Observable<SegmentoDto> {
    return uuid ? this.http.patch<SegmentoDto>(`${this.api}/segmento/${uuid}`, dto) : this.http.post<SegmentoDto>(`${this.api}/segmento`, dto);
  }

  salvarTerritorio(uuid: string | null, dto: TerritorioRequestDto): Observable<TerritorioNoDto> {
    return uuid
      ? this.http.patch<TerritorioNoDto>(`${this.api}/territorio/${uuid}`, dto)
      : this.http.post<TerritorioNoDto>(`${this.api}/territorio`, dto);
  }

  salvarArea(uuid: string | null, dto: AreaRequestDto): Observable<AreaDetalheDto> {
    return uuid ? this.http.patch<AreaDetalheDto>(`${this.api}/area/${uuid}`, dto) : this.http.post<AreaDetalheDto>(`${this.api}/area`, dto);
  }

  salvarMicroarea(uuid: string | null, dto: MicroareaRequestDto): Observable<AreaDetalheDto> {
    return uuid
      ? this.http.patch<AreaDetalheDto>(`${this.api}/microarea/${uuid}`, dto)
      : this.http.post<AreaDetalheDto>(`${this.api}/microarea`, dto);
  }

  atribuirEquipe(areaId: string, equipeId: string, inicio?: string | null, motivo?: string | null): Observable<AreaDetalheDto> {
    return this.http.post<AreaDetalheDto>(`${this.api}/area/${areaId}/responsabilidade`, { equipeId, inicio: inicio || null, motivo: motivo || null });
  }

  atribuirAcs(microareaId: string, membroId: string, inicio?: string | null, motivo?: string | null): Observable<AreaDetalheDto> {
    return this.http.post<AreaDetalheDto>(`${this.api}/microarea/${microareaId}/atribuicao`, { membroId, inicio: inicio || null, motivo: motivo || null });
  }

  coordenadas(unidadeId: string, latitude: number | null, longitude: number | null): Observable<UnidadeNoMapaDto> {
    return this.http.put<UnidadeNoMapaDto>(`${this.api}/unidade/${unidadeId}/coordenadas`, { latitude, longitude });
  }

  encerrarAcs(microareaId: string, motivo: string, fim?: string | null): Observable<AreaDetalheDto> {
    return this.http.post<AreaDetalheDto>(`${this.api}/microarea/${microareaId}/atribuicao/encerrar`, { motivo, fim: fim || null });
  }
}
