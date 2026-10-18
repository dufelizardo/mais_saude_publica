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
  /** Acompanhamentos vigentes (ADR-0119); com filtro de unidade ou equipe, só os dela. */
  inscritos: number;
  faltosos: number;
  atrasados: number;
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

// ── Acompanhamento programático (ADR-0119) ─────────────────────────────────────────────────────

export type SituacaoAcompanhamento = 'ATIVO' | 'FALTOSO' | 'CONCLUIDO' | 'ENCERRADO';
export type MotivoSaidaAcompanhamento = 'ALTA' | 'TRANSFERENCIA' | 'MUDANCA_DE_MUNICIPIO' | 'ABANDONO' | 'OBITO' | 'OUTRO';

export interface EventoAcompanhamentoDto {
  ocorridoEm: string;
  situacaoAnterior?: SituacaoAcompanhamento | null;
  situacao: SituacaoAcompanhamento;
  motivo?: string | null;
  registradoPorCpf?: string | null;
}

export interface AcompanhamentoDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  pacienteIdade?: number | null;
  programaId: string;
  programaCodigo: string;
  programaNome: string;
  sensivel: boolean;
  unidadeId: string;
  unidadeNome: string;
  equipeId?: string | null;
  equipeNome?: string | null;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
  inicio: string;
  fim?: string | null;
  situacao: SituacaoAcompanhamento;
  motivoSaida?: MotivoSaidaAcompanhamento | null;
  observacaoSaida?: string | null;
  ultimoAtendimento?: string | null;
  atrasado: boolean;
  avisoPublico?: string | null;
  eventos: EventoAcompanhamentoDto[];
}

export interface AcompanhamentosDoPacienteDto {
  pacienteId: string;
  acesso?: { base: string; descricao?: string; expiraEm?: string | null } | null;
  vigentes: AcompanhamentoDto[];
  encerrados: AcompanhamentoDto[];
  restritos: number;
}

export interface InscritosProgramaDto {
  programaId: string;
  inscritos: number;
  faltosos: number;
  atrasados: number;
  lista: AcompanhamentoDto[];
}

export interface ResumoAcompanhamentosDto {
  pacientes: number;
  acompanhamentos: number;
  faltosos: number;
}

export const SITUACOES_ACOMPANHAMENTO: Record<SituacaoAcompanhamento, { rotulo: string; classe: string }> = {
  ATIVO: { rotulo: 'Ativo', classe: 'ok' },
  FALTOSO: { rotulo: 'Faltoso', classe: 'alert' },
  CONCLUIDO: { rotulo: 'Concluído', classe: 'info' },
  ENCERRADO: { rotulo: 'Encerrado', classe: 'muted' },
};

export const MOTIVOS_SAIDA: Record<MotivoSaidaAcompanhamento, string> = {
  ALTA: 'Alta (metas atingidas)',
  TRANSFERENCIA: 'Transferência para outra unidade',
  MUDANCA_DE_MUNICIPIO: 'Mudança de município',
  ABANDONO: 'Abandono',
  OBITO: 'Óbito',
  OUTRO: 'Outro',
};

/** Cor do ícone do cartão pela posição da categoria (o protótipo usa uma cor por eixo). */
export const CORES_CATEGORIA = ['pg__icon--blue', 'pg__icon--ok', 'pg__icon--pink', 'pg__icon--purple', 'pg__icon--warn'];
