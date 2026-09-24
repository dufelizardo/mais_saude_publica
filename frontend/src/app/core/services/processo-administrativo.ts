import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ProcessoAdministrativoRequestDto,
  ProcessoAdministrativoResponseDto,
} from '../models/processo-administrativo';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class ProcessoAdministrativoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/processo-administrativo';

  listar(): Observable<ProcessoAdministrativoResponseDto[]> {
    return this.http.get<ProcessoAdministrativoResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: ProcessoAdministrativoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: ProcessoAdministrativoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
