import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AdministracaoMedicamentoRequestDto, AdministracaoMedicamentoResponseDto } from '../models/administracao-medicamento';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class AdministracaoMedicamentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/administracao-medicamento';

  /** Da mais recente para a mais antiga, todas as versões marcadas. */
  listar(): Observable<AdministracaoMedicamentoResponseDto[]> {
    return this.http.get<AdministracaoMedicamentoResponseDto[]>(`${this.baseUrl}/`);
  }

  /** Administrado baixa o lote pelo livro da Farmácia; 422 quando o lote é de outra unidade, venceu ou não tem saldo. */
  registrar(dto: AdministracaoMedicamentoRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  /** Nova versão com motivo; a baixa anterior é estornada antes da nova (ADR-0062, ADR-0064). */
  retificar(uuid: string, dto: AdministracaoMedicamentoRequestDto & { motivoRetificacao: string }): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/retificacao`, dto);
  }
}
