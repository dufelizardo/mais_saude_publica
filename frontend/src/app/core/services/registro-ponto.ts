import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RegistroPontoResponseDto } from '../models/registro-ponto';

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
}
