export interface PerfilAdministrativoRequestDto {
  codigo: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

export interface PerfilAdministrativoResponseDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
}
