import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import {
  AvaliacaoOdontologicaDto,
  AvaliacaoOdontologicaRequestDto,
  EspecialidadeDto,
  EspecialidadeRequestDto,
} from '../models/saude-bucal';

/** Catálogo de especialidades e avaliação odontológica (ADR-0134). */
@Injectable({ providedIn: 'root' })
export class SaudeBucalService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';
  private catalogo$?: Observable<EspecialidadeDto[]>;

  /** O catálogo muda pouco: uma busca por sessão, renovada quando ele é alterado aqui. */
  especialidades(): Observable<EspecialidadeDto[]> {
    this.catalogo$ ??= this.http.get<EspecialidadeDto[]>(`${this.api}/especialidades`).pipe(shareReplay(1));
    return this.catalogo$;
  }

  criarEspecialidade(dto: EspecialidadeRequestDto): Observable<EspecialidadeDto> {
    this.catalogo$ = undefined;
    return this.http.post<EspecialidadeDto>(`${this.api}/especialidade`, dto);
  }

  atualizarEspecialidade(uuid: string, dto: EspecialidadeRequestDto): Observable<EspecialidadeDto> {
    this.catalogo$ = undefined;
    return this.http.put<EspecialidadeDto>(`${this.api}/especialidade/${uuid}`, dto);
  }

  avaliar(dto: AvaliacaoOdontologicaRequestDto): Observable<AvaliacaoOdontologicaDto> {
    return this.http.post<AvaliacaoOdontologicaDto>(`${this.api}/avaliacao-odontologica`, dto);
  }

  retificar(uuid: string, dto: AvaliacaoOdontologicaRequestDto): Observable<AvaliacaoOdontologicaDto> {
    return this.http.post<AvaliacaoOdontologicaDto>(`${this.api}/avaliacao-odontologica/${uuid}/retificacao`, dto);
  }
}
