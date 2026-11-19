/** Patrimônio, fatia PM1 (ADR-0215): o bem tombado, o termo de responsabilidade e a transferência entre unidades. */

export type ClasseBem = 'EQUIPAMENTO_MEDICO_HOSPITALAR' | 'INFORMATICA' | 'MOBILIARIO' | 'VEICULO' | 'ELETRODOMESTICO' | 'OUTRO';
export type SituacaoBem = 'A_TOMBAR' | 'EM_USO' | 'OCIOSO' | 'EM_MANUTENCAO' | 'BAIXADO';
export type OrigemBem = 'COMPRA' | 'DOACAO' | 'CESSAO_COMODATO' | 'TRANSFERENCIA_ENTE';
export type SituacaoTransferenciaBem = 'PENDENTE' | 'ACEITA' | 'RECUSADA' | 'CANCELADA';

export const CLASSES_BEM: Record<ClasseBem, string> = {
  EQUIPAMENTO_MEDICO_HOSPITALAR: 'Equipamento médico-hospitalar',
  INFORMATICA: 'Informática',
  MOBILIARIO: 'Mobiliário',
  VEICULO: 'Veículo',
  ELETRODOMESTICO: 'Eletrodoméstico',
  OUTRO: 'Outro',
};

export const SITUACOES_BEM: Record<SituacaoBem, string> = {
  A_TOMBAR: 'A tombar',
  EM_USO: 'Em uso',
  OCIOSO: 'Ocioso',
  EM_MANUTENCAO: 'Em manutenção',
  BAIXADO: 'Baixado',
};

export const ORIGENS_BEM: Record<OrigemBem, string> = {
  COMPRA: 'Compra',
  DOACAO: 'Doação',
  CESSAO_COMODATO: 'Cessão ou comodato',
  TRANSFERENCIA_ENTE: 'Transferência de outro ente',
};

export const SITUACOES_TRANSFERENCIA: Record<SituacaoTransferenciaBem, string> = {
  PENDENTE: 'À espera do aceite',
  ACEITA: 'Aceita',
  RECUSADA: 'Recusada',
  CANCELADA: 'Cancelada',
};

export interface TransferenciaBemDto {
  uuid: string;
  bemId: string;
  numeroTombamento: string | null;
  bemDescricao: string;
  origemId: string;
  origemNome: string;
  destinoId: string;
  destinoNome: string;
  motivo: string;
  enviadaEm: string;
  situacao: SituacaoTransferenciaBem;
  resolvidaEm: string | null;
  motivoRecusa: string | null;
}

export interface BemDto {
  uuid: string;
  numeroTombamento: string | null;
  descricao: string;
  classe: ClasseBem | null;
  marca: string | null;
  modelo: string | null;
  numeroSerie: string | null;
  unidadeId: string;
  unidadeNome: string;
  setorId: string | null;
  setorNome: string | null;
  situacao: SituacaoBem;
  origem: OrigemBem;
  valorAquisicao: number | null;
  dataAquisicao: string | null;
  documento: string | null;
  fornecedorNome: string | null;
  tombadoEm: string | null;
  responsavelMatricula: string | null;
  responsavelNome: string | null;
  responsavelDesde: string | null;
  veiculoId: string | null;
  veiculoPlaca: string | null;
  transferenciaPendente: TransferenciaBemDto | null;
  observacao: string | null;
}

export interface ResponsavelBemDto {
  uuid: string;
  matricula: string;
  nome: string;
  desde: string;
  ate: string | null;
}

export interface DetalheBemDto {
  bem: BemDto;
  responsaveis: ResponsavelBemDto[];
  transferencias: TransferenciaBemDto[];
}

export interface BemRequest {
  descricao: string;
  classe: ClasseBem;
  marca: string | null;
  modelo: string | null;
  numeroSerie: string | null;
  unidadeId: string;
  setorId: string | null;
  origem: OrigemBem;
  valorAquisicao: number | null;
  dataAquisicao: string;
  documento: string | null;
  responsavelMatricula: string;
  observacao: string | null;
}
