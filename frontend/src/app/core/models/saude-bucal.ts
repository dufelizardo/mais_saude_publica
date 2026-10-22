/** Especialidades e Saúde Bucal (ADR-0134). */

export interface EspecialidadeDto {
  uuid: string;
  codigo: string;
  nome: string;
  odontologica: boolean;
  ativa: boolean;
}

export interface EspecialidadeRequestDto {
  codigo: string;
  nome: string;
  odontologica?: boolean;
  ativa?: boolean;
}

export type RiscoSaudeBucal = 'BAIXO' | 'MEDIO' | 'ALTO';
export type NecessidadeTratamentoOdontologico = 'SEM_NECESSIDADE' | 'ELETIVA' | 'PRIORITARIA' | 'URGENCIA';

export const RISCOS_SAUDE_BUCAL: Record<RiscoSaudeBucal, { rotulo: string; classe: string }> = {
  BAIXO: { rotulo: 'Risco baixo', classe: 'ok' },
  MEDIO: { rotulo: 'Risco médio', classe: 'warn' },
  ALTO: { rotulo: 'Risco alto', classe: 'alert' },
};

export const NECESSIDADES_TRATAMENTO: Record<NecessidadeTratamentoOdontologico, string> = {
  SEM_NECESSIDADE: 'Sem necessidade de tratamento',
  ELETIVA: 'Tratamento eletivo',
  PRIORITARIA: 'Tratamento prioritário',
  URGENCIA: 'Urgência',
};

export interface AvaliacaoOdontologicaDto {
  uuid: string;
  atendimentoId: string;
  pacienteId: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  queixaPrincipal?: string | null;
  anamnese?: string | null;
  exameClinico?: string | null;
  diagnostico?: string | null;
  risco: RiscoSaudeBucal;
  necessidadeTratamento: NecessidadeTratamentoOdontologico;
  planoInicial?: string | null;
  observacoes?: string | null;
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  retificado: boolean;
  retificadoPorUuid?: string | null;
  registradoEm: string;
}

export interface AvaliacaoOdontologicaRequestDto {
  atendimentoId: string;
  profissionalMatricula: string;
  dataHora?: string | null;
  queixaPrincipal?: string | null;
  anamnese?: string | null;
  exameClinico?: string | null;
  diagnostico?: string | null;
  risco: RiscoSaudeBucal;
  necessidadeTratamento: NecessidadeTratamentoOdontologico;
  planoInicial?: string | null;
  observacoes?: string | null;
  motivoRetificacao?: string | null;
}

// ── Odontograma (ADR-0135) ─────────────────────────────────────────────────────────────────────

export type AlcanceCondicaoOdontologica = 'FACE' | 'DENTE' | 'AMBOS';
export type FaceDente = 'MESIAL' | 'DISTAL' | 'OCLUSAL' | 'INCISAL' | 'VESTIBULAR' | 'LINGUAL_PALATINA';

export const FACES_DENTE: Record<FaceDente, string> = {
  MESIAL: 'Mesial', DISTAL: 'Distal', OCLUSAL: 'Oclusal', INCISAL: 'Incisal', VESTIBULAR: 'Vestibular', LINGUAL_PALATINA: 'Lingual ou palatina',
};

export interface CondicaoOdontologicaDto {
  uuid: string;
  codigo: string;
  nome: string;
  alcance: AlcanceCondicaoOdontologica;
  cor: string;
  sigla: string;
  ativa: boolean;
}

export interface RegistroOdontogramaDto {
  uuid: string;
  dente: number;
  face?: FaceDente | null;
  condicaoId: string;
  condicaoCodigo: string;
  condicaoNome: string;
  cor: string;
  sigla: string;
  data: string;
  avaliacaoId?: string | null;
  atendimentoId?: string | null;
  profissionalNome?: string | null;
  observacao?: string | null;
  anulado: boolean;
  anuladoEm?: string | null;
  motivoAnulacao?: string | null;
  registradoEm: string;
  /** Origem procedimento (ADR-0136): qual procedimento mudou a condição. */
  procedimentoId?: string | null;
  procedimentoNome?: string | null;
  /** Origem prótese entregue (ADR-0139). */
  tratamentoProteticoId?: string | null;
}

export interface OdontogramaDto {
  pacienteId: string;
  data: string;
  dentes: { dente: number; condicaoDente?: RegistroOdontogramaDto | null; faces: { face: FaceDente; registro: RegistroOdontogramaDto }[] }[];
  registros: number;
}

export interface MarcacaoOdontogramaDto {
  dente: number;
  face?: FaceDente | null;
  condicaoId: string;
  observacao?: string | null;
}

// ── Plano de tratamento (ADR-0137) ────────────────────────────────────────────────────────────────

