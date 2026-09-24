import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  PerfilAdministrativoRequestDto,
  PerfilAdministrativoResponseDto,
} from '../models/perfil-administrativo';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class PerfilAdministrativoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/perfil-administrativo';

  listar(): Observable<PerfilAdministrativoResponseDto[]> {
    return this.http.get<PerfilAdministrativoResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: PerfilAdministrativoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: PerfilAdministrativoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
