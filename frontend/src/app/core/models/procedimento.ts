export type StatusProcedimento = 'AGENDADO' | 'REALIZADO' | 'CANCELADO';

export interface ProcedimentoRequestDto {
  consultaId: string;
  profissionalMatricula: string;
  tipo: string;
  descricao?: string;
  dataRealizacao: string;
  status: StatusProcedimento;
}

export interface ProcedimentoResponseDto {
  uuid: string;
  consultaUuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipo: string;
  descricao?: string;
  dataRealizacao: string;
  status: StatusProcedimento;
}
