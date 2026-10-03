/** Agenda do profissional (ADRs 0091 e 0092). */
import { StatusAgendamento, TipoAgendamento } from './agendamento';

export type DiaSemana = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export type MotivoBloqueioAgenda = 'FOLGA' | 'REUNIAO' | 'CAPACITACAO' | 'UNIDADE_FECHADA' | 'OUTRO';

export type TipoItemAgenda = 'VAGA' | 'MARCACAO' | 'ENCAIXE' | 'BLOQUEIO' | 'AFASTAMENTO';

export interface BlocoAgendaRequestDto {
  profissionalMatricula: string;
  unidadeId: string;
  diaSemana: DiaSemana;
  horaInicio: string;
  horaFim: string;
  duracaoMinutos: number;
  tipo: TipoAgendamento;
  vigenteDesde?: string;
  vigenteAte?: string;
}

export interface BlocoAgendaResponseDto {
  uuid: string;
  profissionalMatricula: string;
  profissionalNome: string;
  unidadeId: string;
  unidadeNome: string;
  diaSemana: DiaSemana;
  horaInicio: string;
  horaFim: string;
  duracaoMinutos: number;
  tipo: TipoAgendamento;
  vigenteDesde: string;
  vigenteAte?: string | null;
  vagasPorDia: number;
}

export interface BloqueioAgendaRequestDto {
  profissionalMatricula?: string;
  unidadeId?: string;
  inicio: string;
  fim: string;
  motivo: MotivoBloqueioAgenda;
  descricao?: string;
}

export interface BloqueioAgendaResponseDto {
  uuid: string;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  inicio: string;
  fim: string;
  motivo: MotivoBloqueioAgenda;
  descricao?: string | null;
}

/** LocalDateTime sem fuso em inicio e fim. */
export interface ItemAgendaDto {
  tipo: TipoItemAgenda;
  inicio: string;
  fim: string;
  tipoAtendimento?: TipoAgendamento | null;
  agendamentoId?: string | null;
  pacienteId?: string | null;
  pacienteNome?: string | null;
  status?: StatusAgendamento | null;
  descricao?: string | null;
}

export interface DiaAgendaDto {
  data: string;
  itens: ItemAgendaDto[];
}

export interface AgendaResponseDto {
  profissionalMatricula: string;
  profissionalNome: string;
  unidadeId: string;
  unidadeNome: string;
  de: string;
  ate: string;
  vagasOfertadas: number;
  vagasOcupadas: number;
  ocupacaoPercentual: number;
  marcacoes: number;
  encaixes: number;
  faltas: number;
  dias: DiaAgendaDto[];
  /** Situação da unidade quando não está em operação (ADR-0101). */
  avisoUnidade?: string | null;
}
