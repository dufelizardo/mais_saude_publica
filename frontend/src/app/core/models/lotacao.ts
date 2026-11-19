export interface LotacaoRequestDto {
  matriculaProfissional: string;
  unidadeId: string;
  cargoId: string;
  /** O setor da unidade, opcional (ADR-0221): a folha vai ao centro de custo do setor. */
  setorId?: string;
  jornadaSemanalHoras?: number;
  dataInicio: string;
  motivo?: string;
}

export interface LotacaoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  unidadeNome: string;
  setorNome?: string | null;
  cargoNome: string;
  categoriaNome: string;
  jornadaSemanalHoras?: number;
  dataInicio: string;
  dataFim?: string;
  motivo?: string;
}
