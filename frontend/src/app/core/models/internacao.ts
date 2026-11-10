/** Leitos e internação (#12) — ADRs 0098 e 0099. */

export type TipoLeito =
  | 'CLINICO'
  | 'CIRURGICO'
  | 'PEDIATRICO'
  | 'OBSTETRICO'
  | 'UTI_ADULTO'
  | 'UTI_PEDIATRICA'
  | 'UTI_NEONATAL'
  | 'OBSERVACAO'
  | 'ISOLAMENTO';
export type SexoLeito = 'MASCULINO' | 'FEMININO' | 'MISTO';
export type SituacaoLeito = 'LIVRE' | 'OCUPADO' | 'HIGIENIZACAO' | 'BLOQUEADO' | 'RESERVADO';
/** Central de regulação de leitos (ADR-0184). */
export type FaixaEtariaLeito = 'ADULTO' | 'PEDIATRICO' | 'NEONATAL';
export type PrioridadeLeito = 'VERMELHO' | 'AMARELO' | 'VERDE' | 'AZUL';
export type StatusSolicitacaoLeito = 'SOLICITADA' | 'RESERVADA' | 'ADMITIDA' | 'NEGADA' | 'CANCELADA';
export type TipoEventoSolicitacaoLeito = 'SOLICITACAO' | 'RESERVA' | 'VAGA_ZERO' | 'EXPIRACAO' | 'RECUSA_DESTINO' | 'NEGACAO' | 'CANCELAMENTO' | 'ADMISSAO';
export type CaraterInternacao = 'ELETIVA' | 'URGENCIA';
export type StatusInternacao = 'INTERNADO' | 'ALTA';
export type TipoAlta = 'MELHORADO' | 'A_PEDIDO' | 'TRANSFERENCIA' | 'EVASAO' | 'OBITO';
export type TipoEventoLeito = 'ADMISSAO' | 'TROCA_DE_LEITO' | 'ALTA' | 'HIGIENIZACAO_CONCLUIDA' | 'BLOQUEIO' | 'DESBLOQUEIO';

export interface LeitoRequestDto {
  unidadeId: string;
  setorId: string;
  identificacao: string;
  tipo: TipoLeito;
  sexo: SexoLeito;
  ativo?: boolean;
  faixaEtaria?: FaixaEtariaLeito;
}

export interface LeitoMapaDto {
  uuid: string;
  unidadeId: string;
  unidadeNome: string;
  setorId: string;
  setorNome: string;
  identificacao: string;
  tipo: TipoLeito;
  sexo: SexoLeito;
  situacao: SituacaoLeito;
  motivoBloqueio?: string | null;
  ativo: boolean;
  internacaoId?: string | null;
  pacienteId?: string | null;
  pacienteNome?: string | null;
  admitidaEm?: string | null;
  diasInternado?: number | null;
  previsaoAlta?: string | null;
  medicoResponsavelNome?: string | null;
  faixaEtaria: FaixaEtariaLeito;
  reservadoAte?: string | null;
  reservaSolicitacaoId?: string | null;
}

export interface IndicadoresLeitosDto {
  leitos: number;
  livres: number;
  ocupados: number;
  higienizacao: number;
  bloqueados: number;
  taxaOcupacao?: number | null;
  mediaPermanenciaDias?: number | null;
  altas30Dias: number;
}

export interface InternacaoRequestDto {
  pacienteId: string;
  atendimentoId?: string;
  leitoId: string;
  medicoMatricula: string;
  cid: string;
  motivo: string;
  carater: CaraterInternacao;
  previsaoAlta?: string;
  /** Leito de outra faixa etária: só com justificativa (ADR-0184). */
  justificativaFaixaEtaria?: string;
}

export interface SolicitacaoLeitoRequestDto {
  pacienteId: string;
  unidadeOrigemId: string;
  atendimentoId?: string;
  internacaoOrigemId?: string;
  profissionalMatricula: string;
  cid: string;
  quadroClinico: string;
  tipoLeito: TipoLeito;
  prioridade: PrioridadeLeito;
}

