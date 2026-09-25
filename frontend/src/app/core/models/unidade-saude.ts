export interface UnidadeSaudeResponseDto {
  uuid: string;
  nome: string;
}

export type TipoUnidadeDeSaude =
  | 'FEDERAL'
  | 'ESTADUAL'
  | 'MUNICIPAL'
  | 'REGIONAL'
  | 'UBS'
  | 'HOSPITAL'
  | 'UPA'
  | 'LABORATORIO'
  | 'CAPS'
  | 'CENTRO_ESPECIALIDADES'
  | 'CENTRO_REABILITACAO'
  | 'POLICLINICA';
