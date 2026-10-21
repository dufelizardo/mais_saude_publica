import { FaceDente } from './saude-bucal';

export type StatusProcedimento = 'AGENDADO' | 'REALIZADO' | 'CANCELADO';

/** O que o item do catálogo pede de complemento odontológico (ADR-0136). */
export type AlcanceProcedimento = 'NENHUM' | 'DENTE' | 'FACE';

/** Item do catálogo geral de procedimentos (ADR-0136): de todas as especialidades, com o SIGTAP opcional. */
export interface ProcedimentoCatalogoDto {
  uuid: string;
  codigo: string;
  nome: string;
  especialidadeId: string | null;
  especialidadeNome: string | null;
  odontologico: boolean;
  sigtap: string | null;
  alcance: AlcanceProcedimento;
  condicaoResultanteId: string | null;
  condicaoResultanteNome: string | null;
  exigeDentista: boolean;
  ativo: boolean;
}

export interface ProcedimentoCatalogoRequestDto {
  codigo: string;
  nome: string;
  especialidadeId?: string | null;
  sigtap?: string | null;
  alcance?: AlcanceProcedimento;
  condicaoResultanteId?: string | null;
  exigeDentista?: boolean;
  ativo?: boolean;
}

/** Desfecho de um procedimento agendado, uma única vez (ADR-0062). */
export interface StatusProcedimentoRequestDto {
  status: 'REALIZADO' | 'CANCELADO';
  profissionalMatricula: string;
  dataRealizacao?: string;
  justificativa?: string;
}

export interface ProcedimentoRequestDto {
  /** A consulta; sem ela, o atendimento (ADR-0136). */
  consultaId?: string | null;
  atendimentoId?: string | null;
  /** O item do catálogo; sem ele, o tipo em texto livre. */
  procedimentoCatalogoId?: string | null;
  profissionalMatricula: string;
  tipo?: string;
  /** Complemento odontológico, quando o item pede. */
  dente?: number | null;
  faces?: FaceDente[];
  descricao?: string;
  dataRealizacao: string;
  status: StatusProcedimento;
}

export interface ProcedimentoResponseDto {
  uuid: string;
  consultaUuid: string | null;
  profissionalMatricula: string;
  profissionalNome: string;
  tipo: string;
  descricao?: string;
  dataRealizacao: string;
  status: StatusProcedimento;
  /** Retificação (ADR-0062): versão que esta corrige, motivo, e se outra versão já corrige esta. */
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  registradoEm?: string | null;
  registradoPorCpf?: string | null;
  retificado?: boolean;
  retificadoPorUuid?: string | null;
  /** Desfecho de um procedimento agendado (ADR-0062). */
  dataPrevista?: string | null;
  statusAlteradoEm?: string | null;
  profissionalStatusMatricula?: string | null;
  profissionalStatusNome?: string | null;
  justificativaStatus?: string | null;
  /** Catálogo e complemento odontológico (ADR-0136). */
  atendimentoUuid?: string;
  procedimentoCatalogoId?: string | null;
  procedimentoCatalogoCodigo?: string | null;
  sigtap?: string | null;
  odontologico?: boolean;
  dente?: number | null;
  faces?: FaceDente[];
}
