// ── Vigilância epidemiológica, fatia V1 (ADR-0124) ─────────────────────────────────────────────

export type PeriodicidadeNotificacao = 'IMEDIATA' | 'SEMANAL';
export type CriterioClassificacao = 'LABORATORIAL' | 'CLINICO_EPIDEMIOLOGICO' | 'CLINICO';
export type OrigemNotificacao = 'ATENDIMENTO' | 'INTERNACAO' | 'EXAME' | 'SAUDE_MENTAL' | 'BUSCA_ATIVA' | 'COMUNIDADE' | 'OUTRA';
export type SituacaoNotificacao = 'NOTIFICADA' | 'EM_INVESTIGACAO' | 'ENCERRADA';
export type ClassificacaoCaso = 'SUSPEITO' | 'PROVAVEL' | 'CONFIRMADO' | 'DESCARTADO' | 'INCONCLUSIVO';
export type EvolucaoCaso = 'CURA' | 'OBITO_PELO_AGRAVO' | 'OBITO_POR_OUTRA_CAUSA' | 'IGNORADA';
export type OrigemLocalCaso = 'DOMICILIO' | 'MARCADO';
export type TipoExposicaoContato = 'DOMICILIAR' | 'SEXUAL' | 'TRABALHO' | 'ESCOLA' | 'SERVICO_SAUDE' | 'COMUNITARIO' | 'OUTRO';
export type SituacaoContato = 'EM_ACOMPANHAMENTO' | 'LIBERADO' | 'SINTOMATICO' | 'VIROU_CASO' | 'PERDA_DE_SEGUIMENTO';

/** Contatos e local do caso (ADR-0130). */
export const TIPOS_EXPOSICAO: Record<TipoExposicaoContato, string> = {
  DOMICILIAR: 'Domiciliar', SEXUAL: 'Sexual', TRABALHO: 'Trabalho', ESCOLA: 'Escola ou creche', SERVICO_SAUDE: 'Serviço de saúde',
  COMUNITARIO: 'Comunitário', OUTRO: 'Outro',
};
export const SITUACOES_CONTATO: Record<SituacaoContato, { rotulo: string; classe: string }> = {
  EM_ACOMPANHAMENTO: { rotulo: 'Em acompanhamento', classe: 'info' },
  LIBERADO: { rotulo: 'Liberado', classe: 'ok' },
  SINTOMATICO: { rotulo: 'Sintomático', classe: 'warn' },
  VIROU_CASO: { rotulo: 'Virou caso', classe: 'alert' },
  PERDA_DE_SEGUIMENTO: { rotulo: 'Perda de seguimento', classe: 'muted' },
};

export interface ContatoDto {
  uuid: string;
  pacienteId?: string | null;
  nome?: string | null;
  telefone?: string | null;
  endereco?: string | null;
  tipoExposicao: TipoExposicaoContato;
  dataExposicao?: string | null;
  fimMonitoramento?: string | null;
  monitoramentoVencido: boolean;
  situacao: SituacaoContato;
  observacao?: string | null;
  notificacaoGeradaId?: string | null;
}

export interface ContatoRequestDto {
  pacienteId?: string | null;
  nome?: string | null;
  telefone?: string | null;
  endereco?: string | null;
  tipoExposicao: TipoExposicaoContato;
  dataExposicao?: string | null;
  situacao?: SituacaoContato | null;
  observacao?: string | null;
}

/** Surto e medidas de controle (ADR-0131). */
export type TipoSurto = 'COMUNITARIO' | 'INSTITUCIONAL';
export type TipoInstituicaoSurto = 'ESCOLA' | 'CRECHE' | 'SERVICO_SAUDE' | 'ILPI' | 'PRISIONAL' | 'ALOJAMENTO' | 'EMPRESA' | 'OUTRO';
export type SituacaoSurto = 'SUSPEITO' | 'EM_INVESTIGACAO' | 'CONFIRMADO' | 'DESCARTADO' | 'ENCERRADO';
export type TipoMedidaControle = 'BLOQUEIO_VACINAL' | 'ISOLAMENTO' | 'QUIMIOPROFILAXIA' | 'CONTROLE_VETORIAL' | 'BUSCA_ATIVA' | 'EDUCACAO_EM_SAUDE'
  | 'INTERDICAO' | 'OUTRA';
