import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RegistroPontoCorrecaoRequestDto, RegistroPontoRequestDto, RegistroPontoResponseDto } from '../models/registro-ponto';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class RegistroPontoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/registro-ponto';

  listarPorProfissional(matricula: string, dataInicio?: string, dataFim?: string): Observable<RegistroPontoResponseDto[]> {
    let params = new HttpParams();
    if (dataInicio && dataFim) {
      params = params.set('dataInicio', dataInicio).set('dataFim', dataFim);
    }
    return this.http.get<RegistroPontoResponseDto[]>(`${this.baseUrl}/profissional/${matricula}`, { params });
  }

  criar(dto: RegistroPontoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  solicitarCorrecao(uuid: string, dto: RegistroPontoCorrecaoRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}/solicitar-correcao`, dto);
  }

  aprovarCorrecao(uuid: string): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}/aprovar-correcao`, null);
  }

  rejeitarCorrecao(uuid: string): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.baseUrl}/${uuid}/rejeitar-correcao`, null);
  }
}
