/** Território e adscrição, fatia F1 — ADR-0125 (desenho na ADR-0108). */

import { TipoEquipe } from './equipe';
import { TipoUnidadeDeSaude } from './unidade-saude';

export type TipoSegmentoTerritorial = 'DISTRITO_SANITARIO' | 'ZONA_IBGE' | 'OUTRO';
export type FinalidadeTerritorio = 'ATENCAO_PRIMARIA' | 'REFERENCIA_ASSISTENCIAL' | 'VIGILANCIA';

export const TIPOS_SEGMENTO: Record<TipoSegmentoTerritorial, string> = {
  DISTRITO_SANITARIO: 'Distrito sanitário',
  ZONA_IBGE: 'Zona do IBGE',
  OUTRO: 'Outro agrupamento',
};

export const FINALIDADES: Record<FinalidadeTerritorio, string> = {
  ATENCAO_PRIMARIA: 'Atenção primária',
  REFERENCIA_ASSISTENCIAL: 'Referência assistencial',
  VIGILANCIA: 'Vigilância',
};

/** Referência do CNES para a população de uma microárea. */
export const POPULACAO_REFERENCIA_CNES = 750;

export interface EquipeRefDto {
  uuid: string;
  nome: string;
  tipo: TipoEquipe;
  unidadeId: string;
  unidadeNome: string;
  desde: string;
}

export interface AcsRefDto {
  membroId: string;
  matricula: string;
  nome: string;
  desde?: string | null;
}

export interface MicroareaNoDto {
  uuid: string;
  areaId: string;
  codigo: string;
  nome?: string | null;
  geometria?: string | null;
  populacaoReferencia?: number | null;
  acs?: AcsRefDto | null;
  avisos: string[];
  /** Cadastro territorial (ADR-0127). */
  imoveis: number;
  familias: number;
  pessoas: number;
}

export interface AreaNoDto {
  uuid: string;
  territorioId: string;
  codigo: string;
  codigoCnes?: string | null;
  nome: string;
  geometria?: string | null;
  equipe?: EquipeRefDto | null;
  microareas: MicroareaNoDto[];
}

export interface TerritorioNoDto {
  uuid: string;
  segmentoId: string;
  nome: string;
  finalidade: FinalidadeTerritorio;
  ativo: boolean;
  areas: AreaNoDto[];
}

export interface SegmentoDto {
  uuid: string;
  codigo: string;
  nome: string;
  tipo: TipoSegmentoTerritorial;
  unidadeGestaoId?: string | null;
  unidadeGestaoNome?: string | null;
  geometria?: string | null;
  territorios: TerritorioNoDto[];
}

export interface ArvoreTerritorialDto {
  indicadores: {
    areas: number;
    areasSemEquipe: number;
    microareas: number;
    microareasSemAcs: number;
    semGeometria: number;
    acimaDaReferencia: number;
    imoveis: number;
    familias: number;
    pessoas: number;
  };
  segmentos: SegmentoDto[];
}

export interface AreaDetalheDto {
  area: AreaNoDto;
  territorioNome: string;
  finalidade: FinalidadeTerritorio;
  segmentoNome: string;
  responsabilidades: { uuid: string; equipeId: string; equipeNome: string; inicio: string; fim?: string | null; motivoFim?: string | null }[];
  atribuicoes: {
    uuid: string;
    microareaId: string;
    microareaCodigo: string;
    membroId: string;
    matricula: string;
    nome: string;
    inicio: string;
    fim?: string | null;
    motivoFim?: string | null;
  }[];
  acsSemMicroarea: AcsRefDto[];
}

export interface UnidadeNoMapaDto {
  uuid: string;
  nome: string;
  tipo: TipoUnidadeDeSaude;
  cnes?: string | null;
  latitude?: number | null;
  longitude?: number | null;
}

export interface SegmentoRequestDto {
  codigo: string;
  nome: string;
  tipo: TipoSegmentoTerritorial;
  unidadeGestaoId?: string | null;
  geometria?: string | null;
}

export interface TerritorioRequestDto {
  segmentoId: string;
  nome: string;
  finalidade: FinalidadeTerritorio;
  ativo: boolean;
}

export interface AreaRequestDto {
  territorioId: string;
  codigo: string;
  codigoCnes?: string | null;
  nome: string;
  geometria?: string | null;
}

export interface MicroareaRequestDto {
  areaId: string;
  codigo: string;
  nome?: string | null;
  populacaoReferencia?: number | null;
  geometria?: string | null;
}

/** O escopo territorial de quem está logado (ADR-0129). */
export interface MeuEscopoTerritorialDto {
  ativo: boolean;
  restrito: boolean;
  microareas: { uuid: string; codigo: string; areaId: string; areaCodigo: string }[];
  areas: { uuid: string; codigo: string; areaId: string; areaCodigo: string }[];
}
