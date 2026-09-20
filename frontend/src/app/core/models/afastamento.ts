export type TipoAfastamento = 'FERIAS' | 'LICENCA_MEDICA' | 'LICENCA_PESSOAL' | 'OUTROS';
export type StatusAfastamento = 'SOLICITADO' | 'APROVADO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';

export interface AfastamentoRequestDto {
  matriculaProfissional: string;
  tipo: TipoAfastamento;
  dataInicio: string;
  dataFim: string;
  status: StatusAfastamento;
  observacao?: string;
}

export interface AfastamentoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipo: TipoAfastamento;
  dataInicio: string;
  dataFim: string;
  status: StatusAfastamento;
  observacao?: string;
}
