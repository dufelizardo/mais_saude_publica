export interface ProcessoAdministrativoRequestDto {
  capacidadeId: string;
  codigo: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

export interface ProcessoAdministrativoResponseDto {
  uuid: string;
  capacidadeUuid: string;
  capacidadeCodigo: string;
  capacidadeNome: string;
  codigo: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
}
