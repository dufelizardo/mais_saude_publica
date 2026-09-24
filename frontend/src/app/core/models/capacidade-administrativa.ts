export interface CapacidadeAdministrativaRequestDto {
  codigo: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

export interface CapacidadeAdministrativaResponseDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
}
