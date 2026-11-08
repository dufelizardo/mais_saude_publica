import { PrioridadeRegulacao } from './regulacao';

/** Regulação de urgência (ADR-0176). */
export type OrigemOcorrenciaUrgencia = 'CHAMADO_192' | 'UNIDADE_DE_SAUDE' | 'CRISE_SAUDE_MENTAL';
export type SituacaoOcorrenciaUrgencia = 'RECEBIDA' | 'REGULADA' | 'EM_ATENDIMENTO' | 'CONCLUIDA' | 'CANCELADA';
export type DecisaoRegulacaoUrgencia = 'ORIENTACAO' | 'ENVIO_DE_RECURSO' | 'ENCAMINHAMENTO' | 'TRANSFERENCIA';
export type TipoRecursoUrgencia = 'USA' | 'USB' | 'MOTOLANCIA' | 'AEROMEDICO' | 'OUTRO';
export type DesfechoOcorrenciaUrgencia =
  | 'ORIENTADO' | 'ATENDIDO_NO_LOCAL' | 'RECEBIDO_NO_DESTINO' | 'OBITO_NO_LOCAL' | 'RECUSA' | 'SEM_OCORRENCIA' | 'DESISTENCIA';
export type TipoEventoUrgencia =
  | 'RECEBIMENTO' | 'REGULACAO' | 'RECLASSIFICACAO' | 'DESTINO' | 'ENVIO_DE_RECURSO' | 'CHEGADA_AO_LOCAL' | 'SAIDA_DO_LOCAL'
  | 'CHEGADA_AO_DESTINO' | 'RECEBIMENTO_NO_DESTINO' | 'VINCULO_DO_PACIENTE' | 'ENCERRAMENTO' | 'CANCELAMENTO';
export type LocalOcorrencia = 'DOMICILIO' | 'VIA_PUBLICA' | 'LOCAL_DO_EVENTO' | 'ESCOLA' | 'PONTO_DA_UNIDADE' | 'OUTRO';

export const ORIGENS: Record<OrigemOcorrenciaUrgencia, string> = {
  CHAMADO_192: 'Chamado 192',
  UNIDADE_DE_SAUDE: 'Pedido de unidade',
  CRISE_SAUDE_MENTAL: 'Crise em saúde mental',
};

