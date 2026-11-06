// ── Vigilância ambiental, fatia V4c da Vigilância (ADR-0164) ──

export type TipoAbastecimentoAgua = 'REDE' | 'SOLUCAO_ALTERNATIVA_COLETIVA' | 'SOLUCAO_ALTERNATIVA_INDIVIDUAL';
export type TipoRiscoAmbiental = 'AREA_CONTAMINADA' | 'DESCARTE_IRREGULAR' | 'FONTE_POLUIDORA' | 'OUTRO';
export type SituacaoRiscoAmbiental = 'ATIVO' | 'CONTROLADO';

export const TIPOS_ABASTECIMENTO: Record<TipoAbastecimentoAgua, string> = {
  REDE: 'Rede de abastecimento',
  SOLUCAO_ALTERNATIVA_COLETIVA: 'Solução alternativa coletiva',
  SOLUCAO_ALTERNATIVA_INDIVIDUAL: 'Solução alternativa individual',
};

export const TIPOS_RISCO_AMBIENTAL: Record<TipoRiscoAmbiental, string> = {
  AREA_CONTAMINADA: 'Área contaminada',
  DESCARTE_IRREGULAR: 'Descarte irregular',
  FONTE_POLUIDORA: 'Fonte poluidora',
  OUTRO: 'Outro',
};

export interface PontoColetaAguaDto {
  uuid: string;
  nome: string;
  tipo: TipoAbastecimentoAgua;
  local: string;
  latitude: number | null;
  longitude: number | null;
  microareaCodigo: string | null;
  areaNome: string | null;
  ativo: boolean;
  ultimaColeta: string | null;
  ultimaConforme: boolean | null;
  amostras: number;
}

export interface AmostraAguaDto {
  uuid: string;
  pontoId: string;
  pontoNome: string;
  tipo: TipoAbastecimentoAgua;
  dataColeta: string;
  coliformesTotais: boolean | null;
  ecoli: boolean | null;
  cloroResidual: number | null;
  turbidez: number | null;
  fluor: number | null;
  conforme: boolean;
  naoConformidades: string[];
  medida: string | null;
  medidaEm: string | null;
}

export interface RiscoAmbientalDto {
  uuid: string;
  tipo: TipoRiscoAmbiental;
  descricao: string;
  local: string;
  latitude: number | null;
  longitude: number | null;
  microareaCodigo: string | null;
  areaNome: string | null;
  populacaoExposta: number | null;
  situacao: SituacaoRiscoAmbiental;
  controladoEm: string | null;
  motivoControle: string | null;
  registradoEm: string;
}
