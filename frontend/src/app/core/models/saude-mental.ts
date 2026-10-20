import { MotivoSaidaAcompanhamento, PeriodicidadeAcompanhamento, SituacaoAcompanhamento } from './programa';
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
  ptsVersao?: number | null;
  ptsRevisarEm?: string | null;
  ptsRevisaoVencida: boolean;
  faltasSeguidas: number;
  pts: ProjetoTerapeuticoDto[];
  intervencoes: IntervencaoSaudeMentalDto[];
  redeApoio: PessoaRedeApoioDto[];
  encaminhamentos: EncaminhamentoSaudeMentalDto[];
  compartilhamentos: CompartilhamentoCuidadoDto[];
  transferencias: TransferenciaCuidadoDto[];
  casoAnteriorId?: string | null;
}

export interface CasosSaudeMentalDto {
  emAcompanhamento: number;
  faltosos: number;
  semAvaliacao: number;
  semPts: number;
  ptsARevisar: number;
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

// ── SM2: PTS, intervenções e rede de apoio (ADR-0121) ──────────────────────────────────────────

export type SituacaoObjetivoTerapeutico = 'EM_ANDAMENTO' | 'ALCANCADO' | 'NAO_ALCANCADO' | 'SUSPENSO';
export type SituacaoIntervencaoSaudeMental = 'REALIZADA' | 'NAO_COMPARECEU';
export type PapelRedeApoio = 'FAMILIAR' | 'CUIDADOR' | 'PESSOA_DE_REFERENCIA' | 'SERVICO' | 'OUTRO';
export type PendenciaSaudeMental = '' | 'SEM_AVALIACAO' | 'SEM_PTS' | 'PTS_A_REVISAR';

export const SITUACOES_OBJETIVO: Record<SituacaoObjetivoTerapeutico, { rotulo: string; classe: string }> = {
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'info' },
  ALCANCADO: { rotulo: 'Alcançado', classe: 'ok' },
  NAO_ALCANCADO: { rotulo: 'Não alcançado', classe: 'warn' },
  SUSPENSO: { rotulo: 'Suspenso', classe: 'alert' },
};

export const SITUACOES_INTERVENCAO: Record<SituacaoIntervencaoSaudeMental, { rotulo: string; classe: string }> = {
  REALIZADA: { rotulo: 'Realizada', classe: 'ok' },
  NAO_COMPARECEU: { rotulo: 'Não compareceu', classe: 'warn' },
};

export const PAPEIS_REDE: Record<PapelRedeApoio, string> = {
  FAMILIAR: 'Familiar',
  CUIDADOR: 'Cuidador',
  PESSOA_DE_REFERENCIA: 'Pessoa de referência',
  SERVICO: 'Serviço',
  OUTRO: 'Outro',
};

export interface TipoIntervencaoSaudeMentalDto {
  uuid: string;
  codigo: string;
  nome: string;
  ativo: boolean;
}

export interface ObjetivoTerapeuticoDto {
  uuid: string;
  descricao: string;
  meta?: string | null;
  prazo?: string | null;
  responsavelMatricula?: string | null;
  responsavelNome?: string | null;
  situacao: SituacaoObjetivoTerapeutico;
}

export interface IntervencaoPrevistaDto {
  uuid: string;
  tipoId: string;
  tipoNome: string;
  frequencia?: string | null;
  responsavelMatricula?: string | null;
  responsavelNome?: string | null;
  servico?: string | null;
}

export interface ProjetoTerapeuticoDto {
  uuid: string;
  versao: number;
  vigente: boolean;
  necessidades: string;
  periodicidadeRevisao: PeriodicidadeAcompanhamento;
  revisarEm: string;
  revisaoVencida: boolean;
  pactuadoComPessoa: boolean;
  pactuacaoObservacao?: string | null;
  sinteseRevisao?: string | null;
  elaboradoEm: string;
  profissionalNome?: string | null;
  objetivos: ObjetivoTerapeuticoDto[];
  intervencoesPrevistas: IntervencaoPrevistaDto[];
}

