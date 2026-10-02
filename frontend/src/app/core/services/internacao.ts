import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  IndicadoresLeitosDto,
  InternacaoRequestDto,
  InternacaoResponseDto,
  LeitoMapaDto,
  LeitoRequestDto,
  StatusInternacao,
  TipoAlta,
} from '../models/internacao';

/** Leitos e internação (ADR-0098): mapa, indicadores, cadastro de leito, internação, troca de leito e alta. */
@Injectable({ providedIn: 'root' })
export class InternacaoService {
  private readonly http = inject(HttpClient);
  private readonly leitoUrl = '/api/v1/leito';
  private readonly internacaoUrl = '/api/v1/internacao';

  // ── Leitos ──

  /** Mapa: leitos em uso (com `todos`, também os fora de uso), com o paciente de cada ocupado. */
  mapa(unidadeId?: string, todos = false): Observable<LeitoMapaDto[]> {
    let params = new HttpParams();
    if (unidadeId) params = params.set('unidadeId', unidadeId);
    if (todos) params = params.set('todos', 'true');
    return this.http.get<LeitoMapaDto[]>(`${this.leitoUrl}/`, { params });
  }

  indicadores(unidadeId?: string): Observable<IndicadoresLeitosDto> {
    const params = unidadeId ? new HttpParams().set('unidadeId', unidadeId) : undefined;
    return this.http.get<IndicadoresLeitosDto>(`${this.leitoUrl}/indicadores`, { params });
  }

  criarLeito(dto: LeitoRequestDto): Observable<LeitoMapaDto> {
    return this.http.post<LeitoMapaDto>(`${this.leitoUrl}/`, dto);
  }

  atualizarLeito(uuid: string, dto: LeitoRequestDto): Observable<LeitoMapaDto> {
    return this.http.patch<LeitoMapaDto>(`${this.leitoUrl}/${uuid}`, dto);
  }

  bloquear(uuid: string, motivo: string): Observable<LeitoMapaDto> {
    return this.http.post<LeitoMapaDto>(`${this.leitoUrl}/${uuid}/bloqueio`, { motivo });
  }

  desbloquear(uuid: string): Observable<LeitoMapaDto> {
    return this.http.post<LeitoMapaDto>(`${this.leitoUrl}/${uuid}/desbloqueio`, {});
  }

  /** Registra a higienização: o leito volta a livre. */
  liberar(uuid: string): Observable<LeitoMapaDto> {
    return this.http.post<LeitoMapaDto>(`${this.leitoUrl}/${uuid}/liberacao`, {});
  }

  // ── Internações ──

  listar(filtro: { unidadeId?: string; status?: StatusInternacao | ''; pacienteId?: string } = {}): Observable<InternacaoResponseDto[]> {
    let params = new HttpParams();
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor) params = params.set(chave, valor);
    }
    return this.http.get<InternacaoResponseDto[]>(`${this.internacaoUrl}/`, { params });
  }

  /** Detalhe com motivo, sumário e movimentos; leitura auditada. */
  buscar(uuid: string): Observable<InternacaoResponseDto> {
    return this.http.get<InternacaoResponseDto>(`${this.internacaoUrl}/${uuid}`);
  }

  internar(dto: InternacaoRequestDto): Observable<InternacaoResponseDto> {
    return this.http.post<InternacaoResponseDto>(`${this.internacaoUrl}/`, dto);
  }

  trocarLeito(uuid: string, leitoId: string, profissionalMatricula: string, motivo: string): Observable<InternacaoResponseDto> {
    return this.http.post<InternacaoResponseDto>(`${this.internacaoUrl}/${uuid}/troca-de-leito`, { leitoId, profissionalMatricula, motivo });
  }

  darAlta(uuid: string, dto: { tipoAlta: TipoAlta; sumario: string; medicoMatricula: string; altaEm?: string }): Observable<InternacaoResponseDto> {
    return this.http.post<InternacaoResponseDto>(`${this.internacaoUrl}/${uuid}/alta`, dto);
  }
}
