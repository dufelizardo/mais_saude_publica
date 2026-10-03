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
export type SituacaoLeito = 'LIVRE' | 'OCUPADO' | 'HIGIENIZACAO' | 'BLOQUEADO';
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
}

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
