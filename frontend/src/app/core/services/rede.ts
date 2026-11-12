import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CopiaPpiDto,
  OfertaAssistencialDto,
  PainelPpiDto,
  PainelRedeTematicaDto,
  PainelReferenciasDto,
  RedeTematica,
  ReferenciaRedeDto,
  SaldoPpiDto,
  SugestaoReferenciaDto,
} from '../models/rede';

/** Gestão da Rede (ADR-0179): oferta, cotas da PPI e o saldo que a Regulação consulta. */
@Injectable({ providedIn: 'root' })
export class RedeService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/rede';

  painel(competencia: string): Observable<PainelPpiDto> {
    return this.http.get<PainelPpiDto>(`${this.api}/ppi`, { params: { competencia } });
  }

  saldo(procedimentoId: string, executanteId: string, solicitanteId: string, competencia: string): Observable<SaldoPpiDto> {
    return this.http.get<SaldoPpiDto>(`${this.api}/ppi/saldo`, { params: { procedimentoId, executanteId, solicitanteId, competencia } });
  }

  criarOferta(dto: { executanteId: string; procedimentoId: string; competencia: string; quantidade: number; observacao?: string | null }): Observable<OfertaAssistencialDto> {
    return this.http.post<OfertaAssistencialDto>(`${this.api}/oferta`, dto);
  }

  ajustarOferta(uuid: string, quantidade: number, observacao?: string | null): Observable<OfertaAssistencialDto> {
    return this.http.put<OfertaAssistencialDto>(`${this.api}/oferta/${uuid}`, { quantidade, observacao: observacao ?? null });
  }

  criarCota(ofertaId: string, beneficiariaId: string, quantidade: number): Observable<OfertaAssistencialDto> {
    return this.http.post<OfertaAssistencialDto>(`${this.api}/oferta/${ofertaId}/cota`, { beneficiariaId, quantidade });
  }

  ajustarCota(uuid: string, quantidade: number): Observable<OfertaAssistencialDto> {
    return this.http.put<OfertaAssistencialDto>(`${this.api}/cota/${uuid}`, { quantidade });
  }

  removerCota(uuid: string): Observable<OfertaAssistencialDto> {
    return this.http.delete<OfertaAssistencialDto>(`${this.api}/cota/${uuid}`);
  }

  copiar(origem: string, destino: string): Observable<CopiaPpiDto> {
    return this.http.post<CopiaPpiDto>(`${this.api}/ppi/copia`, { origem, destino });
  }

  // ── Redes temáticas e referência (ADR-0197) ──

  redeTematica(rede: RedeTematica): Observable<PainelRedeTematicaDto> {
    return this.http.get<PainelRedeTematicaDto>(`${this.api}/tematica/${rede}`);
  }

  referencias(procedimentoId: string | null): Observable<PainelReferenciasDto> {
    return this.http.get<PainelReferenciasDto>(`${this.api}/referencia`, { params: procedimentoId ? { procedimentoId } : {} });
  }

  registrarReferencia(dto: { origemId: string; procedimentoId: string; executanteId: string; inicio?: string | null; observacao?: string | null }): Observable<ReferenciaRedeDto> {
    return this.http.post<ReferenciaRedeDto>(`${this.api}/referencia`, dto);
  }

  encerrarReferencia(uuid: string): Observable<ReferenciaRedeDto> {
    return this.http.post<ReferenciaRedeDto>(`${this.api}/referencia/${uuid}/encerramento`, {});
  }

  sugestaoDeReferencia(procedimentoId: string, solicitanteId: string): Observable<SugestaoReferenciaDto> {
    return this.http.get<SugestaoReferenciaDto>(`${this.api}/referencia/sugestao`, { params: { procedimentoId, solicitanteId } });
  }
}
