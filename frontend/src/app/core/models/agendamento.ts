export type TipoAgendamento = 'CONSULTA' | 'PROCEDIMENTO' | 'RETORNO';
export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'REALIZADO' | 'CANCELADO';

export interface AgendamentoRequestDto {
  pacienteId: string;
  profissionalMatricula: string;
  dataHora: string;
  status: StatusAgendamento;
  tipo: TipoAgendamento;
  observacao?: string;
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
}
