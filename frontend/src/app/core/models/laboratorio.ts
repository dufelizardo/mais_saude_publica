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
}