export type SituacaoMedidaControle = 'PLANEJADA' | 'EM_ANDAMENTO' | 'CONCLUIDA' | 'CANCELADA';

export const SITUACOES_SURTO: Record<SituacaoSurto, { rotulo: string; classe: string }> = {
  SUSPEITO: { rotulo: 'Suspeito', classe: 'warn' },
  EM_INVESTIGACAO: { rotulo: 'Em investigação', classe: 'purple' },
  CONFIRMADO: { rotulo: 'Confirmado', classe: 'alert' },
  DESCARTADO: { rotulo: 'Descartado', classe: 'muted' },
  ENCERRADO: { rotulo: 'Encerrado', classe: 'ok' },
};
export const TIPOS_INSTITUICAO: Record<TipoInstituicaoSurto, string> = {
  ESCOLA: 'Escola', CRECHE: 'Creche', SERVICO_SAUDE: 'Serviço de saúde', ILPI: 'Instituição de longa permanência (ILPI)',
  PRISIONAL: 'Unidade prisional', ALOJAMENTO: 'Alojamento ou abrigo', EMPRESA: 'Empresa', OUTRO: 'Outro',
};
export const TIPOS_MEDIDA: Record<TipoMedidaControle, string> = {
  BLOQUEIO_VACINAL: 'Bloqueio vacinal', ISOLAMENTO: 'Isolamento', QUIMIOPROFILAXIA: 'Quimioprofilaxia', CONTROLE_VETORIAL: 'Controle vetorial',
  BUSCA_ATIVA: 'Busca ativa', EDUCACAO_EM_SAUDE: 'Educação em saúde', INTERDICAO: 'Interdição ou recolhimento', OUTRA: 'Outra',
};
export const SITUACOES_MEDIDA: Record<SituacaoMedidaControle, { rotulo: string; classe: string }> = {
  PLANEJADA: { rotulo: 'Planejada', classe: 'info' },
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'purple' },
  CONCLUIDA: { rotulo: 'Concluída', classe: 'ok' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export interface MedidaControleDto {
  uuid: string;
  surtoId?: string | null;
  notificacaoId?: string | null;
  tipo: TipoMedidaControle;
  descricao: string;
  responsavelNome?: string | null;
  prazo?: string | null;
  vencida: boolean;
  situacao: SituacaoMedidaControle;
  resultado?: string | null;
  criadaEm: string;
  concluidaEm?: string | null;
}

export interface MedidaControleRequestDto {
  tipo: TipoMedidaControle;
  descricao: string;
  responsavelMatricula?: string | null;
  prazo?: string | null;
  situacao?: SituacaoMedidaControle | null;
  resultado?: string | null;
}

export interface EventoLinhaDoTempoDto {
  em: string;
  titulo: string;
  detalhe?: string | null;
  autor?: string | null;
}

export interface SurtoDto {
  uuid: string;
  agravoId: string;
  agravoCodigo: string;
  agravoNome: string;
  sensivel: boolean;
  unidadeId: string;
  unidadeNome: string;
  nome: string;
  tipo: TipoSurto;
  tipoInstituicao?: TipoInstituicaoSurto | null;
  nomeInstituicao?: string | null;
  enderecoInstituicao?: string | null;
  definicaoCaso?: string | null;
  inicio: string;
  fim?: string | null;
  situacao: SituacaoSurto;
  conclusao?: string | null;
  encerradoEm?: string | null;
  abertoEm: string;
  casos: number;
  confirmados: number;
  obitos: number;
  medidasAbertas: number;
  medidasVencidas: number;
  microareas: { microareaId: string; codigo: string; areaCodigo: string; geometria?: string | null; casos: number }[];
  listaCasos: { notificacaoId: string; pacienteNome: string; unidadeNome: string; dataPrimeirosSintomas?: string | null; dataNotificacao: string;
    situacao: SituacaoNotificacao; classificacao?: ClassificacaoCaso | null; evolucao?: string | null; microareaCodigo?: string | null;
    incluidoEm?: string | null }[];
  medidas: MedidaControleDto[];
  curva: { ano: number; semana: number; inicio: string; casos: number }[];
  linhaDoTempo: EventoLinhaDoTempoDto[];
}

export interface SurtosDto {
  abertos: number;
  confirmados: number;
  medidasVencidas: number;
  lista: SurtoDto[];
}

export interface SurtoRequestDto {
  agravoId: string;
  unidadeId: string;
  nome: string;
  tipo: TipoSurto;
  tipoInstituicao?: TipoInstituicaoSurto | null;
  nomeInstituicao?: string | null;
  enderecoInstituicao?: string | null;
  microareasIds?: string[];
  definicaoCaso?: string | null;
  inicio: string;
  fim?: string | null;
  notificacoesIds?: string[];
}

/** Mapa dos casos (ADR-0130). */
export interface MapaVigilanciaDto {
  de: string;
  ate: string;
  total: number;
  semLocal: number;
  pontosVisiveis: boolean;
  microareas: { microareaId: string; codigo: string; areaCodigo: string; geometria?: string | null; casos: number; doSurto?: boolean;
    notificacoesIds?: string[] }[];
  pontos: { notificacaoId: string; latitude: number; longitude: number; agravoCodigo: string; situacao: SituacaoNotificacao; classificacao?: ClassificacaoCaso | null }[];
  curva: { ano: number; semana: number; inicio: string; casos: number }[];
}

export type TipoCampoFicha = 'SIM_NAO' | 'TEXTO' | 'NUMERO' | 'DATA' | 'OPCAO' | 'MULTIPLA';

export const PERIODICIDADES_NOTIFICACAO: Record<PeriodicidadeNotificacao, string> = { IMEDIATA: 'Imediata (até 24 horas)', SEMANAL: 'Semanal' };
export const CRITERIOS: Record<CriterioClassificacao, string> = {
  LABORATORIAL: 'Laboratorial', CLINICO_EPIDEMIOLOGICO: 'Clínico-epidemiológico', CLINICO: 'Clínico',
};
export const ORIGENS: Record<OrigemNotificacao, string> = {
  ATENDIMENTO: 'Atendimento', INTERNACAO: 'Internação', EXAME: 'Exame', SAUDE_MENTAL: 'Saúde mental (crise)',
  BUSCA_ATIVA: 'Busca ativa', COMUNIDADE: 'Comunidade', OUTRA: 'Outra',
};
export const SITUACOES_NOTIFICACAO: Record<SituacaoNotificacao, { rotulo: string; classe: string }> = {
  NOTIFICADA: { rotulo: 'Notificada', classe: 'info' },
  EM_INVESTIGACAO: { rotulo: 'Em investigação', classe: 'warn' },
  ENCERRADA: { rotulo: 'Encerrada', classe: 'muted' },
};
export const CLASSIFICACOES: Record<ClassificacaoCaso, { rotulo: string; classe: string }> = {
  SUSPEITO: { rotulo: 'Suspeito', classe: 'info' },
  PROVAVEL: { rotulo: 'Provável', classe: 'warn' },
  CONFIRMADO: { rotulo: 'Confirmado', classe: 'alert' },
  DESCARTADO: { rotulo: 'Descartado', classe: 'ok' },
  INCONCLUSIVO: { rotulo: 'Inconclusivo', classe: 'muted' },
};
export const EVOLUCOES: Record<EvolucaoCaso, string> = {
  CURA: 'Cura', OBITO_PELO_AGRAVO: 'Óbito pelo agravo', OBITO_POR_OUTRA_CAUSA: 'Óbito por outra causa', IGNORADA: 'Ignorada',
};
export const TIPOS_CAMPO: Record<TipoCampoFicha, string> = {
  SIM_NAO: 'Sim/Não', TEXTO: 'Texto', NUMERO: 'Número', DATA: 'Data', OPCAO: 'Uma opção', MULTIPLA: 'Várias opções',
};

export interface CampoFichaDto {
  chave: string;
  rotulo: string;
  tipo: TipoCampoFicha;
  obrigatorio?: boolean | null;
  opcoes?: string[] | null;
}

export interface AgravoDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  cids: string[];
  compulsorio: boolean;
  periodicidade: PeriodicidadeNotificacao;
  prazoEncerramentoDias: number;
  criterios: CriterioClassificacao[];
  sensivel: boolean;
  ativo: boolean;
  fichaVersao?: number | null;
  ficha: CampoFichaDto[];
  periodoMonitoramentoContatoDias?: number | null;
}

