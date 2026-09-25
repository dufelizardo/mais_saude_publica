import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProntuarioResponseDto } from '../models/prontuario';

@Injectable({ providedIn: 'root' })
export class ProntuarioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/prontuario';

  buscarPorPacienteId(pacienteId: string): Observable<ProntuarioResponseDto> {
    return this.http.get<ProntuarioResponseDto>(`${this.baseUrl}/${pacienteId}`);
  }
}
