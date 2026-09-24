import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ResponsabilidadeAdministrativaRequestDto,
  ResponsabilidadeAdministrativaResponseDto,
} from '../models/responsabilidade-administrativa';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class ResponsabilidadeAdministrativaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/responsabilidade-administrativa';

  listarHistorico(matricula: string): Observable<ResponsabilidadeAdministrativaResponseDto[]> {
    return this.http.get<ResponsabilidadeAdministrativaResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: ResponsabilidadeAdministrativaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  encerrar(uuid: string, dataFim: string): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}/encerrar`, null, { params: { dataFim } });
  }
}
