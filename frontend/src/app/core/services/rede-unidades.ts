import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  FichaUnidadeDto,
  RedeUnidadesResponseDto,
  SituacaoOperacional,
  TurnoHorarioDto,
  UnidadeCadastroRequestDto,
} from '../models/rede-unidades';

/** Equipamentos de Saúde (ADR-0101): rede, ficha, cadastro e edição por id, horário estruturado e situação. */
@Injectable({ providedIn: 'root' })
export class RedeUnidadesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/unidade-saude';

  rede(): Observable<RedeUnidadesResponseDto> {
    return this.http.get<RedeUnidadesResponseDto>(`${this.baseUrl}/rede`);
  }

  ficha(uuid: string): Observable<FichaUnidadeDto> {
    return this.http.get<FichaUnidadeDto>(`${this.baseUrl}/id/${uuid}`);
  }

  criar(dto: UnidadeCadastroRequestDto): Observable<FichaUnidadeDto> {
    return this.http.post<FichaUnidadeDto>(`${this.baseUrl}/id`, dto);
  }

  atualizar(uuid: string, dto: UnidadeCadastroRequestDto): Observable<FichaUnidadeDto> {
    return this.http.patch<FichaUnidadeDto>(`${this.baseUrl}/id/${uuid}`, dto);
  }

  horarios(uuid: string, funciona24h: boolean, turnos: TurnoHorarioDto[]): Observable<FichaUnidadeDto> {
    return this.http.put<FichaUnidadeDto>(`${this.baseUrl}/id/${uuid}/horarios`, { funciona24h, turnos });
  }

  situacao(uuid: string, situacao: SituacaoOperacional, motivo?: string, previsaoRetorno?: string): Observable<FichaUnidadeDto> {
    return this.http.post<FichaUnidadeDto>(`${this.baseUrl}/id/${uuid}/situacao`, { situacao, motivo, previsaoRetorno });
  }
}
