/** Estoque e Almoxarifado (ADR-0177). */
export type CategoriaMaterial = 'MEDICO_HOSPITALAR' | 'EPI' | 'ODONTOLOGICO' | 'LABORATORIO' | 'LIMPEZA_HIGIENE' | 'EXPEDIENTE' | 'ROUPARIA' | 'OUTRO';
export type UnidadeMedidaMaterial = 'UNIDADE' | 'CAIXA' | 'PACOTE' | 'PAR' | 'ROLO' | 'LITRO' | 'FRASCO';
export type TipoMovimentacaoEstoque =
  | 'SALDO_INICIAL' | 'ENTRADA' | 'SAIDA_CONSUMO' | 'PERDA' | 'AJUSTE_INVENTARIO' | 'TRANSFERENCIA_SAIDA' | 'TRANSFERENCIA_ENTRADA' | 'TRANSFERENCIA_ESTORNO';
export type OrigemEntradaEstoque = 'NOTA_FISCAL' | 'DOACAO' | 'SALDO_INICIAL' | 'OUTRA';
export type MotivoPerdaEstoque = 'VENCIMENTO' | 'AVARIA' | 'EXTRAVIO' | 'OUTRO';

export const CATEGORIAS_MATERIAL: Record<CategoriaMaterial, string> = {
  MEDICO_HOSPITALAR: 'Médico-hospitalar',
  EPI: 'EPI',
  ODONTOLOGICO: 'Odontológico',
  LABORATORIO: 'Laboratório',
  LIMPEZA_HIGIENE: 'Limpeza e higiene',
  EXPEDIENTE: 'Expediente',
  ROUPARIA: 'Rouparia',
  OUTRO: 'Outro',
};

export const UNIDADES_MEDIDA: Record<UnidadeMedidaMaterial, string> = {
  UNIDADE: 'unidade', CAIXA: 'caixa', PACOTE: 'pacote', PAR: 'par', ROLO: 'rolo', LITRO: 'litro', FRASCO: 'frasco',
};

export const TIPOS_MOVIMENTACAO: Record<TipoMovimentacaoEstoque, string> = {
  SALDO_INICIAL: 'Saldo inicial', ENTRADA: 'Entrada', SAIDA_CONSUMO: 'Saída por consumo', PERDA: 'Perda', AJUSTE_INVENTARIO: 'Ajuste de inventário',
  TRANSFERENCIA_SAIDA: 'Transferência (saída)', TRANSFERENCIA_ENTRADA: 'Transferência (entrada)', TRANSFERENCIA_ESTORNO: 'Transferência (estorno)',
};

export const ORIGENS_ENTRADA: Record<OrigemEntradaEstoque, string> = {
  NOTA_FISCAL: 'Nota fiscal', DOACAO: 'Doação', SALDO_INICIAL: 'Saldo inicial', OUTRA: 'Outra',
};

export const MOTIVOS_PERDA: Record<MotivoPerdaEstoque, string> = {
  VENCIMENTO: 'Vencimento', AVARIA: 'Avaria', EXTRAVIO: 'Extravio', OUTRO: 'Outro',
};

export interface MaterialEstoqueDto {
  uuid: string;
  codigo: string;
  nome: string;
  catmat?: string | null;
  categoria: CategoriaMaterial;
  unidadeMedida: UnidadeMedidaMaterial;
  controlaLote: boolean;
  descricao?: string | null;
  ativo: boolean;
  comMovimento: boolean;
}

export interface MaterialEstoqueRequestDto {
  codigo: string;
  nome: string;
  catmat?: string | null;
  categoria: CategoriaMaterial;
  unidadeMedida: UnidadeMedidaMaterial;
  controlaLote: boolean;
  descricao?: string | null;
  ativo: boolean;
}

export interface LoteMaterialDto {
  uuid: string;
  numeroLote?: string | null;
  validade?: string | null;
  quantidade: number;
  custoUnitario?: number | null;
  vencido: boolean;
  diasParaVencer?: number | null;
}

export interface ItemDeSaldoDto {
  materialId: string;
  codigo: string;
  nome: string;
  categoria: CategoriaMaterial;
  unidadeMedida: UnidadeMedidaMaterial;
  controlaLote: boolean;
  total: number;
  minimo?: number | null;
  abaixoDoMinimo: boolean;
  proximaValidade?: string | null;
  valor?: number | null;
  lotes: LoteMaterialDto[];
  /** Reservado para emergência (ADR-0191) e o que fica livre dentro da validade. */
  reservado: number;
  disponivel: number;
}

