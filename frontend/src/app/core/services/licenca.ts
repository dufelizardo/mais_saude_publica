import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { LicencaRequestDto, LicencaResponseDto } from '../models/licenca';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class LicencaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/licenca';

  listarPorProfissional(matricula: string): Observable<LicencaResponseDto[]> {
    return this.http.get<LicencaResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: LicencaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
