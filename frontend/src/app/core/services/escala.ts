import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AplicacaoModeloResponseDto, CopiaSemanaResponseDto, EscalaSemanaResponseDto, ModeloJornada, TurnoEscalaDto, TurnoEscalaRequestDto } from '../models/escala';

/** Escalas (ADR-0105): semana da unidade, turnos, designação e troca, remoção e cópia de semana. */
@Injectable({ providedIn: 'root' })
export class EscalaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/escala';

  semana(unidadeId: string, semana?: string, equipeId?: string): Observable<EscalaSemanaResponseDto> {
    let params = new HttpParams().set('unidadeId', unidadeId);
    if (semana) params = params.set('semana', semana);
    if (equipeId) params = params.set('equipeId', equipeId);
    return this.http.get<EscalaSemanaResponseDto>(`${this.baseUrl}/`, { params });
  }

  criar(dto: TurnoEscalaRequestDto): Observable<TurnoEscalaDto> {
    return this.http.post<TurnoEscalaDto>(`${this.baseUrl}/turno`, dto);
  }

  atualizar(uuid: string, dto: TurnoEscalaRequestDto): Observable<TurnoEscalaDto> {
    return this.http.patch<TurnoEscalaDto>(`${this.baseUrl}/turno/${uuid}`, dto);
  }

  designar(uuid: string, profissionalMatricula: string, motivo?: string): Observable<TurnoEscalaDto> {
    return this.http.post<TurnoEscalaDto>(`${this.baseUrl}/turno/${uuid}/designar`, { profissionalMatricula, motivo });
  }

  remover(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/turno/${uuid}`);
  }

  aplicarModelo(dto: {
    unidadeId: string;
    profissionalMatricula: string;
    modelo: ModeloJornada;
    semana: string;
    inicio?: string;
    intervaloMinutos?: number;
    equipeId?: string;
  }): Observable<AplicacaoModeloResponseDto> {
    return this.http.post<AplicacaoModeloResponseDto>(`${this.baseUrl}/aplicar-modelo`, dto);
  }

  copiarSemana(unidadeId: string, origem: string, destino: string): Observable<CopiaSemanaResponseDto> {
    return this.http.post<CopiaSemanaResponseDto>(`${this.baseUrl}/copiar-semana`, { unidadeId, origem, destino });
  }
}
