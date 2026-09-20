import { AjusteIndividualResponseDto } from './ajuste-individual';

export interface ComposicaoRemuneratoriaResponseDto {
  profissionalMatricula: string;
  profissionalNome: string;
  cargoNome: string;
  categoriaNome: string;
  valorBase: number;
  anosCompletos?: number;
  percentualAnuenio?: number;
  valorAnuenio: number;
  ajustesIndividuaisVigentes: AjusteIndividualResponseDto[];
  totalAjustesIndividuais: number;
  total: number;
}
