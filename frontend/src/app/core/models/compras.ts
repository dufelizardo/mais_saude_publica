/** Compras, fatia CP1 (ADR-0212): fornecedores, solicitações de compra e pesquisa de preços. */

export type TipoCertidaoFornecedor = 'FEDERAL' | 'FGTS' | 'TRABALHISTA' | 'ESTADUAL' | 'MUNICIPAL';
export type TipoSancaoFornecedor = 'ADVERTENCIA' | 'MULTA' | 'IMPEDIMENTO' | 'INIDONEIDADE';
export type PrioridadeCompra = 'NORMAL' | 'URGENTE';
export type SituacaoSolicitacaoCompra = 'RASCUNHO' | 'ENVIADA' | 'DEVOLVIDA' | 'APROVADA' | 'REJEITADA' | 'EM_COTACAO' | 'COTADA' | 'CANCELADA';
export type TipoItemCompra = 'MATERIAL' | 'MEDICAMENTO' | 'SERVICO' | 'BEM_PERMANENTE';
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
  BEM_PERMANENTE: 'Bem permanente',
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
  contratacaoId: string | null;
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

// ── CP2: contratação, atas e contratos (ADR-0213) ──

export type ModalidadeContratacao = 'PREGAO_ELETRONICO' | 'CONCORRENCIA' | 'DISPENSA' | 'INEXIGIBILIDADE' | 'ADESAO_ATA';
export type SituacaoContratacao = 'EM_ANDAMENTO' | 'HOMOLOGADA' | 'CANCELADA';
export type ResultadoItemContratacao = 'PENDENTE' | 'ADJUDICADO' | 'FRACASSADO' | 'DESERTO';
export type TipoInstrumentoContratual = 'ATA_REGISTRO_PRECOS' | 'CONTRATO';
export type TipoAditivoContratual = 'PRAZO' | 'ACRESCIMO' | 'SUPRESSAO' | 'REEQUILIBRIO';

export const MODALIDADES: Record<ModalidadeContratacao, string> = {
  PREGAO_ELETRONICO: 'Pregão eletrônico',
  CONCORRENCIA: 'Concorrência',
  DISPENSA: 'Dispensa',
  INEXIGIBILIDADE: 'Inexigibilidade',
  ADESAO_ATA: 'Adesão a ata',
};

export const SITUACOES_CONTRATACAO: Record<SituacaoContratacao, string> = {
  EM_ANDAMENTO: 'Em andamento',
  HOMOLOGADA: 'Homologada',
  CANCELADA: 'Cancelada',
};

export const RESULTADOS_ITEM: Record<ResultadoItemContratacao, string> = {
  PENDENTE: 'Pendente',
  ADJUDICADO: 'Adjudicado',
  FRACASSADO: 'Fracassado',
  DESERTO: 'Deserto',
};

export const TIPOS_INSTRUMENTO: Record<TipoInstrumentoContratual, string> = {
  ATA_REGISTRO_PRECOS: 'Ata de registro de preços',
  CONTRATO: 'Contrato',
};

export const TIPOS_ADITIVO: Record<TipoAditivoContratual, string> = {
  PRAZO: 'Prorrogação de prazo',
  ACRESCIMO: 'Acréscimo de quantidade',
  SUPRESSAO: 'Supressão de quantidade',
  REEQUILIBRIO: 'Reequilíbrio de preço',
};

export interface ItemContratacaoDto {
  uuid: string;
  ordem: number;
  tipo: TipoItemCompra;
  descricao: string;
  unidadeMedida: string;
  quantidade: number;
  precoReferencia: number | null;
  resultado: ResultadoItemContratacao;
  fornecedorId: string | null;
  fornecedorNome: string | null;
  precoUnitario: number | null;
  justificativaPreco: string | null;
  valor: number | null;
}

export interface ContratacaoDto {
  uuid: string;
  numeroProcesso: string;
  modalidade: ModalidadeContratacao;
  fundamentoLegal: string | null;
  ataOrigem: string | null;
  objeto: string;
  situacao: SituacaoContratacao;
  homologadaEm: string | null;
  motivoCancelamento: string | null;
  solicitacoes: { uuid: string; numero: string; unidadeNome: string }[];
  itens: ItemContratacaoDto[];
  valorReferencia: number;
  valorAdjudicado: number;
  instrumentos: { uuid: string; tipo: TipoInstrumentoContratual; numero: string; fornecedorNome: string }[];
}

export interface ItemInstrumentoDto {
  uuid: string;
  tipo: TipoItemCompra;
  materialId: string | null;
  medicamentoId: string | null;
  descricao: string;
  unidadeMedida: string;
  quantidadeInicial: number;
  quantidade: number;
  saldo: number;
  precoUnitario: number;
  saldoBaixo: boolean;
}

export interface AditivoDto {
  uuid: string;
  numero: number;
  tipo: TipoAditivoContratual;
  fimAnterior: string | null;
  novoFim: string | null;
  itemId: string | null;
  itemDescricao: string | null;
  quantidade: number | null;
  precoAnterior: number | null;
  novoPreco: number | null;
  justificativa: string;
  assinadoEm: string;
}

