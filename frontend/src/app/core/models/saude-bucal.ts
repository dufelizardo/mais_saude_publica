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
