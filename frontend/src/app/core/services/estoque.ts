import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ItemDeSaldoDto,
  MaterialEstoqueDto,
  MaterialEstoqueRequestDto,
  MotivoPerdaEstoque,
  MovimentacaoEstoqueDto,
  OrigemEntradaEstoque,
  SaldoEstoqueDto,
  TipoMovimentacaoEstoque,
  CentralEstoqueDto,
  InventarioEstoqueDto,
  OperacaoEstoqueRequestDto,
  RequisicaoEstoqueDto,
  TransferenciaEstoqueDto,
  EmergenciaAbertaDto,
  ReservaEstoqueDto,
} from '../models/estoque';

/** Estoque e Almoxarifado (ADR-0177): catálogo, saldo por unidade, livro de movimentação e alertas. */
@Injectable({ providedIn: 'root' })
export class EstoqueService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/v1/estoque';

  materiais(ativos = false): Observable<MaterialEstoqueDto[]> {
    return this.http.get<MaterialEstoqueDto[]>(`${this.api}/material`, { params: ativos ? { ativos: 'true' } : {} });
  }

  criarMaterial(dto: MaterialEstoqueRequestDto): Observable<MaterialEstoqueDto> {
    return this.http.post<MaterialEstoqueDto>(`${this.api}/material`, dto);
  }

  atualizarMaterial(uuid: string, dto: MaterialEstoqueRequestDto): Observable<MaterialEstoqueDto> {
    return this.http.put<MaterialEstoqueDto>(`${this.api}/material/${uuid}`, dto);
  }

  /** O almoxarife conferiu o consumo sem saldo (ADR-0183). */
  conferirConsumoSemSaldo(uuid: string): Observable<void> {
    return this.http.post<void>(`${this.api}/consumo-sem-saldo/${uuid}/conferencia`, {});
  }

  saldo(unidadeId: string): Observable<SaldoEstoqueDto> {
    return this.http.get<SaldoEstoqueDto>(`${this.api}/saldo`, { params: { unidadeId } });
  }

  movimentacoes(unidadeId: string, tipo?: TipoMovimentacaoEstoque | '', dias = 30): Observable<MovimentacaoEstoqueDto[]> {
    const params: Record<string, string> = { unidadeId, dias: String(dias) };
    if (tipo) params['tipo'] = tipo;
    return this.http.get<MovimentacaoEstoqueDto[]>(`${this.api}/movimentacao`, { params });
  }

  definirMinimo(unidadeId: string, materialId: string, quantidadeMinima: number): Observable<ItemDeSaldoDto> {
    return this.http.put<ItemDeSaldoDto>(`${this.api}/minimo`, { unidadeId, materialId, quantidadeMinima });
  }

  entrada(dto: { unidadeId: string; materialId: string; quantidade: number; origem: OrigemEntradaEstoque; numeroLote?: string | null; validade?: string | null;
    custoUnitario?: number | null; documento?: string | null; fornecedor?: string | null }): Observable<MovimentacaoEstoqueDto> {
    return this.http.post<MovimentacaoEstoqueDto>(`${this.api}/entrada`, dto);
  }

  saida(dto: { unidadeId: string; materialId: string; quantidade: number; loteId?: string | null; setorId?: string | null; justificativa?: string | null;
    emergenciaId?: string | null }): Observable<MovimentacaoEstoqueDto[]> {
    return this.http.post<MovimentacaoEstoqueDto[]>(`${this.api}/saida`, dto);
  }

  perda(dto: { loteId: string; quantidade: number; motivo: MotivoPerdaEstoque; justificativa?: string | null }): Observable<MovimentacaoEstoqueDto> {
    return this.http.post<MovimentacaoEstoqueDto>(`${this.api}/perda`, dto);
  }

  ajuste(dto: { loteId: string; quantidadeContada: number; justificativa: string }): Observable<MovimentacaoEstoqueDto> {
    return this.http.post<MovimentacaoEstoqueDto>(`${this.api}/ajuste`, dto);
  }

  // ── Central, requisição, transferência e inventário (ADR-0189) ──

  // ── Emergências (ADR-0191) ──

  emergenciasAbertas(): Observable<EmergenciaAbertaDto[]> {
    return this.http.get<EmergenciaAbertaDto[]>(`${this.api}/emergencias`);
  }

  reservas(filtro: { unidadeId?: string; emergenciaId?: string }): Observable<ReservaEstoqueDto[]> {
    let params = new HttpParams();
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.emergenciaId) params = params.set('emergenciaId', filtro.emergenciaId);
    return this.http.get<ReservaEstoqueDto[]>(`${this.api}/reserva`, { params });
  }

  reservar(dto: { emergenciaId: string; unidadeId: string; materialId: string; quantidade: number; justificativa: string }): Observable<ReservaEstoqueDto> {
    return this.http.post<ReservaEstoqueDto>(`${this.api}/reserva`, dto);
  }

  liberarReserva(uuid: string, motivo: string): Observable<ReservaEstoqueDto> {
    return this.http.post<ReservaEstoqueDto>(`${this.api}/reserva/${uuid}/liberacao`, { motivo });
  }

  centrais(): Observable<CentralEstoqueDto[]> {
    return this.http.get<CentralEstoqueDto[]>(`${this.api}/centrais`);
  }

  requisicoes(filtro: { unidadeId?: string; centralId?: string }): Observable<RequisicaoEstoqueDto[]> {
    let params = new HttpParams();
    if (filtro.unidadeId) params = params.set('unidadeId', filtro.unidadeId);
    if (filtro.centralId) params = params.set('centralId', filtro.centralId);
    return this.http.get<RequisicaoEstoqueDto[]>(`${this.api}/requisicao`, { params });
  }

  requisicao(uuid: string): Observable<RequisicaoEstoqueDto> {
    return this.http.get<RequisicaoEstoqueDto>(`${this.api}/requisicao/${uuid}`);
  }

  criarRequisicao(dto: OperacaoEstoqueRequestDto): Observable<RequisicaoEstoqueDto> {
    return this.http.post<RequisicaoEstoqueDto>(`${this.api}/requisicao`, dto);
  }

  acaoRequisicao(uuid: string, acao: 'envio' | 'cancelamento' | 'negacao' | 'atendimento', dto: OperacaoEstoqueRequestDto): Observable<RequisicaoEstoqueDto> {
    return this.http.post<RequisicaoEstoqueDto>(`${this.api}/requisicao/${uuid}/${acao}`, dto);
  }

  transferencias(unidadeId: string): Observable<TransferenciaEstoqueDto[]> {
    return this.http.get<TransferenciaEstoqueDto[]>(`${this.api}/transferencia`, { params: { unidadeId } });
  }

  transferir(dto: OperacaoEstoqueRequestDto): Observable<TransferenciaEstoqueDto> {
    return this.http.post<TransferenciaEstoqueDto>(`${this.api}/transferencia`, dto);
  }

  receber(uuid: string, dto: OperacaoEstoqueRequestDto): Observable<TransferenciaEstoqueDto> {
    return this.http.post<TransferenciaEstoqueDto>(`${this.api}/transferencia/${uuid}/recebimento`, dto);
  }

  inventarios(unidadeId: string): Observable<InventarioEstoqueDto[]> {
    return this.http.get<InventarioEstoqueDto[]>(`${this.api}/inventario`, { params: { unidadeId } });
  }

  inventario(uuid: string): Observable<InventarioEstoqueDto> {
    return this.http.get<InventarioEstoqueDto>(`${this.api}/inventario/${uuid}`);
  }

  abrirInventario(dto: OperacaoEstoqueRequestDto): Observable<InventarioEstoqueDto> {
    return this.http.post<InventarioEstoqueDto>(`${this.api}/inventario`, dto);
  }

  acaoInventario(uuid: string, acao: 'contagem' | 'fechamento' | 'cancelamento', dto: OperacaoEstoqueRequestDto): Observable<InventarioEstoqueDto> {
    return this.http.post<InventarioEstoqueDto>(`${this.api}/inventario/${uuid}/${acao}`, dto);
  }
}
