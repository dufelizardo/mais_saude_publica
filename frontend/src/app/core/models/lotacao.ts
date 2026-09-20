export interface LotacaoRequestDto {
  matriculaProfissional: string;
  unidadeId: string;
  cargoId: string;
  jornadaSemanalHoras?: number;
  dataInicio: string;
  motivo?: string;
}
