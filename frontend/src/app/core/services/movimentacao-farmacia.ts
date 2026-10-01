import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MovimentacaoFarmaciaRequestDto, MovimentacaoFarmaciaResponseDto } from '../models/movimentacao-farmacia';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class MovimentacaoFarmaciaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/movimentacao-farmacia';

  /** Só perda e ajuste de inventário; 422 quando a perda passa do saldo. */
  registrar(dto: MovimentacaoFarmaciaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  /** Responde 404 quando o lote não tem registros no livro (convenção dos endpoints de listagem). */
  extratoDoLote(loteId: string): Observable<MovimentacaoFarmaciaResponseDto[]> {
    return this.http.get<MovimentacaoFarmaciaResponseDto[]>(`${this.baseUrl}/lote/${loteId}`);
  }
}
