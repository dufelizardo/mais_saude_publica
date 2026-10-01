import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AcessoJustificadoRequestDto, AcessoJustificadoResponseDto, ProntuarioResponseDto } from '../models/prontuario';

/** Código do 403 do prontuário sem vínculo assistencial (ADR-0076). */
export const VINCULO_AUSENTE = 'VINCULO_ASSISTENCIAL_AUSENTE';

/** A mensagem da recusa, quando o 403 é por falta de vínculo; senão, null. */
export function semVinculo(erro: unknown): string | null {
  if (!(erro instanceof HttpErrorResponse) || erro.status !== 403) return null;
  const corpo = erro.error as { details?: string; message?: string } | null;
  return corpo?.details === VINCULO_AUSENTE ? (corpo.message ?? 'Sem vínculo assistencial com este paciente.') : null;
}

@Injectable({ providedIn: 'root' })
export class ProntuarioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/prontuario';

  buscarPorPacienteId(pacienteId: string): Observable<ProntuarioResponseDto> {
    return this.http.get<ProntuarioResponseDto>(`${this.baseUrl}/${pacienteId}`);
  }

  /** Acesso sem vínculo assistencial, declarado com motivo; vale por algumas horas (ADR-0076). */
  justificarAcesso(pacienteId: string, dto: AcessoJustificadoRequestDto): Observable<AcessoJustificadoResponseDto> {
    return this.http.post<AcessoJustificadoResponseDto>(`${this.baseUrl}/${pacienteId}/acesso-justificado`, dto);
  }
}