export interface NotificacaoDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  agravoId: string;
  agravoCodigo: string;
  agravoNome: string;
  sensivel: boolean;
  periodicidade: PeriodicidadeNotificacao;
  unidadeId: string;
  unidadeNome: string;
  profissionalNome?: string | null;
  dataPrimeirosSintomas?: string | null;
  dataNotificacao: string;
  origem: OrigemNotificacao;
  criseId?: string | null;
  localProvavel?: string | null;
  respostas?: Record<string, unknown> | null;
  fichaVersao?: number | null;
  ficha: CampoFichaDto[];
  situacao: SituacaoNotificacao;
  prazoEncerramento: string;
  encerramentoAtrasado: boolean;
  classificacaoAtual?: ClassificacaoCaso | null;
  classificacaoFinal?: ClassificacaoCaso | null;
  evolucao?: EvolucaoCaso | null;
  encerradaEm?: string | null;
  aviso?: string | null;
  investigacao?: { investigadorNome?: string | null; iniciadaEm: string; exposicao?: string | null; localProvavelInfeccao?: string | null;
    medidas?: string | null; examesIds: string[] } | null;
  classificacoes: { classificacao: ClassificacaoCaso; criterio?: CriterioClassificacao | null; justificativa?: string | null; classificadaEm: string;
    responsavelNome?: string | null }[];
  latitude?: number | null;
  longitude?: number | null;
  origemLocal?: OrigemLocalCaso | null;
  microareaCodigo?: string | null;
  areaCodigo?: string | null;
  contatos: ContatoDto[];
  surtoId?: string | null;
  surtoNome?: string | null;
  medidas?: MedidaControleDto[];
  linhaDoTempo?: EventoLinhaDoTempoDto[];
}

