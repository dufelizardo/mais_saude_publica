export type TipoExameOcupacional = 'ADMISSIONAL' | 'PERIODICO' | 'DEMISSIONAL' | 'RETORNO' | 'MUDANCA_FUNCAO';
export type ResultadoExameOcupacional = 'APTO' | 'INAPTO' | 'APTO_COM_RESTRICAO';

export interface ExameOcupacionalRequestDto {
  matriculaProfissional: string;
  tipo: TipoExameOcupacional;
  dataRealizacao: string;
  dataValidade?: string;
  resultado: ResultadoExameOcupacional;
  asoUrl?: string;
}

export interface ExameOcupacionalResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipo: TipoExameOcupacional;
  dataRealizacao: string;
  dataValidade?: string;
  resultado: ResultadoExameOcupacional;
  asoUrl?: string;
}
