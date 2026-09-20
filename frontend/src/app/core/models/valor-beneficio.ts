export interface ValorBeneficioRequestDto {
  tipoBeneficioId: string;
  valor: number;
  dataVigencia: string;
  motivo?: string;
}

export interface ValorBeneficioResponseDto {
  uuid: string;
  tipoBeneficioNome: string;
  valor: number;
  dataVigencia: string;
  motivo?: string;
}
