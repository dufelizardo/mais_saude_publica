import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AgendaResponseDto,
  BlocoAgendaRequestDto,
  BlocoAgendaResponseDto,
  BloqueioAgendaRequestDto,
  BloqueioAgendaResponseDto,
  ItemAgendaDto,
} from '../models/agenda';
import { SuccessResponseDto } from '../models/profissional';

/** Agenda do profissional por unidade (ADR-0091): leitura, vagas, blocos e bloqueios. */
@Injectable({ providedIn: 'root' })
export class AgendaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/agenda';

  private periodo(profissionalMatricula: string, unidadeId: string, de: string, ate: string): HttpParams {
    return new HttpParams().set('profissionalMatricula', profissionalMatricula).set('unidadeId', unidadeId).set('de', de).set('ate', ate);
  }

  /** Dia a dia, com o resumo do período (até 62 dias). */
  agenda(profissionalMatricula: string, unidadeId: string, de: string, ate: string): Observable<AgendaResponseDto> {
    return this.http.get<AgendaResponseDto>(`${this.baseUrl}/`, { params: this.periodo(profissionalMatricula, unidadeId, de, ate) });
  }

  /** Vagas livres a partir de agora. */
  vagas(profissionalMatricula: string, unidadeId: string, de: string, ate: string): Observable<ItemAgendaDto[]> {
    return this.http.get<ItemAgendaDto[]>(`${this.baseUrl}/vagas`, { params: this.periodo(profissionalMatricula, unidadeId, de, ate) });
  }

  listarBlocos(profissionalMatricula: string, unidadeId: string): Observable<BlocoAgendaResponseDto[]> {
    return this.http.get<BlocoAgendaResponseDto[]>(`${this.baseUrl}/bloco`, {
      params: new HttpParams().set('profissionalMatricula', profissionalMatricula).set('unidadeId', unidadeId),
    });
  }

  /** 409 quando cruza com outro bloco do profissional, em qualquer unidade. */
  criarBloco(dto: BlocoAgendaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/bloco`, dto);
  }

  encerrarBloco(uuid: string, vigenteAte: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/bloco/${uuid}/encerramento`, { vigenteAte });
  }

  listarBloqueios(profissionalMatricula: string, unidadeId: string, de: string, ate: string): Observable<BloqueioAgendaResponseDto[]> {
    return this.http.get<BloqueioAgendaResponseDto[]>(`${this.baseUrl}/bloqueio`, { params: this.periodo(profissionalMatricula, unidadeId, de, ate) });
  }

  criarBloqueio(dto: BloqueioAgendaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/bloqueio`, dto);
  }

  removerBloqueio(uuid: string): Observable<SuccessResponseDto> {
    return this.http.delete<SuccessResponseDto>(`${this.baseUrl}/bloqueio/${uuid}`);
  }
}