export const SITUACOES_URGENCIA: Record<SituacaoOcorrenciaUrgencia, { rotulo: string; classe: string }> = {
  RECEBIDA: { rotulo: 'Aguardando regulação', classe: 'warn' },
  REGULADA: { rotulo: 'Regulada', classe: 'info' },
  EM_ATENDIMENTO: { rotulo: 'Recurso em campo', classe: 'purple' },
  CONCLUIDA: { rotulo: 'Concluída', classe: 'ok' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export const DECISOES: Record<DecisaoRegulacaoUrgencia, string> = {
  ORIENTACAO: 'Orientação por telefone',
  ENVIO_DE_RECURSO: 'Envio de recurso',
  ENCAMINHAMENTO: 'Encaminhamento (meios próprios)',
  TRANSFERENCIA: 'Transferência entre unidades',
};

export const RECURSOS: Record<TipoRecursoUrgencia, string> = {
  USA: 'USA · suporte avançado',
  USB: 'USB · suporte básico',
  MOTOLANCIA: 'Motolância',
  AEROMEDICO: 'Aeromédico',
  OUTRO: 'Outro',
};

export const DESFECHOS: Record<DesfechoOcorrenciaUrgencia, string> = {
  ORIENTADO: 'Orientado por telefone',
  ATENDIDO_NO_LOCAL: 'Atendido no local',
  RECEBIDO_NO_DESTINO: 'Recebido no destino',
  OBITO_NO_LOCAL: 'Óbito no local',
  RECUSA: 'Recusa',
  SEM_OCORRENCIA: 'Sem ocorrência (engano ou trote)',
  DESISTENCIA: 'Desistência',
};

export const EVENTOS_URGENCIA: Record<TipoEventoUrgencia, string> = {
  RECEBIMENTO: 'Chamado recebido',
  REGULACAO: 'Regulação médica',
  RECLASSIFICACAO: 'Reclassificação',
  DESTINO: 'Destino definido',
  ENVIO_DE_RECURSO: 'Recurso enviado',
  CHEGADA_AO_LOCAL: 'Chegada ao local',
  SAIDA_DO_LOCAL: 'Saída do local',
  CHEGADA_AO_DESTINO: 'Chegada ao destino',
  RECEBIMENTO_NO_DESTINO: 'Recebido no destino',
  VINCULO_DO_PACIENTE: 'Paciente identificado',
  ENCERRAMENTO: 'Encerramento',
  CANCELAMENTO: 'Cancelamento',
};

export const LOCAIS: Record<LocalOcorrencia, string> = {
  DOMICILIO: 'Domicílio',
  VIA_PUBLICA: 'Via pública',
  LOCAL_DO_EVENTO: 'Local de evento',
  ESCOLA: 'Escola',
  PONTO_DA_UNIDADE: 'Ponto da unidade',
  OUTRO: 'Outro',
};

export interface EventoUrgenciaDto {
  uuid: string;
  tipo: TipoEventoUrgencia;
  situacaoResultante: SituacaoOcorrenciaUrgencia;
  prioridade?: PrioridadeRegulacao | null;
  texto?: string | null;
  profissionalNome?: string | null;
  ocorridoEm: string;
}

export interface OcorrenciaUrgenciaDto {
  uuid: string;
  protocolo: string;
  centralId: string;
  centralNome: string;
  origem: OrigemOcorrenciaUrgencia;
  situacao: SituacaoOcorrenciaUrgencia;
  prioridade?: PrioridadeRegulacao | null;
  clinico: boolean;
  solicitanteNome?: string | null;
  solicitanteTelefone?: string | null;
  unidadeSolicitanteId?: string | null;
  unidadeSolicitanteNome?: string | null;
  criseId?: string | null;
  pacienteId?: string | null;
  pacienteNome?: string | null;
  descricaoPaciente?: string | null;
  localTipo?: LocalOcorrencia | null;
  endereco?: string | null;
  referencia?: string | null;
  queixa?: string | null;
  recebidaEm: string;
  reguladaEm?: string | null;
  reguladorNome?: string | null;
  decisao?: DecisaoRegulacaoUrgencia | null;
  conduta?: string | null;
  tipoRecurso?: TipoRecursoUrgencia | null;
  recursoIdentificacao?: string | null;
  destinoId?: string | null;
  destinoNome?: string | null;
  vagaZero: boolean;
  justificativaVagaZero?: string | null;
  recebidaNoDestinoEm?: string | null;
  desfecho?: DesfechoOcorrenciaUrgencia | null;
  encerradaEm?: string | null;
  ultimoDeslocamento?: TipoEventoUrgencia | null;
  minutosAteRegulacao?: number | null;
  minutosAteEnvio?: number | null;
  minutosEnvioAteLocal?: number | null;
  minutosTotal?: number | null;
  eventos?: EventoUrgenciaDto[] | null;
}

export interface PainelUrgenciaDto {
  abertas: OcorrenciaUrgenciaDto[];
  aguardandoRegulacao: number;
  emAtendimento: number;
  porPrioridade: Record<PrioridadeRegulacao, number>;
  recebidasHoje: number;
  concluidasHoje: number;
  tempoMedioAteRegulacaoMinutos?: number | null;
  tempoMedioAteEnvioMinutos?: number | null;
}

export interface DestinoUrgenciaDto {
  unidadeId: string;
  nome: string;
  tipo: string;
  leitosLivres: number;
  leitosAtivos: number;
  aCaminho: number;
}

export interface CentralUrgenciaDto {
  unidadeId: string;
  nome: string;
}

export interface OcorrenciaUrgenciaRequestDto {
  centralId: string;
  origem: OrigemOcorrenciaUrgencia;
  solicitanteNome?: string | null;
  solicitanteTelefone?: string | null;
  unidadeSolicitanteId?: string | null;
  pacienteId?: string | null;
  descricaoPaciente?: string | null;
  localTipo?: LocalOcorrencia | null;
  endereco?: string | null;
  referencia?: string | null;
  queixa: string;
}

export interface RegulacaoUrgenciaRequestDto {
  profissionalMatricula: string;
  prioridade: PrioridadeRegulacao;
  decisao: DecisaoRegulacaoUrgencia;
  conduta: string;
  tipoRecurso?: TipoRecursoUrgencia | null;
  recursoIdentificacao?: string | null;
  destinoId?: string | null;
  vagaZero?: boolean;
  justificativaVagaZero?: string | null;
}
