import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AjusteIndividualRequestDto, AjusteIndividualResponseDto } from '../models/ajuste-individual';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AjusteIndividualService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/ajuste-individual';

  listarPorProfissional(matricula: string): Observable<AjusteIndividualResponseDto[]> {
    return this.http.get<AjusteIndividualResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: AjusteIndividualRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
