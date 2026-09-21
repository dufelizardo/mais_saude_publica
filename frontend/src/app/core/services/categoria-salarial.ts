import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CategoriaSalarialRequestDto,
  CategoriaSalarialResponseDto,
} from '../models/categoria-salarial';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class CategoriaSalarialService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/categoria-salarial';

  listar(): Observable<CategoriaSalarialResponseDto[]> {
    return this.http.get<CategoriaSalarialResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: CategoriaSalarialRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: CategoriaSalarialRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