export interface IntervencaoSaudeMentalDto {
  uuid: string;
  tipoId: string;
  tipoNome: string;
  realizadaEm: string;
  situacao: SituacaoIntervencaoSaudeMental;
  profissionalNome?: string | null;
  compartilhadoComNome?: string | null;
  previstaId?: string | null;
  resumo: string;
  equipeMatriciadaNome?: string | null;
}

export interface PessoaRedeApoioDto {
  uuid: string;
  nome: string;
  papel: PapelRedeApoio;
  vinculo?: string | null;
  telefone?: string | null;
  observacao?: string | null;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface ProjetoTerapeuticoRequestDto {
  versaoBase?: number | null;
  necessidades: string;
  objetivos: { descricao: string; meta?: string | null; prazo?: string | null; responsavelMatricula?: string | null; situacao?: SituacaoObjetivoTerapeutico }[];
  intervencoesPrevistas: { tipoId: string; frequencia?: string | null; responsavelMatricula?: string | null; servico?: string | null }[];
  periodicidadeRevisao?: PeriodicidadeAcompanhamento | null;
  revisarEm?: string | null;
  pactuadoComPessoa: boolean;
  pactuacaoObservacao?: string | null;
  sinteseRevisao?: string | null;
}

// ── SM4: rede de atenção (ADR-0122) ────────────────────────────────────────────────────────────

export type SituacaoTransferenciaCuidado = 'PROPOSTA' | 'ACEITA' | 'RECUSADA' | 'CANCELADA';

export const SITUACOES_TRANSFERENCIA: Record<SituacaoTransferenciaCuidado, { rotulo: string; classe: string }> = {
  PROPOSTA: { rotulo: 'Aguardando o destino', classe: 'warn' },
  ACEITA: { rotulo: 'Aceita', classe: 'ok' },
  RECUSADA: { rotulo: 'Recusada', classe: 'alert' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

/** Situação da solicitação na Regulação, como na tela da Regulação (ADR-0088). */
export const STATUS_ENCAMINHAMENTO: Record<string, { rotulo: string; classe: string }> = {
  SOLICITADA: { classe: 'info', rotulo: 'Na fila' },
  DEVOLVIDA: { classe: 'warn', rotulo: 'Devolvida' },
  AUTORIZADA: { classe: 'ok', rotulo: 'Autorizada' },
  NEGADA: { classe: 'alert', rotulo: 'Negada' },
  AGENDADA: { classe: 'ok', rotulo: 'Agendada' },
  REALIZADA: { classe: 'purple', rotulo: 'Realizada' },
  FALTOU: { classe: 'alert', rotulo: 'Faltou' },
  CANCELADA: { classe: 'muted', rotulo: 'Cancelada' },
};

export const PRIORIDADES_ENCAMINHAMENTO: Record<string, string> = {
  VERMELHO: 'Vermelho · emergência',
  AMARELO: 'Amarelo · urgente',
  VERDE: 'Verde · não urgente',
  AZUL: 'Azul · eletivo',
};

export interface EncaminhamentoSaudeMentalDto {
  uuid: string;
  solicitacaoId: string;
  procedimentoNome: string;
  status: string;
  prioridade: string;
  solicitadoEm: string;
  unidadeExecutanteNome?: string | null;
  dataHoraPrevista?: string | null;
  contrarreferencia?: string | null;
}

export interface CompartilhamentoCuidadoDto {
  uuid: string;
  equipeId: string;
  equipeNome: string;
  unidadeNome: string;
  objetivo: string;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface TransferenciaCuidadoDto {
  uuid: string;
  casoId: string;
  pacienteId: string;
  pacienteNome: string;
  unidadeOrigemId: string;
  unidadeOrigemNome: string;
  unidadeDestinoId: string;
  unidadeDestinoNome: string;
  resumo: string;
  situacao: SituacaoTransferenciaCuidado;
  propostaEm: string;
  decididaEm?: string | null;
  motivoDecisao?: string | null;
  casoDestinoId?: string | null;
}

export interface TransferenciasCuidadoDto {
  recebidas: TransferenciaCuidadoDto[];
  enviadas: TransferenciaCuidadoDto[];
}
