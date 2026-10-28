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

// ── Ações programáticas (ADR-0132) ─────────────────────────────────────────────────────────────

export type TipoAcaoProgramatica = 'CONSULTA_PROGRAMADA' | 'AFERICAO' | 'EXAME' | 'VACINA' | 'VISITA_DOMICILIAR' | 'BUSCA_ATIVA' | 'GRUPO'
  | 'ORIENTACAO' | 'MOBILIZACAO' | 'OUTRA';
export type SituacaoAcaoProgramatica = 'PENDENTE' | 'REALIZADA' | 'NAO_REALIZADA' | 'CANCELADA';
export type ResultadoBuscaAtiva = 'ENCONTRADO_AGENDADO' | 'ENCONTRADO_RECUSOU' | 'NAO_ENCONTRADO' | 'MUDOU_DE_ENDERECO';

export const TIPOS_ACAO: Record<TipoAcaoProgramatica, string> = {
  CONSULTA_PROGRAMADA: 'Consulta programada', AFERICAO: 'Aferição', EXAME: 'Exame', VACINA: 'Vacina', VISITA_DOMICILIAR: 'Visita domiciliar',
  BUSCA_ATIVA: 'Busca ativa', GRUPO: 'Grupo ou atividade coletiva', ORIENTACAO: 'Orientação', MOBILIZACAO: 'Mobilização', OUTRA: 'Outra',
};
/** Só existem para uma pessoa (pedem o acompanhamento). */
export const ACOES_SO_INDIVIDUAIS: TipoAcaoProgramatica[] = ['CONSULTA_PROGRAMADA', 'BUSCA_ATIVA', 'VISITA_DOMICILIAR'];
export const SITUACOES_ACAO: Record<SituacaoAcaoProgramatica, { rotulo: string; classe: string }> = {
  PENDENTE: { rotulo: 'Pendente', classe: 'info' },
  REALIZADA: { rotulo: 'Realizada', classe: 'ok' },
  NAO_REALIZADA: { rotulo: 'Não realizada', classe: 'warn' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};
export const RESULTADOS_BUSCA: Record<ResultadoBuscaAtiva, string> = {
  ENCONTRADO_AGENDADO: 'Encontrado e agendado', ENCONTRADO_RECUSOU: 'Encontrado, recusou', NAO_ENCONTRADO: 'Não encontrado',
  MUDOU_DE_ENDERECO: 'Mudou de endereço',
};

export interface AcaoProgramaticaDto {
  uuid: string;
  programaId: string;
  programaCodigo: string;
  programaNome: string;
  sensivel: boolean;
  tipo: TipoAcaoProgramatica;
  coletiva: boolean;
  acompanhamentoId?: string | null;
  pacienteId?: string | null;
  pacienteNome?: string | null;
  unidadeId: string;
  unidadeNome: string;
  equipeId?: string | null;
  equipeNome?: string | null;
  local?: string | null;
  descricao?: string | null;
  responsavelMatricula?: string | null;
  responsavelNome?: string | null;
  dataPrevista: string;
  situacao: SituacaoAcaoProgramatica;
  vencida: boolean;
  realizadaEm?: string | null;
  atendimentoId?: string | null;
  agendamentoId?: string | null;
  agendamentoDataHora?: string | null;
  prontaParaConfirmar: boolean;
  atendimentoSugeridoId?: string | null;
  motivo?: string | null;
  resultadoBusca?: ResultadoBuscaAtiva | null;
  sugestao?: string | null;
  observacao?: string | null;
  participantes: { pacienteId: string; nome: string }[];
  criadaEm: string;
  /** Ação da campanha (ADR-0133). */
  campanhaId?: string | null;
  campanhaNome?: string | null;
  localCampanhaId?: string | null;
  localNome?: string | null;
  quantidadeAtendida?: number | null;
}

export interface AcoesProgramaticasDto {
  de: string;
  ate: string;
  pendentes: number;
  vencidas: number;
  realizadasNoMes: number;
  prontasParaConfirmar: number;
  lista: AcaoProgramaticaDto[];
}

export interface AcaoProgramaticaRequestDto {
  /** Do programa, ou da campanha (ADR-0133): um dos dois. */
  programaId?: string | null;
  campanhaId?: string | null;
  localCampanhaId?: string | null;
  tipo: TipoAcaoProgramatica;
  acompanhamentoId?: string | null;
  unidadeId?: string | null;
  equipeId?: string | null;
  local?: string | null;
  participantesIds?: string[];
  descricao?: string | null;
  responsavelMatricula?: string | null;
  dataPrevista: string;
}

export interface RealizacaoAcaoRequestDto {
  realizadaEm?: string | null;
  atendimentoId?: string | null;
  resultadoBusca?: ResultadoBuscaAtiva | null;
  participantesIds?: string[] | null;
  observacao?: string | null;
  quantidadeAtendida?: number | null;
}

// ── Campanhas (ADR-0133) ───────────────────────────────────────────────────────────────────────

export type TipoCampanha = 'VACINACAO' | 'RASTREAMENTO' | 'CONSCIENTIZACAO' | 'MUTIRAO' | 'OUTRA';
export type SituacaoCampanha = 'PLANEJADA' | 'EM_ANDAMENTO' | 'ENCERRADA' | 'CANCELADA';

export const TIPOS_CAMPANHA: Record<TipoCampanha, string> = {
  VACINACAO: 'Vacinação', RASTREAMENTO: 'Rastreamento ou prevenção', CONSCIENTIZACAO: 'Conscientização', MUTIRAO: 'Mutirão', OUTRA: 'Outra',
};
export const SITUACOES_CAMPANHA: Record<SituacaoCampanha, { rotulo: string; classe: string }> = {
  PLANEJADA: { rotulo: 'Planejada', classe: 'info' },
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'ok' },
  ENCERRADA: { rotulo: 'Encerrada', classe: 'muted' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'alert' },
};

