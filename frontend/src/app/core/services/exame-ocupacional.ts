import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ExameOcupacionalRequestDto, ExameOcupacionalResponseDto } from '../models/exame-ocupacional';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class ExameOcupacionalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/exame-ocupacional';

  listarPorProfissional(matricula: string): Observable<ExameOcupacionalResponseDto[]> {
    return this.http.get<ExameOcupacionalResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`);
  }

  criar(dto: ExameOcupacionalRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }
}
