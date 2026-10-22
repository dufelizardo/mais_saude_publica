/** Modelo operacional das unidades — ADR-0117 (fatia MO1 da ADR-0115). */

import { TipoUnidadeDeSaude } from './unidade-saude';

export interface PerfilOperacionalDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
  capacidades: number;
  unidades: number;
}

export interface PerfilOperacionalRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

export interface CapacidadeAssistencialDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
  /** Nulos: transversal. */
  perfilId?: string | null;
  perfilCodigo?: string | null;
  perfilNome?: string | null;
  unidades: number;
}

export interface CapacidadeAssistencialRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  perfilId?: string | null;
  ativo: boolean;
}

export interface PerfilDaUnidadeDto {
  /** Id da associação. */
  uuid: string;
  perfilId: string;
  codigo: string;
  nome: string;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface CapacidadeDaUnidadeDto {
  /** Id da associação. */
  uuid: string;
  capacidadeId: string;
  codigo: string;
  nome: string;
  perfilId?: string | null;
  perfilNome?: string | null;
  inicio: string;
  fim?: string | null;
  observacao?: string | null;
  motivoFim?: string | null;
}

export interface ModeloOperacionalUnidadeDto {
  unidadeId: string;
  unidadeNome: string;
  tipo: TipoUnidadeDeSaude;
  nivelDeGestao: boolean;
  perfis: PerfilDaUnidadeDto[];
  capacidades: CapacidadeDaUnidadeDto[];
  perfisEncerrados: PerfilDaUnidadeDto[];
  capacidadesEncerradas: CapacidadeDaUnidadeDto[];
}

/** Rótulo do grupo das capacidades sem perfil. */
export const GRUPO_TRANSVERSAL = 'Transversais';
