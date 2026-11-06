import { ClassificacaoRisco } from './triagem';

export type TipoAtendimento = 'CONSULTA' | 'URGENCIA' | 'INTERNACAO';
export type StatusAtendimento = 'AGENDADO' | 'EM_ANDAMENTO' | 'CONCLUIDO';
export type LocalRealizacao = 'DOMICILIO' | 'VIA_PUBLICA' | 'LOCAL_DO_EVENTO' | 'ESCOLA' | 'PONTO_DA_UNIDADE' | 'OUTRO';

export const LOCAIS_DE_REALIZACAO: { valor: LocalRealizacao; rotulo: string }[] = [
  { valor: 'DOMICILIO', rotulo: 'Domicílio' },
  { valor: 'VIA_PUBLICA', rotulo: 'Via pública' },
  { valor: 'LOCAL_DO_EVENTO', rotulo: 'Local do evento' },
  { valor: 'ESCOLA', rotulo: 'Escola' },
  { valor: 'PONTO_DA_UNIDADE', rotulo: 'Ponto da unidade' },
  { valor: 'OUTRO', rotulo: 'Outro' },
];

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
  /** O local de realização (ADR-0166); sem ele, na unidade. */
  localRealizacao?: LocalRealizacao | null;
  localDescricao?: string | null;
  localLatitude?: number | null;
  localLongitude?: number | null;
  domicilioId?: string | null;
  pontoOperacionalId?: string | null;
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
  /** O local de realização (ADR-0166); sem ele, na unidade. */
  localRealizacao?: LocalRealizacao | null;
  localDescricao?: string | null;
  localLatitude?: number | null;
  localLongitude?: number | null;
  domicilioId?: string | null;
  pontoOperacionalId?: string | null;
  pontoOperacionalNome?: string | null;
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
