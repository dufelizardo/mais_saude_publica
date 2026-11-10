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

/** Laudo para solicitação de AIH e autorização interna (ADR-0186). */
export type StatusLaudoAih = 'SOLICITADO' | 'DEVOLVIDO' | 'AUTORIZADO' | 'NEGADO' | 'CANCELADO';
export type ClinicaAih =
  | 'CIRURGICA' | 'OBSTETRICA' | 'CLINICA_MEDICA' | 'CUIDADOS_PROLONGADOS' | 'PSIQUIATRIA' | 'TISIOLOGIA' | 'PEDIATRIA'
  | 'REABILITACAO' | 'PSIQUIATRIA_HOSPITAL_DIA';
export type CausaExternaAih = 'ACIDENTE_DE_TRANSITO' | 'ACIDENTE_DE_TRABALHO_TIPICO' | 'ACIDENTE_DE_TRABALHO_TRAJETO' | 'OUTRA_CAUSA_EXTERNA';
export type TipoEventoLaudoAih = 'SOLICITACAO' | 'CORRECAO' | 'DEVOLUCAO' | 'AUTORIZACAO' | 'NEGACAO' | 'CANCELAMENTO' | 'VINCULO_INTERNACAO';

export interface LaudoAihRequestDto {
  internacaoId?: string;
  agendamentoId?: string;
  profissionalMatricula: string;
  sinaisSintomas: string;
  condicoesJustificativas: string;
  resultadosExames?: string;
  diagnosticoInicial: string;
  cidPrincipal: string;
  cidSecundario?: string;
  cidCausaAssociada?: string;
  procedimentoId: string;
  clinica: ClinicaAih;
  causaExterna?: CausaExternaAih | null;
}

export interface LaudoAihDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  pacienteCartaoSus?: string | null;
  unidadeId: string;
  unidadeNome: string;
  internacaoId?: string | null;
  agendamentoId?: string | null;
  carater: CaraterInternacao;
  solicitanteMatricula: string;
  solicitanteNome: string;
  sinaisSintomas: string;
  condicoesJustificativas: string;
  resultadosExames?: string | null;
  diagnosticoInicial: string;
  cidPrincipal: string;
  cidSecundario?: string | null;
  cidCausaAssociada?: string | null;
  procedimentoId: string;
  procedimentoCodigo: string;
  procedimentoNome: string;
  procedimentoSigtap?: string | null;
  clinica: ClinicaAih;
  causaExterna?: CausaExternaAih | null;
  status: StatusLaudoAih;
  solicitadoEm: string;
  numeroAih?: string | null;
  autorizadorNome?: string | null;
  decididoEm?: string | null;
  motivoDecisao?: string | null;
  eventos: { tipo: TipoEventoLaudoAih; texto?: string | null; profissionalNome?: string | null; ocorridoEm: string }[];
}

export interface FaixaAihDto {
  uuid: string;
  ano: number;
  inicio: number;
  fim: number;
  proximo: number;
  restantes: number;
  ativa: boolean;
  esgotada: boolean;
  primeiroNumero: string;
  ultimoNumero: string;
  registradaEm: string;
}

export interface PendenciaAihDto {
  internacaoId: string;
  pacienteId: string;
  pacienteNome: string;
  unidadeId: string;
  unidadeNome: string;
  leitoIdentificacao: string;
  admitidaEm: string;
  horasSemLaudo: number;
}

export const STATUS_LAUDO_AIH: Record<StatusLaudoAih, { rotulo: string; classe: string }> = {
  SOLICITADO: { rotulo: 'Aguardando autorização', classe: 'info' },
  DEVOLVIDO: { rotulo: 'Devolvido para correção', classe: 'warn' },
  AUTORIZADO: { rotulo: 'Autorizado', classe: 'ok' },
  NEGADO: { rotulo: 'Negado', classe: 'alert' },
  CANCELADO: { rotulo: 'Cancelado', classe: 'muted' },
};

export const CLINICAS_AIH: Record<ClinicaAih, string> = {
  CIRURGICA: 'Cirúrgica',
  OBSTETRICA: 'Obstétrica',
  CLINICA_MEDICA: 'Clínica médica',
  CUIDADOS_PROLONGADOS: 'Cuidados prolongados',
  PSIQUIATRIA: 'Psiquiatria',
  TISIOLOGIA: 'Tisiologia',
  PEDIATRIA: 'Pediatria',
  REABILITACAO: 'Reabilitação',
  PSIQUIATRIA_HOSPITAL_DIA: 'Psiquiatria em hospital-dia',
};