export interface VencimentoDto {
  loteId: string;
  materialId: string;
  materialNome: string;
  numeroLote?: string | null;
  validade: string;
  quantidade: number;
  diasParaVencer: number;
}

export interface SaldoEstoqueDto {
  unidadeId: string;
  unidadeNome: string;
  itens: ItemDeSaldoDto[];
  abaixoDoMinimo: number;
  vencendo: number;
  vencidosComSaldo: number;
  valorTotal: number;
  vencimentos: VencimentoDto[];
  /** Consumo lançado pelo sistema sem saldo, aguardando conferência (ADR-0183). */
  consumosSemSaldo?: ConsumoSemSaldoDto[];
}

export interface ConsumoSemSaldoDto {
  uuid: string;
  materialId: string;
  materialCodigo: string;
  materialNome: string;
  quantidadeFaltante: number;
  origem: string;
  profissionalNome?: string | null;
  ocorridoEm: string;
}

export interface MovimentacaoEstoqueDto {
  uuid: string;
  tipo: TipoMovimentacaoEstoque;
  materialId: string;
  materialCodigo: string;
  materialNome: string;
  unidadeId: string;
  unidadeNome: string;
  loteId: string;
  numeroLote?: string | null;
  validade?: string | null;
  quantidade: number;
  saldoApos: number;
  origem?: OrigemEntradaEstoque | null;
  documento?: string | null;
  fornecedor?: string | null;
  setorNome?: string | null;
  motivoPerda?: MotivoPerdaEstoque | null;
  justificativa?: string | null;
  profissionalNome?: string | null;
  registradoEm: string;
  /** O consumo extraordinário de uma emergência (ADR-0191). */
  emergenciaId?: string | null;
}

/** Estoque nas emergências (ADR-0191): a reserva e o consumo extraordinário. */
export type SituacaoReservaEstoque = 'ATIVA' | 'LIBERADA' | 'CONSUMIDA';

export const SITUACOES_RESERVA: Record<SituacaoReservaEstoque, { rotulo: string; classe: string }> = {
  ATIVA: { rotulo: 'Ativa', classe: 'warn' },
  LIBERADA: { rotulo: 'Liberada', classe: 'muted' },
  CONSUMIDA: { rotulo: 'Consumida', classe: 'ok' },
};

export interface ReservaEstoqueDto {
  uuid: string;
  emergenciaId: string;
  emergenciaNome: string;
  unidadeId: string;
  unidadeNome: string;
  materialId: string;
  materialCodigo: string;
  materialNome: string;
  unidadeMedida: UnidadeMedidaMaterial;
  quantidade: number;
  consumida: number;
  restante: number;
  situacao: SituacaoReservaEstoque;
  justificativa: string;
  criadaEm: string;
  liberadaEm?: string | null;
  motivoLiberacao?: string | null;
}

export interface ConsumoEmergenciaDto {
  unidadeId: string;
  unidadeNome: string;
  materialId: string;
  materialCodigo: string;
  materialNome: string;
  unidadeMedida: UnidadeMedidaMaterial;
  quantidade: number;
  valor?: number | null;
}

export interface EstoqueDaEmergenciaDto {
  reservas: ReservaEstoqueDto[];
  consumo: ConsumoEmergenciaDto[];
  quantidadeConsumida: number;
  valorConsumido: number;
}

export interface EmergenciaAbertaDto {
  uuid: string;
  nome: string;
  situacao: 'ATIVA' | 'EM_DESMOBILIZACAO';
}


/** Almoxarifado central, requisição, transferência e inventário (ADR-0189). */
export type SituacaoRequisicaoEstoque = 'RASCUNHO' | 'ENVIADA' | 'ATENDIDA' | 'RECEBIDA' | 'NEGADA' | 'CANCELADA';
export type PrioridadeRequisicaoEstoque = 'NORMAL' | 'URGENTE';
export type SituacaoTransferenciaEstoque = 'EM_TRANSITO' | 'RECEBIDA';
export type MotivoDivergenciaEstoque = 'FALTA' | 'AVARIA' | 'OUTRO';
export type SituacaoInventarioEstoque = 'ABERTO' | 'FECHADO' | 'CANCELADO';

