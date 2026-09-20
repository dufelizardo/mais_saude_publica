export interface CicloAvaliacaoResponseDto {
  uuid: string;
  nome: string;
  dataInicio: string;
  dataFim: string;
}

export interface AvaliacaoRequestDto {
  matriculaProfissional: string;
  cicloId: string;
  avaliador: string;
  nota: number;
  observacao?: string;
}

export interface AvaliacaoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  cicloNome: string;
  avaliador: string;
  nota: number;
  observacao?: string;
}
