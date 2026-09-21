import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ComposicaoRemuneratoriaResponseDto } from '../models/composicao-remuneratoria';

@Injectable({ providedIn: 'root' })
export class ComposicaoRemuneratoriaService {
  private readonly http = inject(HttpClient);

  calcular(matricula: string): Observable<ComposicaoRemuneratoriaResponseDto> {
    return this.http.get<ComposicaoRemuneratoriaResponseDto>(`/api/v1/composicao-remuneratoria/profissional/${matricula}`);
  }
}
