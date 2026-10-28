import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { InstituicaoExternaDto, InstituicaoExternaRequestDto, SetorIntersetorial } from '../models/intersetorialidade';
import { AcompanhamentoSaudeMentalDto } from '../models/saude-mental';

/** O catálogo de instituições externas (ADR-0154) e o encaminhamento intersetorial do caso em saúde mental (ADR-0155). */
@Injectable({ providedIn: 'root' })
export class IntersetorialidadeService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1';

  listar(filtro: { setor?: SetorIntersetorial | ''; ativas?: boolean } = {}): Observable<InstituicaoExternaDto[]> {
    let params = new HttpParams();
    if (filtro.setor) params = params.set('setor', filtro.setor);
    if (filtro.ativas) params = params.set('ativas', 'true');
    return this.http.get<InstituicaoExternaDto[]>(`${this.api}/instituicoes-externas`, { params });
  }

  criar(dto: InstituicaoExternaRequestDto): Observable<InstituicaoExternaDto> {
    return this.http.post<InstituicaoExternaDto>(`${this.api}/instituicao-externa`, dto);
  }

  atualizar(uuid: string, dto: InstituicaoExternaRequestDto): Observable<InstituicaoExternaDto> {
    return this.http.put<InstituicaoExternaDto>(`${this.api}/instituicao-externa/${uuid}`, dto);
  }

  encaminhar(casoId: string, dto: { instituicaoId: string; motivo: string; enviadoEm?: string | null; retornoEsperadoEm: string }): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.api}/saude-mental/acompanhamento/${casoId}/encaminhamento-intersetorial`, dto);
  }

  receber(casoId: string, encaminhamentoId: string, data: string | null): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.base(casoId, encaminhamentoId)}/recebimento`, { data });
  }

  retorno(casoId: string, encaminhamentoId: string, dto: { data?: string | null; desfecho: 'CONCLUIDO' | 'RECUSADO'; retorno: string }): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.base(casoId, encaminhamentoId)}/retorno`, dto);
  }

  cancelar(casoId: string, encaminhamentoId: string, motivo: string): Observable<AcompanhamentoSaudeMentalDto> {
    return this.http.post<AcompanhamentoSaudeMentalDto>(`${this.base(casoId, encaminhamentoId)}/cancelamento`, { motivo });
  }

  private base(casoId: string, encaminhamentoId: string): string {
    return `${this.api}/saude-mental/acompanhamento/${casoId}/encaminhamento-intersetorial/${encaminhamentoId}`;
  }
}
