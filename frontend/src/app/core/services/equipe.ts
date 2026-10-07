import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EquipeRequestDto, EquipeResponseDto, EquipesResponseDto, FuncaoEquipe } from '../models/equipe';

/** Equipes de saúde (ADR-0103): lista com indicadores, detalhe, cadastro, edição, entrada e saída de membros. */
@Injectable({ providedIn: 'root' })
export class EquipeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/equipe';

  listar(unidadeId?: string): Observable<EquipesResponseDto> {
    const params = unidadeId ? new HttpParams().set('unidadeId', unidadeId) : undefined;
    return this.http.get<EquipesResponseDto>(`${this.baseUrl}/`, { params });
  }

  buscar(uuid: string): Observable<EquipeResponseDto> {
    return this.http.get<EquipeResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: EquipeRequestDto): Observable<EquipeResponseDto> {
    return this.http.post<EquipeResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: EquipeRequestDto): Observable<EquipeResponseDto> {
    return this.http.patch<EquipeResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }

  adicionarMembro(equipeId: string, dto: { profissionalMatricula: string; funcao: FuncaoEquipe; microarea?: string; inicio?: string }): Observable<EquipeResponseDto> {
    return this.http.post<EquipeResponseDto>(`${this.baseUrl}/${equipeId}/membro`, dto);
  }

  registrarSaida(membroId: string, motivo: string, fim?: string): Observable<EquipeResponseDto> {
    return this.http.post<EquipeResponseDto>(`${this.baseUrl}/membro/${membroId}/saida`, { motivo, fim });
  }
}
