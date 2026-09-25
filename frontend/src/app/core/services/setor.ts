import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SetorRequestDto, SetorResponseDto } from '../models/setor';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class SetorService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/setor';

  listar(): Observable<SetorResponseDto[]> {
    return this.http.get<SetorResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<SetorResponseDto> {
    return this.http.get<SetorResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: SetorRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: SetorRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