export type SituacaoPlanoTratamento = 'ABERTO' | 'CONCLUIDO' | 'ABANDONADO';
export type SituacaoItemPlanoTratamento = 'PLANEJADO' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'ENCAMINHADO' | 'CANCELADO';

export const SITUACOES_PLANO: Record<SituacaoPlanoTratamento, { rotulo: string; classe: string }> = {
  ABERTO: { rotulo: 'Em tratamento', classe: 'info' },
  CONCLUIDO: { rotulo: 'Tratamento concluído', classe: 'ok' },
  ABANDONADO: { rotulo: 'Abandonado', classe: 'muted' },
};

export const SITUACOES_ITEM_PLANO: Record<SituacaoItemPlanoTratamento, { rotulo: string; classe: string }> = {
  PLANEJADO: { rotulo: 'Planejado', classe: 'muted' },
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'warn' },
  CONCLUIDO: { rotulo: 'Concluído', classe: 'ok' },
  ENCAMINHADO: { rotulo: 'Encaminhado', classe: 'info' },
  CANCELADO: { rotulo: 'Cancelado', classe: 'muted' },
};

export interface ItemPlanoTratamentoDto {
  uuid: string;
  ordem: number;
  procedimentoCatalogoId: string;
  procedimentoNome: string;
  sigtap?: string | null;
  alcance: 'NENHUM' | 'DENTE' | 'FACE';
  exigeDentista: boolean;
  dente?: number | null;
  faces: FaceDente[];
  observacao?: string | null;
  situacao: SituacaoItemPlanoTratamento;
  procedimentoId?: string | null;
  procedimentoStatus?: 'AGENDADO' | 'REALIZADO' | 'CANCELADO' | null;
  solicitacaoRegulacaoId?: string | null;
  motivoSituacao?: string | null;
  situacaoAlteradaEm?: string | null;
  /** O encaminhamento pela Regulação (ADR-0138). */
  regulacaoProcedimentoNome?: string | null;
  regulacaoStatus?: string | null;
  regulacaoExecutanteNome?: string | null;
  contrarreferencia?: string | null;
}

export interface PlanoTratamentoDto {
  uuid: string;
  pacienteId: string;
  atendimentoId: string;
  avaliacaoId?: string | null;
  unidadeNome: string;
  profissionalMatricula: string;
  profissionalNome: string;
  diagnostico?: string | null;
  objetivo?: string | null;
  prioridade: NecessidadeTratamentoOdontologico;
  situacao: SituacaoPlanoTratamento;
  abertoEm: string;
  encerradoEm?: string | null;
  motivoEncerramento?: string | null;
  itens: ItemPlanoTratamentoDto[];
}

export interface ItemPlanoTratamentoRequestDto {
  procedimentoCatalogoId: string;
  dente?: number | null;
  faces?: FaceDente[];
  ordem?: number | null;
  observacao?: string | null;
}

export interface PlanoTratamentoRequestDto {
  atendimentoId: string;
  avaliacaoId?: string | null;
  profissionalMatricula: string;
  diagnostico?: string | null;
  objetivo?: string | null;
  prioridade?: NecessidadeTratamentoOdontologico | null;
  itens?: ItemPlanoTratamentoRequestDto[];
}

// ── Tratamento protético (ADR-0139) ───────────────────────────────────────────────────────────────

export type TipoProtese = 'TOTAL' | 'PARCIAL_REMOVIVEL' | 'UNITARIA';
export type ArcadaDentaria = 'SUPERIOR' | 'INFERIOR';
export type EtapaProtetica = 'PLANEJAMENTO' | 'MOLDAGEM' | 'PROVA' | 'LABORATORIO' | 'ENTREGA' | 'ACOMPANHAMENTO';
export type SituacaoTratamentoProtetico = 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';

export const TIPOS_PROTESE: Record<TipoProtese, string> = { TOTAL: 'Prótese total', PARCIAL_REMOVIVEL: 'Prótese parcial removível', UNITARIA: 'Prótese unitária (coroa)' };
export const ARCADAS: Record<ArcadaDentaria, string> = { SUPERIOR: 'Superior', INFERIOR: 'Inferior' };
export const ETAPAS_PROTETICAS: Record<EtapaProtetica, string> = {
  PLANEJAMENTO: 'Planejamento', MOLDAGEM: 'Moldagem', PROVA: 'Prova', LABORATORIO: 'Laboratório', ENTREGA: 'Entrega', ACOMPANHAMENTO: 'Acompanhamento',
};
export const SITUACOES_PROTESE: Record<SituacaoTratamentoProtetico, { rotulo: string; classe: string }> = {
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'info' },
  CONCLUIDO: { rotulo: 'Concluído', classe: 'ok' },
  CANCELADO: { rotulo: 'Cancelado', classe: 'muted' },
};

