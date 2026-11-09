/** Laboratório assistencial (#10) — ADRs 0093, 0094 e 0180 (faixas por sexo e idade, críticos e evolução). */

import { Sexo } from './paciente';

export type MaterialExame = 'SANGUE' | 'URINA' | 'FEZES' | 'SECRECAO' | 'OUTRO';
export type TipoResultadoExame = 'NUMERICO' | 'TEXTO';
export type PrioridadeExame = 'ROTINA' | 'URGENTE';
export type StatusItemExame = 'SOLICITADO' | 'COLETADO' | 'RESULTADO_REGISTRADO' | 'LIBERADO' | 'CANCELADO';
export type MotivoRejeicaoAmostra = 'HEMOLISADA' | 'INSUFICIENTE' | 'COAGULADA' | 'IDENTIFICACAO_INCORRETA' | 'OUTRO';
export type InterpretacaoResultado = 'NORMAL' | 'ACIMA' | 'ABAIXO' | 'CRITICO_ALTO' | 'CRITICO_BAIXO';
export type UnidadeIdade = 'DIAS' | 'MESES' | 'ANOS';
export type TipoEventoExame =
  | 'SOLICITACAO' | 'COLETA' | 'REJEICAO_AMOSTRA' | 'RESULTADO' | 'LIBERACAO' | 'RETIFICACAO' | 'CANCELAMENTO'
  | 'COMUNICACAO_CRITICO' | 'CIENCIA' | 'ENTREGA';

/** Ciência, comunicação de crítico e entrega (ADR-0181). */
export type MeioComunicacaoCritico = 'TELEFONE' | 'PRESENCIAL' | 'MENSAGEM_NO_SISTEMA';
export type FormaEntregaResultado = 'IMPRESSO_NA_UNIDADE' | 'NA_CONSULTA';
export type SituacaoPedidoExame = 'AGUARDANDO_COLETA' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';
export type EtapaTrabalho = 'PARA_COLETAR' | 'EM_ANALISE' | 'PARA_LIBERAR';

export interface ExameLaboratorialRequestDto {
  nome: string;
  material: MaterialExame;
  tipoResultado: TipoResultadoExame;
  unidadeMedida?: string;
  referenciaMinima?: number | null;
  referenciaMaxima?: number | null;
  criticoMinimo?: number | null;
  criticoMaximo?: number | null;
  referenciaTexto?: string;
  preparo?: string;
  prazoDias?: number | null;
  ativo?: boolean;
}

export interface ExameLaboratorialResponseDto {
  uuid: string;
  nome: string;
  material: MaterialExame;
  tipoResultado: TipoResultadoExame;
  unidadeMedida?: string | null;
  referenciaMinima?: number | null;
  referenciaMaxima?: number | null;
  criticoMinimo?: number | null;
  criticoMaximo?: number | null;
  referenciaTexto?: string | null;
  preparo?: string | null;
  prazoDias?: number | null;
  ativo: boolean;
  /** Faixas por sexo e idade (ADR-0180), da mais nova para a mais velha. */
  faixas?: FaixaReferenciaExameDto[];
}

/** Faixa de referência por sexo e idade (ADR-0180). Sem sexo: os dois. Sem idade máxima: sem limite. */
export interface FaixaReferenciaExameRequestDto {
  sexo?: Sexo | null;
  idadeMinima: number;
  unidadeIdadeMinima: UnidadeIdade;
  idadeMaxima?: number | null;
  unidadeIdadeMaxima?: UnidadeIdade | null;
  referenciaMinima?: number | null;
  referenciaMaxima?: number | null;
  criticoMinimo?: number | null;
  criticoMaximo?: number | null;
  descricao?: string;
}

export interface FaixaReferenciaExameDto extends FaixaReferenciaExameRequestDto {
  uuid: string;
  /** "Feminino, 12 a 59 anos". */
  rotulo: string;
}

