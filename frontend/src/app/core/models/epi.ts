export interface EpiRequestDto {
  matriculaProfissional: string;
  tipo: string;
  numeroCA?: string;
  dataEntrega: string;
  dataDevolucao?: string;
}

export interface EpiResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipo: string;
  numeroCA?: string;
  dataEntrega: string;
  dataDevolucao?: string;
}
