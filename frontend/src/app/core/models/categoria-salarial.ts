export interface CategoriaSalarialRequestDto {
  nome: string;
  convencaoColetiva?: string;
}

export interface CategoriaSalarialResponseDto {
  uuid: string;
  nome: string;
  convencaoColetiva?: string;
}
