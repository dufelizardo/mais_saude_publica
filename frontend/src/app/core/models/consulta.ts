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
}