export interface InstrumentoDto {
  uuid: string;
  tipo: TipoInstrumentoContratual;
  numero: string;
  contratacaoId: string;
  numeroProcesso: string;
  fornecedorId: string;
  fornecedorNome: string;
  inicio: string;
  fim: string;
  servicoContinuo: boolean;
  vigente: boolean;
  diasParaVencer: number;
  valorInicial: number;
  valorAtual: number;
  percentualAcrescido: number;
  percentualSuprimido: number;
  gestorMatricula: string;
  gestorNome: string;
  fiscalMatricula: string;
  fiscalNome: string;
  itens: ItemInstrumentoDto[];
  aditivos: AditivoDto[];
  alertas: string[];
  ordensEmAberto: number;
}

// ── CP3: ordens de fornecimento, recebimento, ocorrências e painel (ADR-0214) ──

export type SituacaoOrdemFornecimento = 'EMITIDA' | 'PARCIAL' | 'RECEBIDA' | 'CANCELADA';
export type SituacaoRecebimentoCompra = 'PROVISORIO' | 'DEFINITIVO';
export type TipoOcorrenciaFornecedor = 'ATRASO' | 'ENTREGA_PARCIAL' | 'FORA_ESPECIFICACAO' | 'OUTRA';
export type SituacaoOcorrenciaFornecedor = 'ABERTA' | 'ENCERRADA' | 'SANCIONADA';

export const SITUACOES_ORDEM: Record<SituacaoOrdemFornecimento, string> = {
  EMITIDA: 'Emitida',
  PARCIAL: 'Parcialmente recebida',
  RECEBIDA: 'Recebida',
  CANCELADA: 'Cancelada',
};

export const TIPOS_OCORRENCIA: Record<TipoOcorrenciaFornecedor, string> = {
  ATRASO: 'Atraso',
  ENTREGA_PARCIAL: 'Entrega parcial',
  FORA_ESPECIFICACAO: 'Fora da especificação',
  OUTRA: 'Outra',
};

export const SITUACOES_OCORRENCIA: Record<SituacaoOcorrenciaFornecedor, string> = {
  ABERTA: 'Aberta',
  ENCERRADA: 'Encerrada',
  SANCIONADA: 'Sancionada',
};

export interface ItemOrdemDto {
  uuid: string;
  itemInstrumentoId: string;
  tipo: TipoItemCompra;
  descricao: string;
  unidadeMedida: string;
  quantidade: number;
  aceita: number;
  emRecebimento: number;
  pendente: number;
  precoUnitario: number;
  pedeLote: boolean;
}

export interface ItemRecebimentoDto {
  uuid: string;
  itemOrdemId: string;
  descricao: string;
  entregue: number;
  aceita: number | null;
  recusada: number | null;
  motivoRecusa: string | null;
  numeroLote: string | null;
  validade: string | null;
}

export interface RecebimentoDto {
  uuid: string;
  notaFiscal: string;
  notaEmitidaEm: string;
  recebidoEm: string;
  situacao: SituacaoRecebimentoCompra;
  definitivoEm: string | null;
  observacao: string | null;
  itens: ItemRecebimentoDto[];
}

export interface OcorrenciaDto {
  uuid: string;
  fornecedorId: string;
  fornecedorNome: string;
  ordemId: string | null;
  ordemNumero: string | null;
  tipo: TipoOcorrenciaFornecedor;
  descricao: string;
  registradaEm: string;
  situacao: SituacaoOcorrenciaFornecedor;
  resposta: string | null;
  encerradaEm: string | null;
  sancaoId: string | null;
}

export interface OrdemDto {
  uuid: string;
  numero: string;
  instrumentoId: string;
  instrumentoNumero: string;
  fornecedorId: string;
  fornecedorNome: string;
  unidadeId: string;
  unidadeNome: string;
  empenho: string | null;
  emitidaEm: string;
  prazoEm: string;
  atrasada: boolean;
  diasDeAtraso: number;
  situacao: SituacaoOrdemFornecimento;
  motivoCancelamento: string | null;
  valor: number;
  itens: ItemOrdemDto[];
  recebimentos: RecebimentoDto[];
  ocorrencias: OcorrenciaDto[];
  /** O empenho do Financeiro (ADR-0218), quando a ordem foi emitida com ele. */
  empenhoId: string | null;
}

export interface PainelComprasDto {
  inicio: string;
  fim: string;
  solicitacoesPorSituacao: Partial<Record<SituacaoSolicitacaoCompra, number>>;
  valorContratado: number;
  ordensEmAberto: number;
  ordensEmAtraso: number;
  contratosVencendo: number;
  itensComSaldoBaixo: number;
  ocorrenciasAbertas: number;
  atrasadas: { ordemId: string; numero: string; fornecedorNome: string; unidadeNome: string; prazoEm: string; diasDeAtraso: number }[];
  fornecedores: { fornecedorId: string; fornecedorNome: string; ordens: number; entregas: number; percentualNoPrazo: number | null;
    prazoMedioDias: number | null; quantidadeRecusada: number; ocorrencias: number }[];
}
