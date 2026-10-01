import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CancelamentoTransferenciaRequestDto,
  RecebimentoTransferenciaRequestDto,
  TransferenciaFarmaciaRequestDto,
  TransferenciaFarmaciaResponseDto,
} from '../models/transferencia-farmacia';
import { SuccessResponseDto } from '../models/profissional';

@Injectable({ providedIn: 'root' })
export class TransferenciaFarmaciaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/transferencia-farmacia';

  /** Da mais recente para a mais antiga. */
  listar(): Observable<TransferenciaFarmaciaResponseDto[]> {
    return this.http.get<TransferenciaFarmaciaResponseDto[]>(`${this.baseUrl}/`);
  }

  /** Envio: 422 quando o saldo não basta ou o lote está vencido. */
  enviar(dto: TransferenciaFarmaciaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/`, dto);
  }

  /** 422 quando quem confere é quem enviou, ou a transferência não está em trânsito. */
  receber(uuid: string, dto: RecebimentoTransferenciaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/recebimento`, dto);
  }

  cancelar(uuid: string, dto: CancelamentoTransferenciaRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.baseUrl}/${uuid}/cancelamento`, dto);
  }
}
