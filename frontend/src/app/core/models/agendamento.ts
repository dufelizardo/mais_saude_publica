export type TipoAgendamento = 'CONSULTA' | 'PROCEDIMENTO' | 'RETORNO';
export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'REALIZADO' | 'CANCELADO' | 'FALTOU';

export interface AgendamentoRequestDto {
  pacienteId: string;
  profissionalMatricula: string;
  dataHora: string;
  status: StatusAgendamento;
  tipo: TipoAgendamento;
  observacao?: string;
  /** Com unidade, a marcação segue a agenda do profissional ali: vaga livre ou encaixe (ADR-0091). */
  unidadeId?: string;
  encaixe?: boolean;
  /** O programa da marcação e a ação programática que ela cumpre (ADR-0132). */
  programaId?: string | null;
  acaoProgramaticaId?: string | null;
}

export interface AgendamentoResponseDto {
  uuid: string;
  pacienteUuid: string;
  pacienteNome: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  status: StatusAgendamento;
  tipo: TipoAgendamento;
  observacao?: string;
  unidadeUuid?: string | null;
  unidadeNome?: string | null;
  encaixe?: boolean;
  programaId?: string | null;
  programaNome?: string | null;
  /** A especialidade (ADR-0134). */
  especialidadeId?: string | null;
  especialidadeNome?: string | null;
}
