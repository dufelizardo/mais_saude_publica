import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CapacidadeAdministrativaRequestDto,
  CapacidadeAdministrativaResponseDto,
} from '../models/capacidade-administrativa';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class CapacidadeAdministrativaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/capacidade-administrativa';

  listar(): Observable<CapacidadeAdministrativaResponseDto[]> {
    return this.http.get<CapacidadeAdministrativaResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: CapacidadeAdministrativaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: CapacidadeAdministrativaRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
