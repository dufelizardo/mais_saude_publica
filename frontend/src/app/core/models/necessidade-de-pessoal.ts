export interface NecessidadeDePessoalRequestDto {
  unidadeId: string;
  setorId?: string;
  cargoId: string;
  quantidade: number;
  jornadaSemanalHoras: number;
  competenciasNecessarias?: string;
  justificativa?: string;
}

export interface NecessidadeDePessoalResponseDto {
  uuid: string;
  unidadeUuid: string;
  unidadeNome: string;
  setorUuid?: string;
  setorNome?: string;
  cargoUuid: string;
  cargoNome: string;
  quantidade: number;
  jornadaSemanalHoras: number;
  competenciasNecessarias?: string;
  justificativa?: string;
  dataRegistro: string;
  vagaAssociadaUuid?: string;
}
