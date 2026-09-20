import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { UnidadeSaudeResponseDto } from '../models/unidade-saude';

@Injectable({ providedIn: 'root' })
export class UnidadeSaudeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/unidade-saude';

  listar(): Observable<UnidadeSaudeResponseDto[]> {
    return this.http.get<UnidadeSaudeResponseDto[]>(`${this.baseUrl}/`);
  }
}