/** Evolução de um exame numérico do paciente (ADR-0180): só liberados, da coleta mais antiga para a mais nova. */
export interface EvolucaoExameDto {
  exameId: string;
  exameNome: string;
  unidadeMedida?: string | null;
  pontos: PontoEvolucaoExame[];
}

export interface PontoEvolucaoExame {
  itemId: string;
  pedidoId: string;
  data: string;
  valor: number;
  unidadeMedida?: string | null;
  referenciaMinima?: number | null;
  referenciaMaxima?: number | null;
  criticoMinimo?: number | null;
  criticoMaximo?: number | null;
  interpretacao?: InterpretacaoResultado | null;
  faixaDescricao?: string | null;
  retificado: boolean;
}

export interface PedidoExameRequestDto {
  pacienteId: string;
  atendimentoId?: string;
  unidadeSolicitanteId: string;
  profissionalMatricula: string;
  exameIds: string[];
  indicacaoClinica: string;
  cid?: string;
  prioridade: PrioridadeExame;
}

export interface ColetaExameRequestDto {
  profissionalMatricula: string;
  unidadeColetaId: string;
  laboratorioId?: string;
  itemIds?: string[];
}

export interface ResultadoExameRequestDto {
  profissionalMatricula: string;
  valorNumerico?: number;
  valorTexto?: string;
  observacao?: string;
}

export interface RetificacaoResultadoExameRequestDto extends ResultadoExameRequestDto {
  motivo: string;
}

export interface ResultadoExameDto {
  uuid: string;
  valorNumerico?: number | null;
  valorTexto?: string | null;
  unidadeMedida?: string | null;
  referenciaMinima?: number | null;
  referenciaMaxima?: number | null;
  referenciaTexto?: string | null;
  criticoMinimo?: number | null;
  criticoMaximo?: number | null;
  /** Qual faixa valeu: "Feminino, 12 a 59 anos" ou "Faixa geral" (ADR-0180). */
  faixaDescricao?: string | null;
  interpretacao?: InterpretacaoResultado | null;
  observacao?: string | null;
  analisadoPorMatricula: string;
  analisadoPorNome: string;
  registradoEm: string;
  liberadoPorMatricula?: string | null;
  liberadoPorNome?: string | null;
  liberadoEm?: string | null;
  retificacaoDeId?: string | null;
  motivoRetificacao?: string | null;
  /** Ciência do solicitante (ADR-0181). */
  cienciaPorMatricula?: string | null;
  cienciaPorNome?: string | null;
  cienciaEm?: string | null;
}

export interface ItemPedidoExameDto {
  uuid: string;
  exameId: string;
  exameNome: string;
  material: MaterialExame;
  tipoResultado: TipoResultadoExame;
  status: StatusItemExame;
  amostraCodigo?: string | null;
  motivoCancelamento?: string | null;
  resultado?: ResultadoExameDto | null;
}

export interface AmostraExameDto {
  uuid: string;
  codigo: string;
  material: MaterialExame;
  unidadeColetaId: string;
  unidadeColetaNome: string;
  laboratorioId: string;
  laboratorioNome: string;
  coletadaEm: string;
  coletadaPorNome: string;
  rejeitada: boolean;
  motivoRejeicao?: MotivoRejeicaoAmostra | null;
  observacaoRejeicao?: string | null;
  rejeitadaEm?: string | null;
}

export interface EventoExameDto {
  tipo: TipoEventoExame;
  itemId?: string | null;
  exameNome?: string | null;
  texto?: string | null;
  profissionalMatricula: string;
  profissionalNome: string;
  ocorridoEm: string;
}

export interface PedidoExameResumoDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  unidadeSolicitanteId: string;
  unidadeSolicitanteNome: string;
  profissionalSolicitanteMatricula: string;
  profissionalSolicitanteNome: string;
  prioridade: PrioridadeExame;
  solicitadoEm: string;
  situacao: SituacaoPedidoExame;
  totalExames: number;
  liberados: number;
  itens: ItemPedidoExameDto[];
}

