import { HttpClient } from '@angular/common/http';
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

  saida(dto: { unidadeId: string; materialId: string; quantidade: number; loteId?: string | null; setorId?: string | null; justificativa?: string | null }): Observable<MovimentacaoEstoqueDto[]> {
    return this.http.post<MovimentacaoEstoqueDto[]>(`${this.api}/saida`, dto);
  }

  perda(dto: { loteId: string; quantidade: number; motivo: MotivoPerdaEstoque; justificativa?: string | null }): Observable<MovimentacaoEstoqueDto> {
    return this.http.post<MovimentacaoEstoqueDto>(`${this.api}/perda`, dto);
  }

  ajuste(dto: { loteId: string; quantidadeContada: number; justificativa: string }): Observable<MovimentacaoEstoqueDto> {
    return this.http.post<MovimentacaoEstoqueDto>(`${this.api}/ajuste`, dto);
  }
}