export interface NotificacoesDto {
  abertas: number;
  emInvestigacao: number;
  encerramentoAtrasado: number;
  lista: NotificacaoDto[];
  contatosVencidos: number;
}

export interface SugestaoNotificacaoDto {
  origem: OrigemNotificacao;
  referenciaId: string;
  pacienteId: string;
  pacienteNome: string;
  unidadeId: string;
  unidadeNome: string;
  agravoId: string;
  agravoCodigo: string;
  agravoNome: string;
  cid?: string | null;
  detectadaEm: string;
}

export interface NotificacaoNegativaDto {
  uuid?: string | null;
  unidadeId: string;
  unidadeNome: string;
  ano: number;
  semana: number;
  inicio: string;
  fim: string;
  responsavelNome?: string | null;
  declaradaEm?: string | null;
}

export interface SemanasEpidemiologicasDto {
  anoAtual: number;
  semanaAtual: number;
  declaradas: NotificacaoNegativaDto[];
  pendentes: NotificacaoNegativaDto[];
}

export interface BoletimVigilanciaDto {
  linhas: { agravoCodigo: string; agravoNome: string; notificadas: number; emInvestigacao: number; encerradas: number; confirmadas: number; descartadas: number }[];
}

export interface NotificacaoRequestDto {
  pacienteId: string;
  agravoId: string;
  unidadeId: string;
  origem: OrigemNotificacao;
  dataPrimeirosSintomas?: string | null;
  dataNotificacao?: string | null;
  atendimentoId?: string | null;
  internacaoId?: string | null;
  pedidoExameId?: string | null;
  criseId?: string | null;
  localProvavel?: string | null;
  respostas?: Record<string, unknown>;
  latitude?: number | null;
  longitude?: number | null;
  contatoId?: string | null;
}

export interface AgravoRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  cids: string[];
  compulsorio: boolean;
  periodicidade: PeriodicidadeNotificacao;
  prazoEncerramentoDias: number;
  criterios: CriterioClassificacao[];
  sensivel: boolean;
  periodoMonitoramentoContatoDias?: number | null;
  ativo: boolean;
}
