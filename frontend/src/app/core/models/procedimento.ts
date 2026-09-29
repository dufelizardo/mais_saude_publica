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
  /** Retificação (ADR-0062): versão que esta corrige, motivo, e se outra versão já corrige esta. */
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  registradoEm?: string | null;
  registradoPorCpf?: string | null;
  retificado?: boolean;
  retificadoPorUuid?: string | null;
  /** Desfecho de um procedimento agendado (ADR-0062). */
  dataPrevista?: string | null;
  statusAlteradoEm?: string | null;
  profissionalStatusMatricula?: string | null;
  profissionalStatusNome?: string | null;
  justificativaStatus?: string | null;
}
