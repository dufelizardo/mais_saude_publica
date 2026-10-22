/** Adscrição, cobertura e elegíveis — ADR-0128 (fatia F3b do Território). */

import { TipoEquipe } from './equipe';

export type OrigemAdscricao = 'TERRITORIO' | 'MANUAL';
export type SexoPaciente = 'MASCULINO' | 'FEMININO' | string;

export interface AdscricaoItemDto {
  uuid: string;
  equipeId: string;
  equipeNome: string;
  equipeTipo: TipoEquipe;
  unidadeId: string;
  unidadeNome: string;
  microareaCodigo?: string | null;
  origem: OrigemAdscricao;
  motivo?: string | null;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface AdscricaoDoPacienteDto {
  pacienteId: string;
  atual?: AdscricaoItemDto | null;
  historico: AdscricaoItemDto[];
  domicilio?: string | null;
  areaId?: string | null;
  areaCodigo?: string | null;
  microareaCodigo?: string | null;
  acsNome?: string | null;
}

export interface MicroareaCoberturaDto {
  uuid: string;
  codigo: string;
  populacaoReferencia?: number | null;
  pessoas: number;
  familias: number;
  imoveis: number;
  acsNome?: string | null;
  cobertura?: number | null;
}

export interface CoberturaDto {
  equipeId: string;
  equipeNome: string;
  areaId?: string | null;
  areaCodigo?: string | null;
  pessoasAdscritas: number;
  adscritasManual: number;
  familias: number;
  imoveis: number;
  microareasSemCadastro: number;
  microareas: MicroareaCoberturaDto[];
}

export interface ElegivelDto {
  pacienteId: string;
  nome: string;
  idade?: number | null;
  sexo?: SexoPaciente | null;
  microareaCodigo?: string | null;
  acsNome?: string | null;
}

export interface ElegiveisDto {
  programaId: string;
  equipeId: string;
  microareaId?: string | null;
  criteriosAvaliados: string[];
  criteriosNaoAvaliados: string[];
  semDataNascimento: number;
  elegiveis: ElegivelDto[];
}
