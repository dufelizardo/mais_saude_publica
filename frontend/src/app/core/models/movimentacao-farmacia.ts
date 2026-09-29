/** Farmácia (#9) — livro de movimentação do estoque (ADR-0057). */

export type TipoMovimentacaoFarmacia = 'SALDO_INICIAL' | 'ENTRADA' | 'DISPENSACAO' | 'PERDA' | 'AJUSTE_INVENTARIO';

export type MotivoPerda = 'VENCIMENTO' | 'AVARIA' | 'EXTRAVIO' | 'OUTRO';

/** Só PERDA e AJUSTE_INVENTARIO são aceitos no POST; entrada e dispensação têm rotas próprias. */
export interface MovimentacaoFarmaciaRequestDto {
  loteId: string;
  tipo: 'PERDA' | 'AJUSTE_INVENTARIO';
  quantidade?: number;
  saldoContado?: number;
  motivoPerda?: MotivoPerda;
  justificativa?: string;
  profissionalMatricula: string;
}

export interface MovimentacaoFarmaciaResponseDto {
  uuid: string;
  loteId: string;
  numeroLote: string;
  medicamentoNome: string;
  tipo: TipoMovimentacaoFarmacia;
  /** Variação com sinal: positiva em entradas, negativa em saídas. */
  quantidade: number;
  saldoApos: number;
  motivoPerda?: MotivoPerda | null;
  justificativa?: string | null;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
  dispensacaoId?: string | null;
  registradoEm: string;
  registradoPorCpf?: string | null;
}
