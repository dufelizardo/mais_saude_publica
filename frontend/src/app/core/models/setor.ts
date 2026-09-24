export type TipoSetor = 'ADMINISTRATIVO' | 'ASSISTENCIAL' | 'APOIO' | 'TECNICO';

export interface SetorRequestDto {
  unidadeId: string;
  nome: string;
  codigo: string;
  tipo: TipoSetor;
  ativo: boolean;
  matriculaResponsavel?: string;
}

export interface SetorResponseDto {
  uuid: string;
  unidadeUuid: string;
  unidadeNome: string;
  nome: string;
  codigo: string;
  tipo: TipoSetor;
  ativo: boolean;
  responsavelMatricula?: string;
  responsavelNome?: string;
}
