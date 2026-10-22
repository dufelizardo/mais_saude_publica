import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CapacidadeAssistencialDto,
  CapacidadeAssistencialRequestDto,
  ModeloOperacionalUnidadeDto,
  PerfilOperacionalDto,
  PerfilOperacionalRequestDto,
} from '../models/modelo-operacional';

/** Modelo operacional (ADR-0117): catálogos de perfil e de capacidade, e perfis e capacidades da unidade. */
@Injectable({ providedIn: 'root' })
export class ModeloOperacionalService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  perfis(): Observable<PerfilOperacionalDto[]> {
    return this.http.get<PerfilOperacionalDto[]>(`${this.api}/perfil-operacional/`);
  }

  criarPerfil(dto: PerfilOperacionalRequestDto): Observable<PerfilOperacionalDto> {
    return this.http.post<PerfilOperacionalDto>(`${this.api}/perfil-operacional/`, dto);
  }

  atualizarPerfil(uuid: string, dto: PerfilOperacionalRequestDto): Observable<PerfilOperacionalDto> {
    return this.http.patch<PerfilOperacionalDto>(`${this.api}/perfil-operacional/${uuid}`, dto);
  }

  capacidades(): Observable<CapacidadeAssistencialDto[]> {
    return this.http.get<CapacidadeAssistencialDto[]>(`${this.api}/capacidade-assistencial/`);
  }

  criarCapacidade(dto: CapacidadeAssistencialRequestDto): Observable<CapacidadeAssistencialDto> {
    return this.http.post<CapacidadeAssistencialDto>(`${this.api}/capacidade-assistencial/`, dto);
  }

  atualizarCapacidade(uuid: string, dto: CapacidadeAssistencialRequestDto): Observable<CapacidadeAssistencialDto> {
    return this.http.patch<CapacidadeAssistencialDto>(`${this.api}/capacidade-assistencial/${uuid}`, dto);
  }

  daUnidade(unidadeId: string): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.get<ModeloOperacionalUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional`);
  }

  incluirPerfil(unidadeId: string, perfilId: string, inicio?: string | null): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.post<ModeloOperacionalUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional/perfil`,
      { perfilId, inicio: inicio || null });
  }

  incluirCapacidade(unidadeId: string, capacidadeId: string, inicio?: string | null, observacao?: string | null): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.post<ModeloOperacionalUnidadeDto>(`${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional/capacidade`,
      { capacidadeId, inicio: inicio || null, observacao: observacao || null });
  }

  encerrar(unidadeId: string, tipo: 'perfil' | 'capacidade', associacaoId: string, motivo: string, fim?: string | null): Observable<ModeloOperacionalUnidadeDto> {
    return this.http.post<ModeloOperacionalUnidadeDto>(
      `${this.api}/unidade-saude/id/${unidadeId}/modelo-operacional/${tipo}/${associacaoId}/encerramento`, { motivo, fim: fim || null });
  }
}
