/** Escalas — ADRs 0105 e 0106. */
import { FuncaoEquipe } from './equipe';

export type TipoTurno = 'MANHA' | 'TARDE' | 'NOITE' | 'PLANTAO_12H' | 'PLANTAO_24H' | 'SOBREAVISO' | 'CAPACITACAO';
export type TipoAfastamentoEscala = 'FERIAS' | 'LICENCA_MEDICA' | 'LICENCA_PESSOAL' | 'OUTROS';

export interface TurnoEscalaDto {
  uuid: string;
  unidadeId: string;
  unidadeNome: string;
  equipeId?: string | null;
  equipeNome?: string | null;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
  funcao?: FuncaoEquipe | null;
  tipo: TipoTurno;
  data: string;
  inicioEm: string;
  fimEm: string;
  horas: number;
  descricao?: string | null;
  vaga: boolean;
  alertas: string[];
}

export interface AusenciaEscalaDto {
  matricula: string;
  nome: string;
  cargo?: string | null;
  tipo: TipoAfastamentoEscala;
  inicio: string;
  fim: string;
}

export interface LinhaEscalaDto {
  matricula: string;
  nome: string;
  conselho?: string | null;
  cargo?: string | null;
  jornadaSemanalHoras?: number | null;
  horas: number;
  horasSobreaviso: number;
  equipes: string[];
  alertas: string[];
  ausencias: AusenciaEscalaDto[];
  turnos: TurnoEscalaDto[];
}

export interface EscalaSemanaResponseDto {
  unidadeId: string;
  unidadeNome: string;
  funciona24h: boolean;
  inicio: string;
  fim: string;
  horasPrevistas: number;
  profissionais: number;
  turnos: number;
  vagasAbertas: number;
  plantoes: number;
  afastados: number;
  comAlerta: number;
  linhas: LinhaEscalaDto[];
  vagas: TurnoEscalaDto[];
  ausencias: AusenciaEscalaDto[];
}

export interface TurnoEscalaRequestDto {
  unidadeId: string;
  equipeId?: string;
  profissionalMatricula?: string;
  funcao?: FuncaoEquipe;
  tipo: TipoTurno;
  data: string;
  inicio: string;
  fim: string;
  descricao?: string;
}

export interface CopiaSemanaResponseDto {
  copiados: number;
  ignorados: { data: string; profissionalNome?: string | null; motivo: string }[];
}

/** Rótulo, classe do bloco na grade (do protótipo) e horário sugerido de cada tipo. */
export const TIPOS_TURNO: Record<TipoTurno, { rotulo: string; classe: string; inicio: string; fim: string; plantao: boolean; so24h: boolean }> = {
  MANHA: { rotulo: 'Manhã', classe: 'shift--morn', inicio: '07:00', fim: '13:00', plantao: false, so24h: false },
  TARDE: { rotulo: 'Tarde', classe: 'shift--aft', inicio: '13:00', fim: '19:00', plantao: false, so24h: false },
  NOITE: { rotulo: 'Noite', classe: 'shift--night', inicio: '19:00', fim: '07:00', plantao: true, so24h: true },
  PLANTAO_12H: { rotulo: 'Plantão 12h', classe: 'shift--12h', inicio: '07:00', fim: '19:00', plantao: true, so24h: true },
  PLANTAO_24H: { rotulo: 'Plantão 24h', classe: 'shift--12h', inicio: '07:00', fim: '07:00', plantao: true, so24h: true },
  SOBREAVISO: { rotulo: 'Sobreaviso', classe: 'shift--sob', inicio: '18:00', fim: '06:00', plantao: true, so24h: false },
  CAPACITACAO: { rotulo: 'Capacitação', classe: 'shift--training', inicio: '08:00', fim: '12:00', plantao: false, so24h: false },
};

/** Licença médica aparece só como licença: o motivo de saúde não vai para a escala (ADR-0105). */
export const AUSENCIAS: Record<TipoAfastamentoEscala, { rotulo: string; classe: string; badge: string }> = {
  FERIAS: { rotulo: 'Férias', classe: 'shift--vac', badge: 'badge--warn' },
  LICENCA_MEDICA: { rotulo: 'Licença', classe: 'shift--lic', badge: 'badge--alert' },
  LICENCA_PESSOAL: { rotulo: 'Licença', classe: 'shift--lic', badge: 'badge--alert' },
  OUTROS: { rotulo: 'Afastado', classe: 'shift--lic', badge: 'badge--alert' },
};
