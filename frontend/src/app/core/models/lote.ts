/** Farmácia (#9) — lotes por unidade (ADR-0050, revista pela ADR-0057). */

export interface LoteRequestDto {
  medicamentoId: string;
  unidadeId: string;
  numeroLote: string;
  validade: string;
  quantidade: number;
  profissionalMatricula?: string;
}

/** O PATCH do lote só corrige número e validade — o saldo muda pelo livro (ADR-0057). */
export interface LoteAtualizacaoRequestDto {
  numeroLote: string;
  validade: string;
}

export interface LoteResponseDto {
  uuid: string;
  medicamentoUuid: string;
  medicamentoNome: string;
  unidadeUuid: string;
  unidadeNome: string;
  numeroLote: string;
  validade: string;
  /** Saldo atual, mantido pelo livro de movimentação. */
  quantidade: number;
}