export interface TratamentoProteticoDto {
  uuid: string;
  pacienteId: string;
  atendimentoId: string;
  planoId?: string | null;
  unidadeNome: string;
  profissionalMatricula: string;
  profissionalNome: string;
  tipo: TipoProtese;
  arcada: ArcadaDentaria;
  dente?: number | null;
  laboratorioUnidadeId?: string | null;
  laboratorioNome?: string | null;
  laboratorioContratado: boolean;
  etapaAtual: EtapaProtetica;
  situacao: SituacaoTratamentoProtetico;
  dataEntrega?: string | null;
  observacao?: string | null;
  abertoEm: string;
  encerradoEm?: string | null;
  motivoCancelamento?: string | null;
  proximasEtapas: EtapaProtetica[];
  etapas: { uuid: string; etapa: EtapaProtetica; data: string; profissionalNome: string; observacao?: string | null; registradoEm: string }[];
}

export interface TratamentoProteticoRequestDto {
  atendimentoId: string;
  profissionalMatricula: string;
  tipo: TipoProtese;
  arcada?: ArcadaDentaria | null;
  dente?: number | null;
  laboratorioUnidadeId?: string | null;
  laboratorioExterno?: string | null;
  observacao?: string | null;
}

export interface EtapaProteticaRequestDto {
  etapa: EtapaProtetica;
  data?: string | null;
  profissionalMatricula: string;
  observacao?: string | null;
  laboratorioUnidadeId?: string | null;
  laboratorioExterno?: string | null;
}

// ── Saúde bucal coletiva (ADR-0140) ───────────────────────────────────────────────────────────────

export type AtividadeSaudeBucalColetiva = 'ESCOVACAO_SUPERVISIONADA' | 'APLICACAO_TOPICA_FLUOR' | 'EDUCACAO_EM_SAUDE' | 'AVALIACAO_COLETIVA' | 'LEVANTAMENTO_EPIDEMIOLOGICO';
export type LocalSaudeBucalColetiva = 'ESCOLA' | 'CRECHE' | 'UBS' | 'COMUNIDADE' | 'EMPRESA' | 'INSTITUICAO_LONGA_PERMANENCIA' | 'OUTRO';

export const ATIVIDADES_SAUDE_BUCAL: Record<AtividadeSaudeBucalColetiva, string> = {
  ESCOVACAO_SUPERVISIONADA: 'Escovação supervisionada',
  APLICACAO_TOPICA_FLUOR: 'Aplicação tópica de flúor',
  EDUCACAO_EM_SAUDE: 'Educação em saúde bucal',
  AVALIACAO_COLETIVA: 'Avaliação coletiva de risco',
  LEVANTAMENTO_EPIDEMIOLOGICO: 'Levantamento epidemiológico',
};

export const LOCAIS_SAUDE_BUCAL: Record<LocalSaudeBucalColetiva, string> = {
  ESCOLA: 'Escola', CRECHE: 'Creche', UBS: 'UBS', COMUNIDADE: 'Comunidade', EMPRESA: 'Empresa',
  INSTITUICAO_LONGA_PERMANENCIA: 'Instituição de longa permanência', OUTRO: 'Outro',
};

/** Os programas em que a ação coletiva ganha o detalhe de saúde bucal. */
export const PROGRAMAS_SAUDE_BUCAL = ['SAUDE_BUCAL', 'SAUDE_NA_ESCOLA'];

export interface AcaoSaudeBucalDto {
  acaoId: string;
  preenchido: boolean;
  atividades: AtividadeSaudeBucalColetiva[];
  localTipo?: LocalSaudeBucalColetiva | null;
  instituicao?: string | null;
  publicoAlvo?: string | null;
  kitsDistribuidos?: number | null;
  atualizadoEm?: string | null;
  avaliacoes: { pacienteId: string; pacienteNome: string; risco: RiscoSaudeBucal; necessidadeTratamento: NecessidadeTratamentoOdontologico; observacao?: string | null; registradoEm: string }[];
}

export interface AcaoSaudeBucalRequestDto {
  atividades: AtividadeSaudeBucalColetiva[];
  localTipo: LocalSaudeBucalColetiva;
  instituicao?: string | null;
  publicoAlvo?: string | null;
  kitsDistribuidos?: number | null;
}

export interface ConvocacaoSaudeBucalDto {
  pacienteId: string;
  pacienteNome: string;
  risco: RiscoSaudeBucal;
  necessidadeTratamento: NecessidadeTratamentoOdontologico;
  acaoId: string;
  programaNome: string;
  local?: string | null;
  realizadaEm?: string | null;
  avaliadoEm: string;
}