export interface EventoSolicitacaoLeitoDto {
  tipo: TipoEventoSolicitacaoLeito;
  texto?: string | null;
  profissionalNome?: string | null;
  ocorridoEm: string;
}

export interface SolicitacaoLeitoDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  unidadeOrigemId: string;
  unidadeOrigemNome: string;
  internacaoOrigemId?: string | null;
  atendimentoId?: string | null;
  ocorrenciaUrgenciaId?: string | null;
  solicitadoPorMatricula: string;
  solicitadoPorNome: string;
  cid: string;
  quadroClinico: string;
  tipoLeito: TipoLeito;
  prioridade: PrioridadeLeito;
  status: StatusSolicitacaoLeito;
  solicitadaEm: string;
  posicaoNaFila?: number | null;
  leitoReservadoId?: string | null;
  leitoReservadoIdentificacao?: string | null;
  unidadeDestinoId?: string | null;
  unidadeDestinoNome?: string | null;
  reservadoAte?: string | null;
  vagaZero: boolean;
  justificativaVagaZero?: string | null;
  transporte?: string | null;
  motivoEncerramento?: string | null;
  internacaoDestinoId?: string | null;
  eventos: EventoSolicitacaoLeitoDto[];
}

export interface VagaLeitoDto {
  leitoId: string;
  identificacao: string;
  unidadeId: string;
  unidadeNome: string;
  setorNome: string;
  tipo: TipoLeito;
  sexo: SexoLeito;
  faixaEtaria: FaixaEtariaLeito;
}

export interface ReservaLeitoRequestDto {
  profissionalMatricula: string;
  leitoId?: string;
  unidadeDestinoId?: string;
  justificativaVagaZero?: string;
  horas?: number;
  transporte?: string;
}

export interface AdmissaoRegulacaoLeitoRequestDto {
  medicoMatricula: string;
  leitoId?: string;
  previsaoAlta?: string;
  justificativaFaixaEtaria?: string;
}

export const FAIXAS_ETARIAS: Record<FaixaEtariaLeito, string> = { ADULTO: 'Adulto', PEDIATRICO: 'Pediátrico', NEONATAL: 'Neonatal' };

export const PRIORIDADES_LEITO: Record<PrioridadeLeito, { rotulo: string; classe: string }> = {
  VERMELHO: { rotulo: 'Vermelho · emergência', classe: 'alert' },
  AMARELO: { rotulo: 'Amarelo · urgente', classe: 'warn' },
  VERDE: { rotulo: 'Verde · não urgente', classe: 'ok' },
  AZUL: { rotulo: 'Azul · eletivo', classe: 'info' },
};

export const STATUS_SOLICITACAO_LEITO: Record<StatusSolicitacaoLeito, { rotulo: string; classe: string }> = {
  SOLICITADA: { rotulo: 'Aguardando vaga', classe: 'warn' },
  RESERVADA: { rotulo: 'Leito reservado', classe: 'info' },
  ADMITIDA: { rotulo: 'Admitido', classe: 'ok' },
  NEGADA: { rotulo: 'Negado', classe: 'muted' },
  CANCELADA: { rotulo: 'Cancelado', classe: 'muted' },
};

export const EVENTOS_SOLICITACAO_LEITO: Record<TipoEventoSolicitacaoLeito, string> = {
  SOLICITACAO: 'Pedido de leito', RESERVA: 'Leito reservado', VAGA_ZERO: 'Vaga zero', EXPIRACAO: 'Reserva vencida',
  RECUSA_DESTINO: 'Recusado no destino', NEGACAO: 'Negado pela central', CANCELAMENTO: 'Cancelado pela origem', ADMISSAO: 'Admitido no destino',
};

