import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AtrasadoVacinacaoDto,
  CadeiaFrioDto,
  CadernetaVacinacaoDto,
  CamaraRefrigeracaoDto,
  CoberturaVacinalDto,
  EntradaLoteRequestDto,
  EsquemaVacinalDto,
  EsquemaVacinalRequestDto,
  EstoqueSalaVacinaDto,
  FiltroCobertura,
  ImunobiologicoDto,
  LeituraTemperaturaDto,
  LeituraTemperaturaRequestDto,
  LoteImunobiologicoDto,
  MotivoPerdaImunobiologico,
  MovimentacaoImunobiologicoDto,
  RegistroVacinacaoDto,
  RegistroVacinacaoRequestDto,
  VacinacaoDoDiaDto,
} from '../models/imunizacao';

/**
 * Imunização (ADR-0147): catálogo, calendário, registro de vacinação, caderneta e sala de vacina; estoque e cadeia de
 * frio (ADR-0148); cobertura e busca ativa (ADR-0149).
 */
@Injectable({ providedIn: 'root' })
export class ImunizacaoService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  imunobiologicos(): Observable<ImunobiologicoDto[]> {
    return this.http.get<ImunobiologicoDto[]>(`${this.api}/imunobiologicos`);
  }

  calendario(): Observable<EsquemaVacinalDto[]> {
    return this.http.get<EsquemaVacinalDto[]>(`${this.api}/calendario-vacinal`);
  }

  registrar(dto: RegistroVacinacaoRequestDto): Observable<RegistroVacinacaoDto> {
    return this.http.post<RegistroVacinacaoDto>(`${this.api}/vacinacao`, dto);
  }

  retificar(uuid: string, dto: RegistroVacinacaoRequestDto): Observable<RegistroVacinacaoDto> {
    return this.http.post<RegistroVacinacaoDto>(`${this.api}/vacinacao/${uuid}/retificacao`, dto);
  }

  anular(uuid: string, motivo: string): Observable<RegistroVacinacaoDto> {
    return this.http.post<RegistroVacinacaoDto>(`${this.api}/vacinacao/${uuid}/anulacao`, { motivo });
  }

  caderneta(pacienteId: string): Observable<CadernetaVacinacaoDto> {
    return this.http.get<CadernetaVacinacaoDto>(`${this.api}/paciente/${pacienteId}/vacinacao`);
  }

  doDia(unidadeId: string, data?: string): Observable<VacinacaoDoDiaDto> {
    let params = new HttpParams().set('unidadeId', unidadeId);
    if (data) {
      params = params.set('data', data);
    }
    return this.http.get<VacinacaoDoDiaDto>(`${this.api}/vacinacao/dia`, { params });
  }

  atualizarEsquema(uuid: string, dto: EsquemaVacinalRequestDto): Observable<EsquemaVacinalDto> {
    return this.http.put<EsquemaVacinalDto>(`${this.api}/esquema-vacinal/${uuid}`, dto);
  }

  atrasados(unidadeId: string, equipeId?: string, microareaId?: string): Observable<AtrasadoVacinacaoDto[]> {
    let params = new HttpParams().set('unidadeId', unidadeId);
    if (equipeId) {
      params = params.set('equipeId', equipeId);
    }
    if (microareaId) {
      params = params.set('microareaId', microareaId);
    }
    return this.http.get<AtrasadoVacinacaoDto[]>(`${this.api}/vacinacao/atrasados`, { params });
  }

  // ── Cobertura (ADR-0149) ──

  cobertura(filtro: FiltroCobertura = {}): Observable<CoberturaVacinalDto> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(filtro)) {
      if (v) {
        params = params.set(k, v);
      }
    }
    return this.http.get<CoberturaVacinalDto>(`${this.api}/vacinacao/cobertura`, { params });
  }

  // ── Estoque (ADR-0148) ──

  estoque(unidadeId: string): Observable<EstoqueSalaVacinaDto> {
    return this.http.get<EstoqueSalaVacinaDto>(`${this.api}/sala-de-vacina/estoque`, { params: new HttpParams().set('unidadeId', unidadeId) });
  }

  entrada(dto: EntradaLoteRequestDto): Observable<LoteImunobiologicoDto> {
    return this.http.post<LoteImunobiologicoDto>(`${this.api}/sala-de-vacina/entrada`, dto);
  }

  perda(loteId: string, doses: number, motivo: MotivoPerdaImunobiologico, justificativa: string | null): Observable<LoteImunobiologicoDto> {
    return this.http.post<LoteImunobiologicoDto>(`${this.api}/lote-imunobiologico/${loteId}/perda`, { doses, motivo, justificativa });
  }

  ajuste(loteId: string, saldo: number, justificativa: string): Observable<LoteImunobiologicoDto> {
    return this.http.post<LoteImunobiologicoDto>(`${this.api}/lote-imunobiologico/${loteId}/ajuste`, { saldo, justificativa });
  }

  avaliar(loteId: string, decisao: 'LIBERAR' | 'INUTILIZAR', parecer: string): Observable<LoteImunobiologicoDto> {
    return this.http.post<LoteImunobiologicoDto>(`${this.api}/lote-imunobiologico/${loteId}/avaliacao`, { decisao, parecer });
  }

  movimentacoes(loteId: string): Observable<MovimentacaoImunobiologicoDto[]> {
    return this.http.get<MovimentacaoImunobiologicoDto[]>(`${this.api}/lote-imunobiologico/${loteId}/movimentacoes`);
  }

  // ── Cadeia de frio (ADR-0148) ──

  cadeiaDeFrio(unidadeId: string, data?: string): Observable<CadeiaFrioDto> {
    let params = new HttpParams().set('unidadeId', unidadeId);
    if (data) {
      params = params.set('data', data);
    }
    return this.http.get<CadeiaFrioDto>(`${this.api}/sala-de-vacina/cadeia-de-frio`, { params });
  }

  criarCamara(unidadeId: string, nome: string, temperaturaMinima: number | null, temperaturaMaxima: number | null): Observable<CamaraRefrigeracaoDto> {
    return this.http.post<CamaraRefrigeracaoDto>(`${this.api}/camara-refrigeracao`, { unidadeId, nome, temperaturaMinima, temperaturaMaxima });
  }

  atualizarCamara(uuid: string, unidadeId: string, nome: string, temperaturaMinima: number, temperaturaMaxima: number, ativa: boolean): Observable<CamaraRefrigeracaoDto> {
    return this.http.put<CamaraRefrigeracaoDto>(`${this.api}/camara-refrigeracao/${uuid}`, { unidadeId, nome, temperaturaMinima, temperaturaMaxima, ativa });
  }

  leitura(camaraId: string, dto: LeituraTemperaturaRequestDto): Observable<LeituraTemperaturaDto> {
    return this.http.post<LeituraTemperaturaDto>(`${this.api}/camara-refrigeracao/${camaraId}/leitura`, dto);
  }
}
