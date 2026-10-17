/** Programas de saúde — ADR-0118 (fatia P1 da ADR-0109). */

export type AbrangenciaPrograma = 'NACIONAL' | 'ESTADUAL' | 'MUNICIPAL';
export type SituacaoPrograma = 'ATIVO' | 'SUSPENSO' | 'ENCERRADO';
export type PeriodicidadeAcompanhamento = 'MENSAL' | 'BIMESTRAL' | 'TRIMESTRAL' | 'QUADRIMESTRAL' | 'SEMESTRAL' | 'ANUAL' | 'SEM_PERIODICIDADE';
export type SexoPublico = 'MASCULINO' | 'FEMININO';

export interface CategoriaProgramaDto {
  uuid: string;
  nome: string;
  ordem: number;
  ativa: boolean;
  programas: number;
}

export interface ProgramaResumoDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  categoriaId: string;
  categoriaNome: string;
  abrangencia: AbrangenciaPrograma;
  situacao: SituacaoPrograma;
  inicio?: string | null;
  fim?: string | null;
  coordenadorMatricula?: string | null;
  coordenadorNome?: string | null;
  periodicidade: PeriodicidadeAcompanhamento;
  sensivel: boolean;
  idadeMinima?: number | null;
  idadeMaxima?: number | null;
  sexo?: SexoPublico | null;
  gestante: boolean;
  condicoes: string[];
  publico: string;
  unidades: number;
}

export interface ParticipacaoProgramaDto {
  uuid: string;
  unidadeId: string;
  unidadeNome: string;
  equipeId?: string | null;
  equipeNome?: string | null;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface ProgramaDto {
  resumo: ProgramaResumoDto;
  participacoes: ParticipacaoProgramaDto[];
  participacoesEncerradas: ParticipacaoProgramaDto[];
}

export interface ProgramaRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  categoriaId: string;
  abrangencia: AbrangenciaPrograma;
  situacao: SituacaoPrograma;
  inicio?: string | null;
  fim?: string | null;
  coordenadorMatricula?: string | null;
  periodicidade: PeriodicidadeAcompanhamento;
  sensivel: boolean;
  idadeMinima?: number | null;
  idadeMaxima?: number | null;
  sexo?: SexoPublico | null;
  gestante: boolean;
  condicoes: string[];
}

export interface ProgramaPublicoDto {
  nome: string;
  categoria: string;
  descricao?: string | null;
  publico: string;
  unidades: string[];
}

export const ABRANGENCIAS: Record<AbrangenciaPrograma, string> = { NACIONAL: 'Nacional', ESTADUAL: 'Estadual', MUNICIPAL: 'Municipal' };

export const SITUACOES_PROGRAMA: Record<SituacaoPrograma, { rotulo: string; classe: string }> = {
  ATIVO: { rotulo: 'Ativo', classe: 'ok' },
  SUSPENSO: { rotulo: 'Suspenso', classe: 'warn' },
  ENCERRADO: { rotulo: 'Encerrado', classe: 'muted' },
};

export const PERIODICIDADES: Record<PeriodicidadeAcompanhamento, string> = {
  MENSAL: 'Mensal',
  BIMESTRAL: 'Bimestral',
  TRIMESTRAL: 'Trimestral',
  QUADRIMESTRAL: 'Quadrimestral',
  SEMESTRAL: 'Semestral',
  ANUAL: 'Anual',
  SEM_PERIODICIDADE: 'Sem periodicidade',
};

/** Cor do ícone do cartão pela posição da categoria (o protótipo usa uma cor por eixo). */
export const CORES_CATEGORIA = ['pg__icon--blue', 'pg__icon--ok', 'pg__icon--pink', 'pg__icon--purple', 'pg__icon--warn'];
