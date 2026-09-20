export type TipoRegistroPonto = 'ENTRADA' | 'SAIDA' | 'INICIO_INTERVALO' | 'FIM_INTERVALO';

export interface RegistroPontoRequestDto {
  matriculaProfissional: string;
  dataHora: string;
  tipo: TipoRegistroPonto;
  origem?: string;
}

export interface RegistroPontoCorrecaoRequestDto {
  dataHoraProposta: string;
  tipoProposto: TipoRegistroPonto;
  justificativa: string;
}

export interface RegistroPontoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  tipo: TipoRegistroPonto;
  origem?: string;
  dataHoraProposta?: string;
  tipoProposto?: TipoRegistroPonto;
  justificativaCorrecao?: string;
}
