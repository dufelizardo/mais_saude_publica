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
