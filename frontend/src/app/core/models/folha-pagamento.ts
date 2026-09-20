export interface FolhaPagamentoRequestDto {
  matriculaProfissional: string;
  competencia: string;
  proventos: number;
  descontos: number;
  encargos: number;
  total: number;
}

export interface FolhaPagamentoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  competencia: string;
  proventos: number;
  descontos: number;
  encargos: number;
  total: number;
}
