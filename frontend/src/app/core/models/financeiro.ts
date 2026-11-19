/** Financeiro, fatia F1 (ADR-0218): a dotação e o empenho do Fundo Municipal de Saúde, espelhados do SIAFIC. */

export type FonteRecursoSaude = 'RECURSOS_PROPRIOS' | 'BLOCO_MANUTENCAO' | 'BLOCO_ESTRUTURACAO' | 'TRANSFERENCIA_ESTADUAL' | 'OUTRAS';
export type TipoAlteracaoDotacao = 'SUPLEMENTACAO' | 'ANULACAO';
export type TipoEmpenho = 'ORDINARIO' | 'ESTIMATIVO' | 'GLOBAL';
export type TipoMovimentoEmpenho = 'REFORCO' | 'ANULACAO';

export const FONTES: Record<FonteRecursoSaude, string> = {
  RECURSOS_PROPRIOS: 'Recursos próprios',
  BLOCO_MANUTENCAO: 'Bloco de Manutenção (SUS)',
  BLOCO_ESTRUTURACAO: 'Bloco de Estruturação (SUS)',
  TRANSFERENCIA_ESTADUAL: 'Transferência estadual',
  OUTRAS: 'Outras',
};
export const TIPOS_EMPENHO: Record<TipoEmpenho, string> = { ORDINARIO: 'Ordinário', ESTIMATIVO: 'Estimativo', GLOBAL: 'Global' };
export const TIPOS_ALTERACAO: Record<TipoAlteracaoDotacao, string> = { SUPLEMENTACAO: 'Suplementação', ANULACAO: 'Anulação' };
export const TIPOS_MOVIMENTO: Record<TipoMovimentoEmpenho, string> = { REFORCO: 'Reforço', ANULACAO: 'Anulação' };

export interface DotacaoDto {
  uuid: string;
  exercicio: number;
  programaTrabalho: string;
  descricao: string;
  naturezaDespesa: string;
  fonte: FonteRecursoSaude;
  fonteCodigo: string | null;
  valorInicial: number;
  suplementado: number;
  anulado: number;
  autorizado: number;
  empenhado: number;
  disponivel: number;
  alteracoes: { uuid: string; tipo: TipoAlteracaoDotacao; valor: number; ato: string; data: string; motivo: string | null }[];
}

export interface EmpenhoDto {
  uuid: string;
  numero: string;
  exercicio: number;
  tipo: TipoEmpenho;
  dotacaoId: string;
  dotacaoRotulo: string;
  fornecedorId: string;
  fornecedorNome: string;
  instrumentoId: string | null;
  instrumentoNumero: string | null;
  valorInicial: number;
  reforcado: number;
  anulado: number;
  valorAtual: number;
  comprometido: number;
  saldo: number;
  data: string;
  historico: string | null;
  movimentos: { uuid: string; tipo: TipoMovimentoEmpenho; valor: number; data: string; motivo: string }[];
  ordens: { uuid: string; numero: string; situacao: string; valor: number }[];
}

// ── Liquidação, pagamento, diárias do TFD e execução, fatia F2 (ADR-0219) ──

export type OrigemLiquidacao = 'RECEBIMENTO' | 'ORDEM_SERVICO' | 'DIARIA_TFD' | 'AVULSA';
export type BeneficiarioDiariaTfd = 'PACIENTE' | 'ACOMPANHANTE';

export const ORIGENS_LIQUIDACAO: Record<OrigemLiquidacao, string> = {
  RECEBIMENTO: 'Recebimento de compra',
  ORDEM_SERVICO: 'Ordem de serviço',
  DIARIA_TFD: 'Diária do TFD',
  AVULSA: 'Avulsa',
};

export interface PendenciaLiquidacaoDto {
  origem: OrigemLiquidacao;
  origemId: string;
  descricao: string;
  fornecedorId: string;
  fornecedorNome: string;
  instrumentoId: string | null;
  instrumentoNumero: string | null;
  empenhoId: string | null;
  empenhoNumero: string | null;
  valor: number;
  atestadoEm: string | null;
  unidadeNome: string | null;
}

export interface LiquidacaoDto {
  uuid: string;
  origem: OrigemLiquidacao;
  origemId: string | null;
  descricao: string;
  empenhoId: string;
  empenhoNumero: string;
  fornecedorNome: string;
  fonte: FonteRecursoSaude;
  notaFiscal: string | null;
  notaEmitidaEm: string | null;
  valorBruto: number;
  iss: number;
  irrf: number;
  inss: number;
  outrasRetencoes: number;
  valorLiquido: number;
  liquidadaEm: string;
  vencimento: string;
  vencida: boolean;
  paga: boolean;
  pagaEm: string | null;
  ordemBancaria: string | null;
  justificativa: string | null;
  justificativaForaDeOrdem: string | null;
}

export interface DiariaTfdDto {
  uuid: string;
  solicitacaoId: string;
  pacienteNome: string;
  destino: string | null;
  beneficiario: BeneficiarioDiariaTfd;
  beneficiarioNome: string;
  credorId: string;
  credorNome: string;
  dias: number;
  valorDiaria: number;
  valorTotal: number;
  concedidaEm: string;
  liquidada: boolean;
}

export interface RestoAPagarDto {
  empenhoId: string;
  numero: string;
  exercicio: number;
  fornecedorNome: string;
  processado: number;
  naoProcessado: number;
}

export interface ExecucaoDto {
  exercicio: number;
  dotacoes: { dotacaoId: string; rotulo: string; fonte: FonteRecursoSaude; autorizado: number; empenhado: number; liquidado: number; pago: number }[];
  fontes: { fonte: FonteRecursoSaude; autorizado: number; empenhado: number; liquidado: number; pago: number }[];
  aLiquidarQuantidade: number;
  aLiquidarValor: number;
  vencidasQuantidade: number;
  vencidasValor: number;
  prazoMedioPagamentoDias: number | null;
}
