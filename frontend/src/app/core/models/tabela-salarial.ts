export type MotivoTabelaSalarial = 'DISSIDIO' | 'REVISAO_PLANO_CARGOS_SALARIOS';

export interface TabelaSalarialRequestDto {
  cargoId: string;
  valorBase: number;
  dataVigencia: string;
  motivo: MotivoTabelaSalarial;
}

export interface TabelaSalarialResponseDto {
  uuid: string;
  cargoNome: string;
  categoriaNome: string;
  valorBase: number;
  dataVigencia: string;
  motivo: MotivoTabelaSalarial;
}
