import { ClassificacaoRisco } from './triagem';

export type TipoAtendimento = 'CONSULTA' | 'URGENCIA' | 'INTERNACAO';
export type StatusAtendimento = 'AGENDADO' | 'EM_ANDAMENTO' | 'CONCLUIDO';

export interface AtendimentoRequestDto {
  pacienteId: string;
  profissionalMatricula: string;
  unidadeId: string;
  setorId?: string;
  agendamentoId?: string;
  tipo: TipoAtendimento;
  status: StatusAtendimento;
  dataHora: string;
  /** A especialidade (ADR-0134); sem ela, a da marcação. */
  especialidadeId?: string | null;
}

export interface AtendimentoResponseDto {
  uuid: string;
  pacienteUuid: string;
  pacienteNome: string;
  profissionalMatricula: string;
  profissionalNome: string;
  unidadeUuid: string;
  unidadeNome: string;
  setorUuid?: string;
  setorNome?: string;
  agendamentoUuid?: string;
  tipo: TipoAtendimento;
  status: StatusAtendimento;
  dataHora: string;
  /** Resumo para a listagem (ADR-0062). */
  pacienteCpf?: string;
  pacienteDataNascimento?: string;
  classificacaoRiscoAtual?: ClassificacaoRisco | null;
  totalTriagens?: number;
  totalConsultas?: number;
  totalProcedimentos?: number;
  totalEvolucoes?: number;
  totalAdministracoes?: number;
  especialidadeId?: string | null;
  especialidadeNome?: string | null;
  especialidadeOdontologica?: boolean;
}
