import { SituacaoOperacional } from './rede-unidades';
import { TipoUnidadeDeSaude } from './unidade-saude';

/** Estrutura de gestão (ADR-0175). */
export type TipoAreaGestao = 'GERENCIA' | 'COORDENACAO' | 'SUPERVISAO';
export type TemaGestao = 'ATENCAO_BASICA' | 'ESPECIALIDADES' | 'URGENCIA_EMERGENCIA' | 'VIGILANCIA' | 'SAUDE_MENTAL' | 'SAUDE_BUCAL' | 'ASSISTENCIA_FARMACEUTICA' | 'QUALIDADE' | 'GESTAO';
export type PapelTitularidade = 'TITULAR' | 'SUBSTITUTO';

export const TIPOS_AREA: Record<TipoAreaGestao, string> = { GERENCIA: 'Gerência', COORDENACAO: 'Coordenação', SUPERVISAO: 'Supervisão técnica' };
export const TEMAS_GESTAO: Record<TemaGestao, string> = {
  ATENCAO_BASICA: 'Atenção básica',
  ESPECIALIDADES: 'Especialidades',
  URGENCIA_EMERGENCIA: 'Urgência e emergência',
  VIGILANCIA: 'Vigilância em saúde',
  SAUDE_MENTAL: 'Saúde mental',
  SAUDE_BUCAL: 'Saúde bucal',
  ASSISTENCIA_FARMACEUTICA: 'Assistência farmacêutica',
  QUALIDADE: 'Qualidade',
  GESTAO: 'Gestão e planejamento',
};

export interface TitularidadeDto {
  uuid: string;
  profissionalId: string;
  profissionalNome: string;
  matricula: string;
  papel: PapelTitularidade;
  inicio: string;
  fim?: string | null;
  ato?: string | null;
}

export interface UnidadeDaAreaDto {
  uuid: string;
  nome: string;
  tipo: TipoUnidadeDeSaude;
  situacaoOperacional?: SituacaoOperacional | null;
  ativo: boolean;
}

export interface AreaGestaoDto {
  uuid: string;
  tipo: TipoAreaGestao;
  tema: TemaGestao;
  nome: string;
  sigla?: string | null;
  descricao?: string | null;
  superiorId?: string | null;
  superiorNome?: string | null;
  regionalId?: string | null;
  regionalNome?: string | null;
  ativa: boolean;
  titular?: TitularidadeDto | null;
  substituto?: TitularidadeDto | null;
  unidades: number;
  historico?: TitularidadeDto[] | null;
  unidadesDaRegional?: UnidadeDaAreaDto[] | null;
}

export interface AreaGestaoRequestDto {
  tipo: TipoAreaGestao;
  tema: TemaGestao;
  nome: string;
  sigla?: string | null;
  descricao?: string | null;
  superiorId?: string | null;
  regionalId?: string | null;
  ativa?: boolean;
}
