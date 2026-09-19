import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CargoRequestDto, CargoResponseDto } from '../models/cargo';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class CargoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/cargo';

  listar(): Observable<CargoResponseDto[]> {
    return this.http.get<CargoResponseDto[]>(`${this.baseUrl}/`);
  }

  buscarPorId(uuid: string): Observable<CargoResponseDto> {
    return this.http.get<CargoResponseDto>(`${this.baseUrl}/${uuid}`);
  }

  criar(dto: CargoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  atualizar(uuid: string, dto: CargoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}`, dto);
  }
}
