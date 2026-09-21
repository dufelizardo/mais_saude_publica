export interface LotacaoRequestDto {
  matriculaProfissional: string;
  unidadeId: string;
  cargoId: string;
  jornadaSemanalHoras?: number;
  dataInicio: string;
  motivo?: string;
}

export interface LotacaoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  unidadeNome: string;
  cargoNome: string;
  categoriaNome: string;
  jornadaSemanalHoras?: number;
  dataInicio: string;
  dataFim?: string;
  motivo?: string;
}
