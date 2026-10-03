/** Regulação do acesso (#11) — Central de Regulação do Acesso (ADRs 0087 e 0088). */

export type TipoProcedimentoRegulado = 'CONSULTA_ESPECIALIZADA' | 'EXAME' | 'PROCEDIMENTO';

/** Ordem da fila: vermelho primeiro. */
export type PrioridadeRegulacao = 'VERMELHO' | 'AMARELO' | 'VERDE' | 'AZUL';

export type StatusSolicitacaoRegulacao =
  | 'SOLICITADA'
  | 'DEVOLVIDA'
  | 'AUTORIZADA'
  | 'NEGADA'
  | 'AGENDADA'
  | 'REALIZADA'
  | 'FALTOU'
  | 'CANCELADA';

export type TipoEventoRegulacao =
  | 'SOLICITACAO'
  | 'COMPLEMENTO'
  | 'RECLASSIFICACAO'
  | 'AUTORIZACAO'
  | 'DEVOLUCAO'
  | 'NEGATIVA'
  | 'CANCELAMENTO'
  | 'AGENDAMENTO'
  | 'REALIZACAO'
  | 'FALTA';

export interface ProcedimentoReguladoRequestDto {
  nome: string;
  tipo: TipoProcedimentoRegulado;
  ativo?: boolean;
}

export interface ProcedimentoReguladoResponseDto {
  uuid: string;
  nome: string;
  tipo: TipoProcedimentoRegulado;
  ativo: boolean;
}

export interface SolicitacaoRegulacaoRequestDto {
  pacienteId: string;
  procedimentoId: string;
  unidadeSolicitanteId: string;
  profissionalMatricula: string;
  cid: string;
  justificativa: string;
  prioridade: PrioridadeRegulacao;
}

export interface ComplementoRegulacaoRequestDto {
  profissionalMatricula: string;
  complemento: string;
}

export interface ReclassificacaoRegulacaoRequestDto {
  profissionalMatricula: string;
  prioridade: PrioridadeRegulacao;
  motivo: string;
}

export interface AutorizacaoRegulacaoRequestDto {
  profissionalMatricula: string;
  unidadeExecutanteId: string;
  /** LocalDateTime, sem fuso. */
  dataHoraPrevista: string;
  observacao?: string;
  /** Quem vai atender, se já se sabe: a autorização já agenda (ADR-0089). */
  profissionalExecutanteMatricula?: string;
}

/** Agendamento da autorizada na executante (ADR-0089); sem data, vale a da vaga. */
export interface AgendamentoRegulacaoRequestDto {
  profissionalMatricula: string;
  profissionalExecutanteMatricula: string;
  dataHora?: string;
}

/** Atendido na executante, com a contrarreferência para a origem (ADR-0089). */
export interface RealizacaoRegulacaoRequestDto {
  profissionalMatricula: string;
  contrarreferencia: string;
}

/** Devolução, negativa ou cancelamento. */
export interface MotivoRegulacaoRequestDto {
  profissionalMatricula: string;
  motivo: string;
}

/** Sem dado clínico: listagem, fila e andamento para a recepção. */
export interface SolicitacaoRegulacaoResumoDto {
  uuid: string;
  status: StatusSolicitacaoRegulacao;
  prioridade: PrioridadeRegulacao;
  posicaoNaFila?: number | null;
  procedimentoId: string;
  procedimentoNome: string;
  procedimentoTipo: TipoProcedimentoRegulado;
  pacienteId: string;
  pacienteNome: string;
  unidadeSolicitanteId: string;
  unidadeSolicitanteNome: string;
  profissionalSolicitanteMatricula: string;
  profissionalSolicitanteNome: string;
  /** Instant (UTC). */
  solicitadoEm: string;
  unidadeExecutanteId?: string | null;
  unidadeExecutanteNome?: string | null;
  dataHoraPrevista?: string | null;
  agendamentoId?: string | null;
  agendamentoDataHora?: string | null;
  profissionalExecutanteMatricula?: string | null;
  profissionalExecutanteNome?: string | null;
  concluidoEm?: string | null;
}

export interface EventoRegulacaoResponseDto {
  tipo: TipoEventoRegulacao;
  statusResultante: StatusSolicitacaoRegulacao;
  prioridade: PrioridadeRegulacao;
  texto?: string | null;
  profissionalMatricula: string;
  profissionalNome: string;
  ocorridoEm: string;
  registradoPorCpf?: string | null;
}

/** Detalhe com CID, justificativa e eventos; a leitura é auditada. */
export interface SolicitacaoRegulacaoResponseDto extends SolicitacaoRegulacaoResumoDto {
  cid: string;
  justificativa: string;
  contrarreferencia?: string | null;
  eventos: EventoRegulacaoResponseDto[];
}
