import { MotivoSaidaAcompanhamento, SituacaoAcompanhamento } from './programa';
import { AcessoProntuarioDto } from './prontuario';

// ── Saúde Mental, fatia SM1 (ADR-0120) ─────────────────────────────────────────────────────────

export type RiscoInicialSaudeMental = 'SEM_RISCO_APARENTE' | 'BAIXO' | 'MODERADO' | 'ALTO';
export type DesfechoAcolhimentoSaudeMental =
  | 'ACOMPANHAMENTO_NA_UNIDADE'
  | 'CUIDADO_COMPARTILHADO'
  | 'ENCAMINHAMENTO'
  | 'CRISE'
  | 'ORIENTACAO_SEM_SEGUIMENTO';

export const RISCOS: Record<RiscoInicialSaudeMental, { rotulo: string; classe: string }> = {
  SEM_RISCO_APARENTE: { rotulo: 'Sem risco aparente', classe: 'ok' },
  BAIXO: { rotulo: 'Baixo', classe: 'info' },
  MODERADO: { rotulo: 'Moderado', classe: 'warn' },
  ALTO: { rotulo: 'Alto', classe: 'alert' },
};

export const DESFECHOS: Record<DesfechoAcolhimentoSaudeMental, string> = {
  ACOMPANHAMENTO_NA_UNIDADE: 'Acompanhamento na unidade',
  CUIDADO_COMPARTILHADO: 'Cuidado compartilhado',
  ENCAMINHAMENTO: 'Encaminhamento',
  CRISE: 'Crise',
  ORIENTACAO_SEM_SEGUIMENTO: 'Orientação, sem seguimento',
};

/** Desfechos que abrem o caso (ou entram no vigente). */
export const DESFECHOS_QUE_ACOMPANHAM: DesfechoAcolhimentoSaudeMental[] = ['ACOMPANHAMENTO_NA_UNIDADE', 'CUIDADO_COMPARTILHADO'];

export interface AcolhimentoSaudeMentalDto {
  uuid: string;
  pacienteId: string;
  unidadeId: string;
  unidadeNome: string;
  realizadoEm: string;
  conteudoLiberado: boolean;
  profissionalNome?: string | null;
  atendimentoId?: string | null;
  motivoProcura?: string | null;
  necessidadeImediata?: string | null;
  vulnerabilidade?: string | null;
  riscoInicial?: RiscoInicialSaudeMental | null;
  desfecho?: DesfechoAcolhimentoSaudeMental | null;
  encaminhamentoDestino?: string | null;
  acompanhamentoId?: string | null;
  aviso?: string | null;
}

export interface AvaliacaoSaudeMentalDto {
  uuid: string;
  realizadaEm: string;
  profissionalNome?: string | null;
  contextoHistoria: string;
  aspectosPsicossociais?: string | null;
  fatoresRisco?: string | null;
  fatoresProtecao?: string | null;
  necessidades: string;
  cid?: string | null;
}

export interface ReferenciaSaudeMentalDto {
  equipeId: string;
  equipeNome: string;
  tecnicoMatricula: string;
  tecnicoNome: string;
  inicio: string;
  fim?: string | null;
  motivo?: string | null;
}

export interface AcompanhamentoSaudeMentalDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  pacienteIdade?: number | null;
  unidadeId: string;
  unidadeNome: string;
  acompanhamentoProgramaticoId: string;
  situacao: SituacaoAcompanhamento;
  inicio: string;
  fim?: string | null;
  motivoSaida?: MotivoSaidaAcompanhamento | null;
  observacaoSaida?: string | null;
  equipeId?: string | null;
  equipeNome?: string | null;
  tecnicoMatricula?: string | null;
  tecnicoNome?: string | null;
  ultimaAvaliacaoEm?: string | null;
  conteudoLiberado: boolean;
  acesso?: AcessoProntuarioDto | null;
  referencias: ReferenciaSaudeMentalDto[];
  acolhimentos: AcolhimentoSaudeMentalDto[];
  avaliacoes: AvaliacaoSaudeMentalDto[];
}

export interface CasosSaudeMentalDto {
  emAcompanhamento: number;
  faltosos: number;
  semAvaliacao: number;
  lista: AcompanhamentoSaudeMentalDto[];
}

export interface AcolhimentoSaudeMentalRequestDto {
  pacienteId: string;
  unidadeId: string;
  motivoProcura: string;
  necessidadeImediata?: string | null;
  vulnerabilidade?: string | null;
  riscoInicial: RiscoInicialSaudeMental;
  desfecho: DesfechoAcolhimentoSaudeMental;
  encaminhamentoDestino?: string | null;
  equipeId?: string | null;
  tecnicoMatricula?: string | null;
}

export interface AvaliacaoSaudeMentalRequestDto {
  contextoHistoria: string;
  aspectosPsicossociais?: string | null;
  fatoresRisco?: string | null;
  fatoresProtecao?: string | null;
  necessidades: string;
  cid?: string | null;
}
