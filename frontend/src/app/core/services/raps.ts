import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AcolhimentoNoturnoDto, PainelRapsDto, PermanenciaAcolhimentoDto } from '../models/saude-mental';

/** A Rede de Atenção Psicossocial e o acolhimento noturno do CAPS (ADR-0153). */
@Injectable({ providedIn: 'root' })
export class RapsService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/saude-mental';

  painel(): Observable<PainelRapsDto> {
    return this.http.get<PainelRapsDto>(`${this.api}/raps`);
  }

  acolhimento(unidadeId: string): Observable<AcolhimentoNoturnoDto> {
    return this.http.get<AcolhimentoNoturnoDto>(`${this.api}/acolhimento-noturno`, { params: new HttpParams().set('unidadeId', unidadeId) });
  }

  configurar(unidadeId: string, vagas: number, permanenciaMaximaDias: number): Observable<AcolhimentoNoturnoDto> {
    return this.http.put<AcolhimentoNoturnoDto>(`${this.api}/acolhimento-noturno/configuracao`, { vagas, permanenciaMaximaDias },
      { params: new HttpParams().set('unidadeId', unidadeId) });
  }

  entrar(dto: { unidadeId: string; pacienteId: string; entrada?: string | null; saidaPrevista: string; justificativa?: string | null }): Observable<PermanenciaAcolhimentoDto> {
    return this.http.post<PermanenciaAcolhimentoDto>(`${this.api}/acolhimento-noturno`, dto);
  }

  prorrogar(uuid: string, saidaPrevista: string, justificativa: string | null): Observable<PermanenciaAcolhimentoDto> {
    return this.http.post<PermanenciaAcolhimentoDto>(`${this.api}/acolhimento-noturno/${uuid}/prorrogacao`, { saidaPrevista, justificativa });
  }

  sair(uuid: string, motivo: string): Observable<PermanenciaAcolhimentoDto> {
    return this.http.post<PermanenciaAcolhimentoDto>(`${this.api}/acolhimento-noturno/${uuid}/saida`, { motivo });
  }
}
