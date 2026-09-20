import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { VagaRequestDto, VagaResponseDto } from '../models/vaga';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class VagaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/vaga';

  listar(): Observable<VagaResponseDto[]> {
    return this.http.get<VagaResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<VagaResponseDto> {
    return this.http.get<VagaResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: VagaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: VagaRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