export interface EventoLeitoDto {
  tipo: TipoEventoLeito;
  leitoId: string;
  leitoIdentificacao: string;
  texto?: string | null;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
  ocorridoEm: string;
}

export interface InternacaoResponseDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  atendimentoId?: string | null;
  unidadeId: string;
  unidadeNome: string;
  leitoId: string;
  leitoIdentificacao: string;
  setorNome: string;
  medicoResponsavelMatricula: string;
  medicoResponsavelNome: string;
  cid: string;
  motivo?: string | null;
  carater: CaraterInternacao;
  admitidaEm: string;
  previsaoAlta?: string | null;
  status: StatusInternacao;
  altaEm?: string | null;
  tipoAlta?: TipoAlta | null;
  sumarioAlta?: string | null;
  altaPorNome?: string | null;
  diasPermanencia: number;
  eventos?: EventoLeitoDto[] | null;
  /** Internação psiquiátrica (ADR-0123). */
  modalidade?: ModalidadeInternacao | null;
  ordemJudicial?: string | null;
  comunicacoesMp?: ComunicacaoMpDto[];
  comunicacaoMpPendente?: MomentoComunicacaoMp | null;
  prazoComunicacaoMp?: string | null;
  comunicacaoMpVencida?: boolean;
}

export type ModalidadeInternacao = 'VOLUNTARIA' | 'INVOLUNTARIA' | 'COMPULSORIA';
export type MomentoComunicacaoMp = 'ADMISSAO' | 'ALTA';

export const MODALIDADES_INTERNACAO: Record<ModalidadeInternacao, string> = {
  VOLUNTARIA: 'Voluntária',
  INVOLUNTARIA: 'Involuntária',
  COMPULSORIA: 'Compulsória (ordem judicial)',
};

export interface ComunicacaoMpDto {
  uuid: string;
  momento: MomentoComunicacaoMp;
  comunicadaEm: string;
  responsavelNome?: string | null;
  protocolo?: string | null;
  observacao?: string | null;
}

export const TIPOS_LEITO: Record<TipoLeito, string> = {
  CLINICO: 'Clínico',
  CIRURGICO: 'Cirúrgico',
  PEDIATRICO: 'Pediátrico',
  OBSTETRICO: 'Obstétrico',
  UTI_ADULTO: 'UTI adulto',
  UTI_PEDIATRICA: 'UTI pediátrica',
  UTI_NEONATAL: 'UTI neonatal',
  OBSERVACAO: 'Observação',
  ISOLAMENTO: 'Isolamento',
};

export const SEXOS_LEITO: Record<SexoLeito, string> = { MASCULINO: 'Masculino', FEMININO: 'Feminino', MISTO: 'Misto' };

export const SITUACOES_LEITO: Record<SituacaoLeito, { rotulo: string; classe: string }> = {
  LIVRE: { rotulo: 'Livre', classe: 'ok' },
  OCUPADO: { rotulo: 'Ocupado', classe: 'info' },
  HIGIENIZACAO: { rotulo: 'Em higienização', classe: 'warn' },
  BLOQUEADO: { rotulo: 'Bloqueado', classe: 'muted' },
  RESERVADO: { rotulo: 'Reservado', classe: 'purple' },
};

export const TIPOS_ALTA: Record<TipoAlta, string> = {
  MELHORADO: 'Melhorado',
  A_PEDIDO: 'A pedido',
  TRANSFERENCIA: 'Transferência para outra unidade',
  EVASAO: 'Evasão',
  OBITO: 'Óbito',
};

export const EVENTOS_LEITO: Record<TipoEventoLeito, string> = {
  ADMISSAO: 'Internação',
  TROCA_DE_LEITO: 'Troca de leito',
  ALTA: 'Alta',
  HIGIENIZACAO_CONCLUIDA: 'Higienização concluída',
  BLOQUEIO: 'Leito bloqueado',
  DESBLOQUEIO: 'Leito desbloqueado',
};
