import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FolhaPagamentoRequestDto, FolhaPagamentoResponseDto } from '../models/folha-pagamento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class FolhaPagamentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/folha-pagamento';

  listarPorProfissional(matricula: string): Observable<FolhaPagamentoResponseDto[]> {
    return this.http.get<FolhaPagamentoResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  listarPorCompetencia(competencia: string): Observable<FolhaPagamentoResponseDto[]> {
    return this.http.get<FolhaPagamentoResponseDto[]>(`${this.baseUrl}/competencia`, { params: { valor: competencia } });
  }

  criar(dto: FolhaPagamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