export interface PedidoExameResponseDto extends PedidoExameResumoDto {
  atendimentoId?: string | null;
  indicacaoClinica: string;
  cid?: string | null;
  amostras: AmostraExameDto[];
  eventos: EventoExameDto[];
  comunicacoesCritico: ComunicacaoCriticoExameDto[];
  entregas: EntregaResultadoExameDto[];
}

/** Resultado liberado aguardando ciência ou comunicação do crítico, sem o valor (ADR-0181). */
export interface ResultadoAvisoExameDto {
  itemId: string;
  pedidoId: string;
  pacienteId: string;
  pacienteNome: string;
  exameNome: string;
  interpretacao?: InterpretacaoResultado | null;
  critico: boolean;
  retificado: boolean;
  liberadoEm: string;
  prioridade: PrioridadeExame;
  solicitanteMatricula: string;
  solicitanteNome: string;
  unidadeSolicitanteNome: string;
  comunicadoEm?: string | null;
}

export interface ComunicacaoCriticoExameDto {
  uuid: string;
  itemId: string;
  resultadoId: string;
  exameNome: string;
  receptorNome: string;
  meio: MeioComunicacaoCritico;
  comunicadoEm: string;
  profissionalMatricula: string;
  profissionalNome: string;
}

export interface ComunicacaoCriticoRequestDto {
  profissionalMatricula: string;
  receptorNome: string;
  meio: MeioComunicacaoCritico;
  comunicadoEm?: string;
}

export interface EntregaResultadoExameDto {
  uuid: string;
  retiradoPeloPaciente: boolean;
  terceiroNome?: string | null;
  terceiroDocumento?: string | null;
  terceiroParentesco?: string | null;
  forma: FormaEntregaResultado;
  unidadeId: string;
  unidadeNome: string;
  entregueEm: string;
  profissionalMatricula: string;
  profissionalNome: string;
}

export interface EntregaResultadoRequestDto {
  profissionalMatricula: string;
  unidadeId: string;
  retiradoPeloPaciente: boolean;
  terceiroNome?: string;
  terceiroDocumento?: string;
  terceiroParentesco?: string;
  forma: FormaEntregaResultado;
}

export const MEIOS_COMUNICACAO: Record<MeioComunicacaoCritico, string> = {
  TELEFONE: 'Telefone',
  PRESENCIAL: 'Presencial',
  MENSAGEM_NO_SISTEMA: 'Mensagem no sistema',
};

export const FORMAS_ENTREGA: Record<FormaEntregaResultado, string> = {
  IMPRESSO_NA_UNIDADE: 'Impresso, retirado na unidade',
  NA_CONSULTA: 'Entregue na consulta',
};

export interface ItemTrabalhoExameDto {
  itemId: string;
  pedidoId: string;
  pacienteId: string;
  pacienteNome: string;
  exameNome: string;
  material: MaterialExame;
  preparo?: string | null;
  status: StatusItemExame;
  prioridade: PrioridadeExame;
  solicitadoEm: string;
  unidadeSolicitanteNome: string;
  amostraCodigo?: string | null;
  laboratorioId?: string | null;
  laboratorioNome?: string | null;
  /** Na coleta: motivo da rejeição da amostra anterior, quando é recoleta (ADR-0095). */
  motivoRecoleta?: MotivoRejeicaoAmostra | null;
}

/** Um exame do paciente no prontuário (ADR-0095): resultado só depois de liberado. */
export interface ExameProntuarioDto {
  itemId: string;
  pedidoId: string;
  atendimentoId?: string | null;
  /** O exame do catálogo: chave da evolução (ADR-0180). */
  exameId: string;
  exameNome: string;
  material: MaterialExame;
  status: StatusItemExame;
  prioridade: PrioridadeExame;
  solicitadoEm: string;
  unidadeSolicitanteNome: string;
  profissionalSolicitanteNome: string;
  resultado?: ResultadoExameDto | null;
}

// ── Rótulos e formatação, usados na tela Laboratório, no prontuário e no laudo ──

