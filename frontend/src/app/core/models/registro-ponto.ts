export type TipoRegistroPonto = 'ENTRADA' | 'SAIDA' | 'INICIO_INTERVALO' | 'FIM_INTERVALO';

export interface RegistroPontoResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  tipo: TipoRegistroPonto;
  origem?: string;
}
