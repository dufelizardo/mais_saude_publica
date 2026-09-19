export interface CargoRequestDto {
  categoriaId: string;
  nome: string;
}

export interface CargoResponseDto {
  uuid: string;
  nome: string;
  categoriaUuid: string;
  categoriaNome: string;
}