export const MATERIAIS: Record<MaterialExame, string> = { SANGUE: 'Sangue', URINA: 'Urina', FEZES: 'Fezes', SECRECAO: 'Secreção', OUTRO: 'Outro' };

export const STATUS_ITEM: Record<StatusItemExame, { classe: string; rotulo: string }> = {
  SOLICITADO: { classe: 'info', rotulo: 'Aguardando coleta' },
  COLETADO: { classe: 'warn', rotulo: 'Em análise' },
  RESULTADO_REGISTRADO: { classe: 'purple', rotulo: 'Para liberar' },
  LIBERADO: { classe: 'ok', rotulo: 'Liberado' },
  CANCELADO: { classe: 'muted', rotulo: 'Cancelado' },
};

export const INTERPRETACOES: Record<InterpretacaoResultado, { classe: string; rotulo: string }> = {
  NORMAL: { classe: 'ok', rotulo: 'Dentro da referência' },
  ACIMA: { classe: 'alert', rotulo: 'Acima da referência' },
  ABAIXO: { classe: 'warn', rotulo: 'Abaixo da referência' },
  CRITICO_ALTO: { classe: 'alert', rotulo: 'Crítico alto' },
  CRITICO_BAIXO: { classe: 'alert', rotulo: 'Crítico baixo' },
};

export const UNIDADES_IDADE: Record<UnidadeIdade, string> = { DIAS: 'dias', MESES: 'meses', ANOS: 'anos' };

/** Valor de pânico (ADR-0180): pede aviso imediato. */
export function critico(i: InterpretacaoResultado | null | undefined): boolean {
  return i === 'CRITICO_ALTO' || i === 'CRITICO_BAIXO';
}

/** Os valores críticos, para mostrar ao lado da referência: "crítico < 7 ou > 20". */
export function faixaCritica(e: { criticoMinimo?: number | null; criticoMaximo?: number | null } | null | undefined): string {
  if (!e || (e.criticoMinimo == null && e.criticoMaximo == null)) return '';
  const partes = [
    e.criticoMinimo != null ? `< ${numeroLaboratorio(e.criticoMinimo)}` : '',
    e.criticoMaximo != null ? `> ${numeroLaboratorio(e.criticoMaximo)}` : '',
  ].filter(Boolean);
  return 'crítico ' + partes.join(' ou ');
}

export const MOTIVOS_REJEICAO: Record<MotivoRejeicaoAmostra, string> = {
  HEMOLISADA: 'Hemolisada',
  INSUFICIENTE: 'Volume insuficiente',
  COAGULADA: 'Coagulada',
  IDENTIFICACAO_INCORRETA: 'Identificação incorreta',
  OUTRO: 'Outro',
};

export function numeroLaboratorio(v: number | null | undefined): string {
  return v === null || v === undefined ? '' : Number(v).toLocaleString('pt-BR', { maximumFractionDigits: 4 });
}

type Referencia = { referenciaMinima?: number | null; referenciaMaxima?: number | null; referenciaTexto?: string | null; unidadeMedida?: string | null };

export function faixaReferencia(e: Referencia | undefined | null): string {
  if (!e) return '—';
  if (e.referenciaTexto) return e.referenciaTexto;
  const un = e.unidadeMedida ? ` ${e.unidadeMedida}` : '';
  if (e.referenciaMinima != null && e.referenciaMaxima != null) return `${numeroLaboratorio(e.referenciaMinima)} a ${numeroLaboratorio(e.referenciaMaxima)}${un}`;
  if (e.referenciaMinima != null) return `≥ ${numeroLaboratorio(e.referenciaMinima)}${un}`;
  if (e.referenciaMaxima != null) return `≤ ${numeroLaboratorio(e.referenciaMaxima)}${un}`;
  return '—';
}

export function valorResultado(r: ResultadoExameDto | null | undefined): string {
  if (!r) return '—';
  return r.valorTexto ?? `${numeroLaboratorio(r.valorNumerico)}${r.unidadeMedida ? ' ' + r.unidadeMedida : ''}`;
}
