import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  PerfilPorTipoUnidadeRequestDto,
  PerfilPorTipoUnidadeResponseDto,
} from '../models/perfil-por-tipo-unidade';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class PerfilPorTipoUnidadeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/perfil-por-tipo-unidade';

  listar(): Observable<PerfilPorTipoUnidadeResponseDto[]> {
    return this.http.get<PerfilPorTipoUnidadeResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: PerfilPorTipoUnidadeRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: PerfilPorTipoUnidadeRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
