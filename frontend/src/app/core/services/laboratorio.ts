import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SuccessResponseDto } from '../models/profissional';
import {
  ColetaExameRequestDto,
  EtapaTrabalho,
  ExameLaboratorialRequestDto,
  ExameLaboratorialResponseDto,
  ItemTrabalhoExameDto,
  MotivoRejeicaoAmostra,
  PedidoExameRequestDto,
  PedidoExameResponseDto,
  PedidoExameResumoDto,
  ResultadoExameRequestDto,
  RetificacaoResultadoExameRequestDto,
} from '../models/laboratorio';

/** Laboratório assistencial (ADR-0093): catálogo, pedidos, coleta, resultado, liberação e retificação. */
@Injectable({ providedIn: 'root' })
export class LaboratorioService {
  private readonly http = inject(HttpClient);
  private readonly exameUrl = '/api/v1/exame-laboratorial';
  private readonly pedidoUrl = '/api/v1/pedido-exame';

  // ── Catálogo ──

  listarExames(): Observable<ExameLaboratorialResponseDto[]> {
    return this.http.get<ExameLaboratorialResponseDto[]>(`${this.exameUrl}/`);
  }

  criarExame(dto: ExameLaboratorialRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.exameUrl}/`, dto);
  }

  atualizarExame(uuid: string, dto: ExameLaboratorialRequestDto): Observable<SuccessResponseDto> {
    return this.http.patch<SuccessResponseDto>(`${this.exameUrl}/${uuid}`, dto);
  }

  // ── Pedidos ──

  /** Sem valores; situação calculada. */
  listarPedidos(pacienteId?: string): Observable<PedidoExameResumoDto[]> {
    const params = pacienteId ? new HttpParams().set('pacienteId', pacienteId) : undefined;
    return this.http.get<PedidoExameResumoDto[]>(`${this.pedidoUrl}/`, { params });
  }

  /** Lista de trabalho por exame, urgente primeiro; sem a permissão da etapa, 403. */
  trabalho(etapa: EtapaTrabalho): Observable<ItemTrabalhoExameDto[]> {
    return this.http.get<ItemTrabalhoExameDto[]>(`${this.pedidoUrl}/trabalho`, { params: new HttpParams().set('etapa', etapa) });
  }

  /** Detalhe com resultados, amostras e eventos; leitura auditada. */
  buscarPedido(uuid: string): Observable<PedidoExameResponseDto> {
    return this.http.get<PedidoExameResponseDto>(`${this.pedidoUrl}/${uuid}`);
  }

  solicitar(dto: PedidoExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/`, dto);
  }

  coletar(pedidoId: string, dto: ColetaExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/${pedidoId}/coleta`, dto);
  }

  rejeitarAmostra(amostraId: string, profissionalMatricula: string, motivo: MotivoRejeicaoAmostra, observacao?: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/amostra/${amostraId}/rejeicao`, { profissionalMatricula, motivo, observacao });
  }

  registrarResultado(itemId: string, dto: ResultadoExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/resultado`, dto);
  }

  liberar(itemId: string, profissionalMatricula: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/liberacao`, { profissionalMatricula });
  }

  retificar(itemId: string, dto: RetificacaoResultadoExameRequestDto): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/retificacao`, dto);
  }

  cancelar(itemId: string, profissionalMatricula: string, motivo: string): Observable<SuccessResponseDto> {
    return this.http.post<SuccessResponseDto>(`${this.pedidoUrl}/item/${itemId}/cancelamento`, { profissionalMatricula, motivo });
  }
}
