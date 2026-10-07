/** Equipes de saúde — ADRs 0103 e 0104. */

export type TipoEquipe = 'ESF' | 'EAB' | 'ESB' | 'EMULTI' | 'ECR' | 'CAPS_MULTI';
export type FuncaoEquipe =
  | 'MEDICO'
  | 'ENFERMEIRO'
  | 'TECNICO_ENFERMAGEM'
  | 'AUXILIAR_ENFERMAGEM'
  | 'ACS'
  | 'CIRURGIAO_DENTISTA'
  | 'TECNICO_SAUDE_BUCAL'
  | 'AUXILIAR_SAUDE_BUCAL'
  | 'PSICOLOGO'
  | 'ASSISTENTE_SOCIAL'
  | 'OUTRO';

export interface EquipeResumoDto {
  uuid: string;
  nome: string;
  tipo: TipoEquipe;
  ine?: string | null;
  ativa: boolean;
  unidadeId: string;
  unidadeNome: string;
  microareas?: string | null;
  coordenadorNome?: string | null;
  membros: number;
  agentesComunitarios: number;
  completa: boolean;
  faltando: string[];
  nomesMembros: string[];
  equipesApoiadas: number;
}

export interface EquipesResponseDto {
  equipes: number;
  ativas: number;
  porTipo: Partial<Record<TipoEquipe, number>>;
  profissionaisVinculados: number;
  incompletas: number;
  lista: EquipeResumoDto[];
}

export interface MembroEquipeDto {
  uuid: string;
  matricula: string;
  nome: string;
  conselho?: string | null;
  funcao: FuncaoEquipe;
  microarea?: string | null;
  inicio: string;
  fim?: string | null;
  motivoSaida?: string | null;
  cargo?: string | null;
  jornadaSemanalHoras?: number | null;
  afastamento?: string | null;
  afastadoAte?: string | null;
}

export interface VinculoEquipeDto {
  uuid: string;
  nome: string;
  tipo: TipoEquipe;
  unidadeNome: string;
}

export interface EquipeResponseDto {
  resumo: EquipeResumoDto;
  coordenadorMatricula?: string | null;
  reuniaoDia?: string | null;
  reuniaoInicio?: string | null;
  reuniaoFim?: string | null;
  reuniaoLocal?: string | null;
  membros: MembroEquipeDto[];
  antigos: MembroEquipeDto[];
  apoiadas: VinculoEquipeDto[];
  apoiadaPor: VinculoEquipeDto[];
}

export interface EquipeRequestDto {
  unidadeId: string;
  tipo: TipoEquipe;
  nome: string;
  ine?: string;
  ativa?: boolean;
  microareas?: string;
  coordenadorMatricula?: string;
  reuniaoDia?: string;
  reuniaoInicio?: string;
  reuniaoFim?: string;
  reuniaoLocal?: string;
  apoiadasIds?: string[];
}

/** Rótulo, sigla do marcador e família de cor de cada tipo (marcador do cartão, como no protótipo). */
export const TIPOS_EQUIPE: Record<TipoEquipe, { rotulo: string; sigla: string; classe: string; descricao: string }> = {
  ESF: { rotulo: 'eSF · Saúde da Família', sigla: 'eSF', classe: '', descricao: 'Estratégia Saúde da Família' },
  ESB: { rotulo: 'eSB · Saúde Bucal', sigla: 'eSB', classe: 't-sb', descricao: 'Saúde Bucal' },
  EMULTI: { rotulo: 'eMulti', sigla: 'eMulti', classe: 't-nasf', descricao: 'Equipe Multiprofissional (antigo NASF-AB)' },
  CAPS_MULTI: { rotulo: 'CAPS multi', sigla: 'CAPS', classe: 't-caps', descricao: 'Equipe multiprofissional de CAPS' },
  EAB: { rotulo: 'eAP · Atenção Primária', sigla: 'eAP', classe: 't-eab', descricao: 'Equipe de Atenção Primária' },
  ECR: { rotulo: 'Consultório na Rua', sigla: 'CnR', classe: 't-cons', descricao: 'Consultório na Rua' },
};

export const FUNCOES_EQUIPE: Record<FuncaoEquipe, string> = {
  MEDICO: 'Médico',
  ENFERMEIRO: 'Enfermeiro',
  TECNICO_ENFERMAGEM: 'Técnico de enfermagem',
  AUXILIAR_ENFERMAGEM: 'Auxiliar de enfermagem',
  ACS: 'Agente comunitário (ACS)',
  CIRURGIAO_DENTISTA: 'Cirurgião-dentista',
  TECNICO_SAUDE_BUCAL: 'Técnico de saúde bucal',
  AUXILIAR_SAUDE_BUCAL: 'Auxiliar de saúde bucal',
  PSICOLOGO: 'Psicólogo',
  ASSISTENTE_SOCIAL: 'Assistente social',
  OUTRO: 'Outra função',
};

/** Quantos grupos a composição mínima tem, para a barra do cartão (ADR-0103). */
export const GRUPOS_COMPOSICAO: Record<TipoEquipe, number> = { ESF: 4, EAB: 2, ESB: 2, EMULTI: 0, ECR: 0, CAPS_MULTI: 0 };
