/** Farmácia (#9) — transferência entre unidades em duas etapas (ADRs 0059 e 0061). */

export type StatusTransferenciaFarmacia = 'EM_TRANSITO' | 'RECEBIDA' | 'RECEBIDA_COM_DIVERGENCIA' | 'CANCELADA';

export type MotivoDivergenciaTransferencia = 'AVARIA' | 'EXTRAVIO' | 'OUTRO';

/** Envio: sai do lote de origem e fica em trânsito. */
export interface TransferenciaFarmaciaRequestDto {
  loteOrigemId: string;
  unidadeDestinoId: string;
  quantidade: number;
  profissionalMatricula: string;
  observacao?: string;
}

/** Recebimento conferido por outro profissional; chegar menos exige motivo e justificativa. */
export interface RecebimentoTransferenciaRequestDto {
  quantidadeRecebida: number;
  profissionalMatricula: string;
  motivoDivergencia?: MotivoDivergenciaTransferencia;
  justificativaDivergencia?: string;
}

export interface CancelamentoTransferenciaRequestDto {
  profissionalMatricula: string;
  motivo: string;
}

export interface TransferenciaFarmaciaResponseDto {
  uuid: string;
  status: StatusTransferenciaFarmacia;
  medicamentoNome: string;
  numeroLote: string;
  validade: string;
  loteOrigemId: string;
  unidadeOrigemId: string;
  unidadeOrigemNome: string;
  unidadeDestinoId: string;
  unidadeDestinoNome: string;
  loteDestinoId?: string | null;

  quantidade: number;
  profissionalMatricula: string;
  profissionalNome: string;
  observacao?: string | null;
  registradoEm: string;
  registradoPorCpf?: string | null;

  quantidadeRecebida?: number | null;
  quantidadeDivergente?: number | null;
  motivoDivergencia?: MotivoDivergenciaTransferencia | null;
  justificativaDivergencia?: string | null;
  profissionalRecebimentoMatricula?: string | null;
  profissionalRecebimentoNome?: string | null;
  recebidoEm?: string | null;
  recebidoPorCpf?: string | null;

  motivoCancelamento?: string | null;
  profissionalCancelamentoMatricula?: string | null;
  profissionalCancelamentoNome?: string | null;
  canceladoEm?: string | null;
  canceladoPorCpf?: string | null;
}
