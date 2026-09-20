export type CusteioBeneficio = 'EMPRESA' | 'COMPARTILHADO' | 'PROFISSIONAL';

export interface TipoBeneficioRequestDto {
  nome: string;
  custeio: CusteioBeneficio;
}

export interface TipoBeneficioResponseDto {
  uuid: string;
  nome: string;
  custeio: CusteioBeneficio;
}
