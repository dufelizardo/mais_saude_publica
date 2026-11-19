import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { BemDto, BemRequest, ClasseBem, DetalheBemDto, SituacaoBem, TransferenciaBemDto } from '../models/patrimonio';

/** Patrimônio, fatia PM1 (ADR-0215). */
@Injectable({ providedIn: 'root' })
export class PatrimonioService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/patrimonio';

  bens(filtro: { unidadeId?: string; classe?: ClasseBem; situacao?: SituacaoBem; busca?: string }): Observable<BemDto[]> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(filtro)) if (v) params = params.set(k, v);
    return this.http.get<BemDto[]>(`${this.api}/bens`, { params });
  }

  meusBens(): Observable<BemDto[]> {
    return this.http.get<BemDto[]>(`${this.api}/meus-bens`);
  }

  bem(uuid: string): Observable<DetalheBemDto> {
    return this.http.get<DetalheBemDto>(`${this.api}/bem/${uuid}`);
  }

  cadastrar(dto: BemRequest): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem`, dto);
  }

  editar(uuid: string, dto: { descricao: string; marca: string | null; modelo: string | null; numeroSerie: string | null; setorId: string | null;
    observacao: string | null }): Observable<DetalheBemDto> {
    return this.http.put<DetalheBemDto>(`${this.api}/bem/${uuid}`, dto);
  }

  tombar(uuid: string, dto: { classe: ClasseBem; marca: string | null; modelo: string | null; numeroSerie: string | null; setorId: string | null;
    responsavelMatricula: string; observacao: string | null }): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/tombamento`, dto);
  }

  responsavel(uuid: string, responsavelMatricula: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/responsavel`, { responsavelMatricula });
  }

  situacao(uuid: string, situacao: SituacaoBem): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/situacao`, { situacao });
  }

  veiculo(uuid: string, veiculoId: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/veiculo`, { veiculoId });
  }

  transferencias(pendentes?: boolean): Observable<TransferenciaBemDto[]> {
    return this.http.get<TransferenciaBemDto[]>(`${this.api}/transferencias`, { params: pendentes ? { pendentes: true } : {} });
  }

  transferir(uuid: string, destinoId: string, motivo: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/bem/${uuid}/transferencia`, { destinoId, motivo });
  }

  aceitar(transferenciaId: string, setorId: string | null, responsavelMatricula: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/transferencia/${transferenciaId}/aceite`, { setorId, responsavelMatricula });
  }

  recusar(transferenciaId: string, motivo: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/transferencia/${transferenciaId}/recusa`, { motivo });
  }

  cancelar(transferenciaId: string): Observable<DetalheBemDto> {
    return this.http.post<DetalheBemDto>(`${this.api}/transferencia/${transferenciaId}/cancelamento`, {});
  }
}
