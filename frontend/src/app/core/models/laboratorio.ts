/** Laboratório assistencial (#10) — ADRs 0093 e 0094. */

export type MaterialExame = 'SANGUE' | 'URINA' | 'FEZES' | 'SECRECAO' | 'OUTRO';
export type TipoResultadoExame = 'NUMERICO' | 'TEXTO';
export type PrioridadeExame = 'ROTINA' | 'URGENTE';
export type StatusItemExame = 'SOLICITADO' | 'COLETADO' | 'RESULTADO_REGISTRADO' | 'LIBERADO' | 'CANCELADO';
export type MotivoRejeicaoAmostra = 'HEMOLISADA' | 'INSUFICIENTE' | 'COAGULADA' | 'IDENTIFICACAO_INCORRETA' | 'OUTRO';
export type InterpretacaoResultado = 'NORMAL' | 'ACIMA' | 'ABAIXO';
export type TipoEventoExame = 'SOLICITACAO' | 'COLETA' | 'REJEICAO_AMOSTRA' | 'RESULTADO' | 'LIBERACAO' | 'RETIFICACAO' | 'CANCELAMENTO';
export type SituacaoPedidoExame = 'AGUARDANDO_COLETA' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';
export type EtapaTrabalho = 'PARA_COLETAR' | 'EM_ANALISE' | 'PARA_LIBERAR';

export interface ExameLaboratorialRequestDto {
  nome: string;
  material: MaterialExame;
  tipoResultado: TipoResultadoExame;
  unidadeMedida?: string;
  referenciaMinima?: number | null;
  referenciaMaxima?: number | null;
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
  referenciaTexto?: string | null;
  preparo?: string | null;
  prazoDias?: number | null;
  ativo: boolean;
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
}

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
};

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
