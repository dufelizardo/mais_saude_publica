export type TipoAtendimento = 'CONSULTA' | 'URGENCIA' | 'INTERNACAO';
export type StatusAtendimento = 'AGENDADO' | 'EM_ANDAMENTO' | 'CONCLUIDO';

export interface AtendimentoRequestDto {
  pacienteId: string;
  profissionalMatricula: string;
  unidadeId: string;
  setorId?: string;
  agendamentoId?: string;
  tipo: TipoAtendimento;
  status: StatusAtendimento;
  dataHora: string;
}

export interface AtendimentoResponseDto {
  uuid: string;
  pacienteUuid: string;
  pacienteNome: string;
  profissionalMatricula: string;
  profissionalNome: string;
  unidadeUuid: string;
  unidadeNome: string;
  setorUuid?: string;
  setorNome?: string;
  agendamentoUuid?: string;
  tipo: TipoAtendimento;
  status: StatusAtendimento;
  dataHora: string;
}
