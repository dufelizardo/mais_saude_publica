export type TipoConsulta = 'PRIMEIRA' | 'RETORNO' | 'URGENCIA';

export interface ConsultaRequestDto {
  atendimentoId: string;
  profissionalMatricula: string;
  dataHora: string;
  tipoConsulta: TipoConsulta;
  queixaPrincipal?: string;
  diagnostico?: string;
  receituario?: string;
  examesSolicitados?: string;
  retorno?: string;
}

export interface ConsultaResponseDto {
  uuid: string;
  atendimentoUuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  tipoConsulta: TipoConsulta;
  queixaPrincipal?: string;
  diagnostico?: string;
  receituario?: string;
  examesSolicitados?: string;
  retorno?: string;
  /** Retificação (ADR-0062): versão que esta corrige, motivo, e se outra versão já corrige esta. */
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  registradoEm?: string | null;
  registradoPorCpf?: string | null;
  retificado?: boolean;
  retificadoPorUuid?: string | null;
}
