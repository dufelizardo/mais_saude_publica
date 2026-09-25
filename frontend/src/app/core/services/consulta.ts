import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ConsultaRequestDto, ConsultaResponseDto } from '../models/consulta';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class ConsultaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/consulta';

  listar(): Observable<ConsultaResponseDto[]> {
    return this.http.get<ConsultaResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<ConsultaResponseDto> {
    return this.http.get<ConsultaResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: ConsultaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: ConsultaRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
