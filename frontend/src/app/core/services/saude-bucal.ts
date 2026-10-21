import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import {
  AvaliacaoOdontologicaDto,
  CondicaoOdontologicaDto,
  AvaliacaoOdontologicaRequestDto,
  EspecialidadeDto,
  EspecialidadeRequestDto,
  MarcacaoOdontogramaDto,
  OdontogramaDto,
  RegistroOdontogramaDto,
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

  // ── Odontograma (ADR-0135) ─────────────────────────────────────────────────────────────────────

  condicoes(): Observable<CondicaoOdontologicaDto[]> {
    return this.http.get<CondicaoOdontologicaDto[]>(`${this.api}/condicoes-odontologicas`);
  }

  marcar(avaliacaoId: string, marcacoes: MarcacaoOdontogramaDto[], data?: string | null): Observable<OdontogramaDto> {
    return this.http.post<OdontogramaDto>(`${this.api}/avaliacao-odontologica/${avaliacaoId}/odontograma`, { data: data ?? null, marcacoes });
  }

  anular(registroId: string, motivo: string): Observable<RegistroOdontogramaDto> {
    return this.http.post<RegistroOdontogramaDto>(`${this.api}/registro-odontograma/${registroId}/anulacao`, { motivo });
  }

  odontograma(pacienteId: string, data?: string | null): Observable<OdontogramaDto> {
    return this.http.get<OdontogramaDto>(`${this.api}/paciente/${pacienteId}/odontograma`, { params: data ? { data } : {} });
  }

  historicoDoDente(pacienteId: string, dente: number): Observable<RegistroOdontogramaDto[]> {
    return this.http.get<RegistroOdontogramaDto[]>(`${this.api}/paciente/${pacienteId}/odontograma/dente/${dente}`);
  }
}
