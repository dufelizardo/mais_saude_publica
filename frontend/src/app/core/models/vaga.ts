export type StatusVaga = 'ABERTA' | 'EM_ANDAMENTO' | 'FECHADA' | 'CANCELADA';

export interface VagaRequestDto {
  unidadeId: string;
  cargoId: string;
  quantidade: number;
  status: StatusVaga;
}

export interface VagaResponseDto {
  uuid: string;
  unidadeUuid: string;
  unidadeNome: string;
  cargoUuid: string;
  cargoNome: string;
  cargoDescricao?: string;
  quantidade: number;
  status: StatusVaga;
}