export const CAUSAS_EXTERNAS_AIH: Record<CausaExternaAih, string> = {
  ACIDENTE_DE_TRANSITO: 'Acidente de trânsito',
  ACIDENTE_DE_TRABALHO_TIPICO: 'Acidente de trabalho típico',
  ACIDENTE_DE_TRABALHO_TRAJETO: 'Acidente de trabalho no trajeto',
  OUTRA_CAUSA_EXTERNA: 'Outra causa externa',
};

export const EVENTOS_LAUDO_AIH: Record<TipoEventoLaudoAih, string> = {
  SOLICITACAO: 'Solicitado',
  CORRECAO: 'Corrigido',
  DEVOLUCAO: 'Devolvido',
  AUTORIZACAO: 'Autorizado',
  NEGACAO: 'Negado',
  CANCELAMENTO: 'Cancelado',
  VINCULO_INTERNACAO: 'Passou para a internação',
};

/** Internação eletiva agendada e censo diário (ADR-0185). */
export type StatusInternacaoAgendada = 'AGENDADA' | 'RESERVADA' | 'ADMITIDA' | 'FALTA' | 'CANCELADA';
export type TipoEventoInternacaoAgendada = 'AGENDAMENTO' | 'RESERVA' | 'REMARCACAO' | 'ADMISSAO' | 'FALTA' | 'CANCELAMENTO';

export interface AgendamentoInternacaoRequestDto {
  pacienteId: string;
  unidadeId: string;
  dataPrevista: string;
  tipoLeito: TipoLeito;
  medicoMatricula: string;
  cid: string;
  procedimento: string;
  previsaoDias?: number | null;
  solicitacaoRegulacaoId?: string;
}

export interface EletivaLeitoRequestDto {
  profissionalMatricula: string;
  leitoId?: string;
  dataPrevista?: string;
  previsaoAlta?: string;
  justificativaFaixaEtaria?: string;
  motivo?: string;
}

export interface InternacaoAgendadaDto {
  uuid: string;
  pacienteId: string;
  pacienteNome: string;
  unidadeId: string;
  unidadeNome: string;
  dataPrevista: string;
  tipoLeito: TipoLeito;
  medicoMatricula: string;
  medicoNome: string;
  cid: string;
  procedimento: string;
  previsaoDias?: number | null;
  solicitacaoRegulacaoId?: string | null;
  status: StatusInternacaoAgendada;
  leitoReservadoId?: string | null;
  leitoReservadoIdentificacao?: string | null;
  reservadoAte?: string | null;
  internacaoId?: string | null;
  motivoEncerramento?: string | null;
  aguardandoDesfecho: boolean;
  podeReservar: boolean;
  eventos: { tipo: TipoEventoInternacaoAgendada; texto?: string | null; profissionalNome?: string | null; ocorridoEm: string }[];
}

export interface PacienteDoCensoDto {
  internacaoId: string;
  pacienteNome: string;
  leitoIdentificacao: string;
  setorNome: string;
  diasInternado: number;
}

export interface CensoDiarioDto {
  uuid?: string | null;
  unidadeId: string;
  unidadeNome: string;
  data: string;
  versao?: number | null;
  fechado: boolean;
  leitosExistentes: number;
  leitosBloqueados: number;
  leitosOperacionais: number;
  internadosInicio: number;
  admissoes: number;
  altasMelhorado: number;
  altasAPedido: number;
  altasTransferencia: number;
  evasoes: number;
  obitos: number;
  transferenciasInternas: number;
  internadosFim: number;
  pacienteDia: number;
  taxaOcupacao?: number | null;
  mediaPermanencia?: number | null;
  fechadoEm?: string | null;
  reaberto: boolean;
  motivoReabertura?: string | null;
  pacientes: PacienteDoCensoDto[];
}

export interface PendenciaCensoDto {
  unidadeId: string;
  unidadeNome: string;
  data: string;
}

export const STATUS_ELETIVA: Record<StatusInternacaoAgendada, { rotulo: string; classe: string }> = {
  AGENDADA: { rotulo: 'Agendada', classe: 'info' },
  RESERVADA: { rotulo: 'Leito reservado', classe: 'purple' },
  ADMITIDA: { rotulo: 'Admitida', classe: 'ok' },
  FALTA: { rotulo: 'Falta', classe: 'warn' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export const EVENTOS_ELETIVA: Record<TipoEventoInternacaoAgendada, string> = {
  AGENDAMENTO: 'Agendada', RESERVA: 'Leito reservado', REMARCACAO: 'Remarcada', ADMISSAO: 'Admitida', FALTA: 'Falta', CANCELAMENTO: 'Cancelada',
};

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
  /** Número da AIH autorizada (ADR-0186). */
  numeroAih?: string | null;
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
