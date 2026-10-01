import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MedicamentoRequestDto, MedicamentoResponseDto } from '../models/medicamento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class MedicamentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/medicamento';

  listar(): Observable<MedicamentoResponseDto[]> {
    return this.http.get<MedicamentoResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: MedicamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: MedicamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
