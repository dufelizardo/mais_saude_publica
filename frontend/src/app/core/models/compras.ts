/** Compras, fatia CP1 (ADR-0212): fornecedores, solicitações de compra e pesquisa de preços. */

export type TipoCertidaoFornecedor = 'FEDERAL' | 'FGTS' | 'TRABALHISTA' | 'ESTADUAL' | 'MUNICIPAL';
export type TipoSancaoFornecedor = 'ADVERTENCIA' | 'MULTA' | 'IMPEDIMENTO' | 'INIDONEIDADE';
export type PrioridadeCompra = 'NORMAL' | 'URGENTE';
export type SituacaoSolicitacaoCompra = 'RASCUNHO' | 'ENVIADA' | 'DEVOLVIDA' | 'APROVADA' | 'REJEITADA' | 'EM_COTACAO' | 'COTADA' | 'CANCELADA';
export type TipoItemCompra = 'MATERIAL' | 'MEDICAMENTO' | 'SERVICO';
export type FonteCotacao = 'FORNECEDOR' | 'CONTRATACAO_PUBLICA' | 'SITE' | 'OUTRA';
export type DecisaoSolicitacaoCompra = 'APROVAR' | 'DEVOLVER' | 'REJEITAR';

export const CERTIDOES: Record<TipoCertidaoFornecedor, string> = {
  FEDERAL: 'Federal e previdência',
  FGTS: 'FGTS',
  TRABALHISTA: 'Trabalhista',
  ESTADUAL: 'Estadual',
  MUNICIPAL: 'Municipal',
};

export const SANCOES: Record<TipoSancaoFornecedor, string> = {
  ADVERTENCIA: 'Advertência',
  MULTA: 'Multa',
  IMPEDIMENTO: 'Impedimento de licitar e contratar',
  INIDONEIDADE: 'Declaração de inidoneidade',
};

export const SITUACOES_SOLICITACAO: Record<SituacaoSolicitacaoCompra, string> = {
  RASCUNHO: 'Rascunho',
  ENVIADA: 'Enviada',
  DEVOLVIDA: 'Devolvida',
  APROVADA: 'Aprovada',
  REJEITADA: 'Rejeitada',
  EM_COTACAO: 'Em cotação',
  COTADA: 'Cotada',
  CANCELADA: 'Cancelada',
};

export const TIPOS_ITEM: Record<TipoItemCompra, string> = {
  MATERIAL: 'Material',
  MEDICAMENTO: 'Medicamento',
  SERVICO: 'Serviço',
};

export const FONTES_COTACAO: Record<FonteCotacao, string> = {
  FORNECEDOR: 'Proposta de fornecedor',
  CONTRATACAO_PUBLICA: 'Contratação pública similar',
  SITE: 'Site',
  OUTRA: 'Outra',
};

export interface CertidaoDto {
  uuid: string;
  tipo: TipoCertidaoFornecedor;
  numero: string | null;
  emitidaEm: string;
  validaAte: string;
  vencida: boolean;
}

export interface SancaoDto {
  uuid: string;
  tipo: TipoSancaoFornecedor;
  inicio: string;
  fim: string | null;
  valor: number | null;
  motivo: string;
  processo: string | null;
  vigente: boolean;
  revogadaEm: string | null;
  motivoRevogacao: string | null;
}

export interface FornecedorDto {
  uuid: string;
  documento: string;
  pessoaJuridica: boolean;
  razaoSocial: string;
  nomeFantasia: string | null;
  email: string | null;
  telefone: string | null;
  endereco: string | null;
  ativo: boolean;
  impedido: boolean;
  regular: boolean;
  certidoesPendentes: TipoCertidaoFornecedor[];
  certidoes: CertidaoDto[];
  sancoes: SancaoDto[];
}

export interface CotacaoDto {
  uuid: string;
  fonte: FonteCotacao;
  fornecedorId: string | null;
  fornecedorNome: string | null;
  referencia: string | null;
  precoUnitario: number;
  cotadoEm: string;
}

export interface ItemCompraDto {
  uuid: string;
  ordem: number;
  tipo: TipoItemCompra;
  materialId: string | null;
  medicamentoId: string | null;
  descricao: string;
  unidadeMedida: string;
  quantidade: number;
  cotacoes: CotacaoDto[];
  mediana: number | null;
  precoReferencia: number | null;
  valorEstimado: number | null;
  justificativaCotacao: string | null;
}

export interface SolicitacaoCompraDto {
  uuid: string;
  numero: string;
  unidadeId: string;
  unidadeNome: string;
  setorId: string | null;
  setorNome: string | null;
  justificativa: string;
  prioridade: PrioridadeCompra;
  situacao: SituacaoSolicitacaoCompra;
  motivo: string | null;
  itens: ItemCompraDto[];
  valorEstimado: number | null;
  registradoEm: string;
  enviadaEm: string | null;
  analisadaEm: string | null;
  pesquisaConcluidaEm: string | null;
}

export interface SugestaoCompraDto {
  materialId: string;
  codigo: string;
  nome: string;
  unidadeMedida: string;
  saldo: number;
  minimo: number;
  quantidadeSugerida: number;
}

export interface ItemCompraRequest {
  tipo: TipoItemCompra;
  materialId: string | null;
  medicamentoId: string | null;
  descricao: string | null;
  unidadeMedida: string | null;
  quantidade: number;
}

export interface SolicitacaoCompraRequest {
  unidadeId: string;
  setorId: string | null;
  justificativa: string;
  prioridade: PrioridadeCompra;
  itens: ItemCompraRequest[];
}
