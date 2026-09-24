import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  NecessidadeDePessoalRequestDto,
  NecessidadeDePessoalResponseDto,
} from '../models/necessidade-de-pessoal';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class NecessidadeDePessoalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/necessidade-de-pessoal';

  listar(): Observable<NecessidadeDePessoalResponseDto[]> {
    return this.http.get<NecessidadeDePessoalResponseDto[]>(`${this.baseUrl}/`);
  }

  criar(dto: NecessidadeDePessoalRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: NecessidadeDePessoalRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }

  vincularVaga(uuid: string, vagaId: string): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}/vincular-vaga`, null, { params: { vagaId } });
  }
}