export interface OperacaoEstoqueRequestDto {
  profissionalMatricula: string;
  unidadeId?: string;
  centralId?: string;
  destinoId?: string;
  prioridade?: PrioridadeRequisicaoEstoque;
  observacao?: string | null;
  motivo?: string | null;
  itens?: { materialId?: string; loteId?: string; itemId?: string; quantidade?: number; motivoDivergencia?: MotivoDivergenciaEstoque | null; observacao?: string | null }[];
}

export interface CentralEstoqueDto {
  unidadeId: string;
  nome: string;
}

export interface ItemRequisicaoEstoqueDto {
  uuid: string;
  materialId: string;
  materialCodigo: string;
  materialNome: string;
  unidadeMedida: UnidadeMedidaMaterial;
  quantidadePedida: number;
  quantidadeAtendida?: number | null;
  saldoNaUnidade: number;
  consumo30Dias: number;
  saldoNaCentral: number;
}

export interface RequisicaoEstoqueDto {
  uuid: string;
  unidadeId: string;
  unidadeNome: string;
  centralId: string;
  centralNome: string;
  prioridade: PrioridadeRequisicaoEstoque;
  situacao: SituacaoRequisicaoEstoque;
  parcial: boolean;
  observacao?: string | null;
  solicitanteNome: string;
  criadaEm: string;
  enviadaEm?: string | null;
  decididaEm?: string | null;
  atendenteNome?: string | null;
  motivo?: string | null;
  transferenciaId?: string | null;
  recebidaEm?: string | null;
  itens: ItemRequisicaoEstoqueDto[];
  /** A requisição urgente gerada pela emergência (ADR-0191). */
  emergenciaId?: string | null;
  emergenciaNome?: string | null;
}

export interface ItemTransferenciaEstoqueDto {
  uuid: string;
  materialId: string;
  materialCodigo: string;
  materialNome: string;
  loteOrigemId: string;
  numeroLote?: string | null;
  validade?: string | null;
  quantidadeEnviada: number;
  quantidadeRecebida?: number | null;
  motivoDivergencia?: MotivoDivergenciaEstoque | null;
  observacao?: string | null;
}

export interface TransferenciaEstoqueDto {
  uuid: string;
  origemId: string;
  origemNome: string;
  destinoId: string;
  destinoNome: string;
  requisicaoId?: string | null;
  situacao: SituacaoTransferenciaEstoque;
  observacao?: string | null;
  enviadaEm: string;
  enviadoPorNome: string;
  recebidaEm?: string | null;
  recebidoPorNome?: string | null;
  comDivergencia: boolean;
  itens: ItemTransferenciaEstoqueDto[];
}

export interface ItemInventarioEstoqueDto {
  uuid: string;
  loteId: string;
  materialId: string;
  materialCodigo: string;
  materialNome: string;
  numeroLote?: string | null;
  validade?: string | null;
  saldoSistema: number;
  quantidadeContada?: number | null;
  diferenca?: number | null;
  contadoPorNome?: string | null;
}

export interface InventarioEstoqueDto {
  uuid: string;
  unidadeId: string;
  unidadeNome: string;
  situacao: SituacaoInventarioEstoque;
  abertoEm: string;
  abertoPorNome: string;
  encerradoEm?: string | null;
  encerradoPorNome?: string | null;
  observacao?: string | null;
  lotes: number;
  contados: number;
  comDiferenca: number;
  itens: ItemInventarioEstoqueDto[];
}

export const SITUACOES_REQUISICAO: Record<SituacaoRequisicaoEstoque, { rotulo: string; classe: string }> = {
  RASCUNHO: { rotulo: 'Rascunho', classe: 'muted' },
  ENVIADA: { rotulo: 'Aguardando a central', classe: 'info' },
  ATENDIDA: { rotulo: 'Atendida, em trânsito', classe: 'purple' },
  RECEBIDA: { rotulo: 'Recebida', classe: 'ok' },
  NEGADA: { rotulo: 'Negada', classe: 'alert' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export const MOTIVOS_DIVERGENCIA: Record<MotivoDivergenciaEstoque, string> = { FALTA: 'Falta', AVARIA: 'Avaria', OUTRO: 'Outro' };

export const SITUACOES_INVENTARIO: Record<SituacaoInventarioEstoque, string> = { ABERTO: 'Aberto', FECHADO: 'Fechado', CANCELADO: 'Cancelado' };
