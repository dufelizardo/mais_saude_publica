export interface RegraAnuenioRequestDto {
  categoriaId: string;
  percentualPorAno: number;
  tetoAnos?: number;
}

export interface RegraAnuenioResponseDto {
  uuid: string;
  categoriaUuid: string;
  categoriaNome: string;
  percentualPorAno: number;
  tetoAnos?: number;
}
