export interface AdesaoBeneficioRequestDto {
  matriculaProfissional: string;
  tipoBeneficioId: string;
  dataInicio: string;
  quantidadeDependentes?: number;
}

export interface AdesaoBeneficioResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipoBeneficioNome: string;
  dataInicio: string;
  dataFim?: string;
  quantidadeDependentes?: number;
}