export interface LocalCampanhaDto {
  uuid: string;
  temporario: boolean;
  unidadeId: string;
  unidadeNome: string;
  nome: string;
  endereco?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  funcionamento?: string | null;
  inicio?: string | null;
  fim?: string | null;
}

export interface CampanhaDto {
  uuid: string;
  nome: string;
  descricao?: string | null;
  tipo: TipoCampanha;
  programaId?: string | null;
  programaNome?: string | null;
  inicio: string;
  fim: string;
  situacao: SituacaoCampanha;
  publico: string;
  idadeMinima?: number | null;
  idadeMaxima?: number | null;
  sexo?: SexoPublico | null;
  gestante: boolean;
  condicoes: string[];
  publicoDescricao?: string | null;
  metaDescricao?: string | null;
  metaQuantidade?: number | null;
  realizado: number;
  publicada: boolean;
  canceladaEm?: string | null;
  motivoCancelamento?: string | null;
  acoesPendentes: number;
  acoesRealizadas: number;
  locais: LocalCampanhaDto[];
  /** As vacinas da campanha de vacinação; vazio = qualquer uma (ADR-0149). */
  imunobiologicoIds: string[];
  imunobiologicoSiglas: string[];
}

export interface CampanhaRequestDto {
  nome: string;
  descricao?: string | null;
  tipo: TipoCampanha;
  programaId?: string | null;
  inicio: string;
  fim: string;
  idadeMinima?: number | null;
  idadeMaxima?: number | null;
  sexo?: SexoPublico | null;
  gestante?: boolean;
  condicoes?: string[];
  publicoDescricao?: string | null;
  metaDescricao?: string | null;
  metaQuantidade?: number | null;
  imunobiologicoIds?: string[];
}

export interface LocalCampanhaRequestDto {
  unidadeId: string;
  temporario?: boolean;
  nome?: string | null;
  endereco?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  funcionamento?: string | null;
  inicio?: string | null;
  fim?: string | null;
}

export interface CampanhaPublicaDto {
  nome: string;
  tipo: TipoCampanha;
  descricao?: string | null;
  publico: string;
  inicio: string;
  fim: string;
  situacao: SituacaoCampanha;
  meta?: string | null;
  metaQuantidade?: number | null;
  realizado: number;
  locais: { nome: string; endereco?: string | null; funcionamento?: string | null; latitude?: number | null; longitude?: number | null }[];
}

// ── Metas e indicadores programáticos (P6, ADR-0141): só números ──

export type IndicadorProgramatico =
  | 'ACOMPANHADOS_EM_DIA'
  | 'COBERTURA'
  | 'ACOES_REALIZADAS'
  | 'PA_CONTROLADA'
  | 'EXAME_NO_ALVO'
  | 'PRE_NATAL_6_CONSULTAS'
  | 'TRATAMENTO_ODONTOLOGICO_CONCLUIDO'
  | 'PROTESES_ENTREGUES'
  | 'ESCOVACAO_SUPERVISIONADA';

export type TipoValorIndicador = 'PERCENTUAL' | 'QUANTIDADE';
export type SituacaoMetaProgramatica = 'ATINGIDA' | 'EM_RISCO' | 'ABAIXO' | 'SEM_DADOS';

export const SITUACOES_META: Record<SituacaoMetaProgramatica, { rotulo: string; classe: string }> = {
  ATINGIDA: { rotulo: 'Meta atingida', classe: 'ok' },
  EM_RISCO: { rotulo: 'Em risco', classe: 'warn' },
  ABAIXO: { rotulo: 'Abaixo da meta', classe: 'alert' },
  SEM_DADOS: { rotulo: 'Sem dados', classe: 'info' },
};

export interface IndicadorApuradoDto {
  indicador: IndicadorProgramatico;
  nome: string;
  descricao: string;
  tipo: TipoValorIndicador;
  aplicavel: boolean;
  motivo?: string | null;
  numerador: number;
  denominador?: number | null;
  valor?: number | null;
  de: string;
  ate: string;
  metaId?: string | null;
  alvo?: number | null;
  situacao?: SituacaoMetaProgramatica | null;
}

export interface SerieIndicadorDto {
  indicador: IndicadorProgramatico;
  nome: string;
  aplicavel: boolean;
  motivo?: string | null;
  pontos: { mes: string; numerador: number; denominador?: number | null; valor?: number | null }[];
}

export interface MetaProgramaticaDto {
  uuid: string;
  programaId: string;
  indicador: IndicadorProgramatico;
  indicadorNome: string;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  alvo: number;
  inicio: string;
  fim: string;
  exameId?: string | null;
  exameNome?: string | null;
  limiteMinimo?: number | null;
  limiteMaximo?: number | null;
}

export interface MetaProgramaticaRequestDto {
  indicador: IndicadorProgramatico;
  unidadeId?: string | null;
  alvo: number;
  inicio: string;
  fim: string;
  exameId?: string | null;
  limiteMinimo?: number | null;
  limiteMaximo?: number | null;
}

export interface FiltroIndicadores {
  de?: string;
  ate?: string;
  unidadeId?: string;
}
