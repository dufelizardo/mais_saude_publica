/** Estoque e Almoxarifado (ADR-0177). */
export type CategoriaMaterial = 'MEDICO_HOSPITALAR' | 'EPI' | 'ODONTOLOGICO' | 'LABORATORIO' | 'LIMPEZA_HIGIENE' | 'EXPEDIENTE' | 'ROUPARIA' | 'OUTRO';
export type UnidadeMedidaMaterial = 'UNIDADE' | 'CAIXA' | 'PACOTE' | 'PAR' | 'ROLO' | 'LITRO' | 'FRASCO';
export type TipoMovimentacaoEstoque = 'SALDO_INICIAL' | 'ENTRADA' | 'SAIDA_CONSUMO' | 'PERDA' | 'AJUSTE_INVENTARIO';
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
}
