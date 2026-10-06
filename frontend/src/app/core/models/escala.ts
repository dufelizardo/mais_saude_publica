/** Escalas — ADRs 0105, 0106 e 0107. */
import { FuncaoEquipe } from './equipe';

export type TipoTurno = 'MANHA' | 'TARDE' | 'DIURNO' | 'NOITE' | 'PLANTAO_12H' | 'PLANTAO_24H' | 'SOBREAVISO' | 'CAPACITACAO';
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
  /** Horas trabalhadas: a duração menos o intervalo. */
  horas: number;
  intervaloMinutos: number;
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
  intervaloMinutos?: number;
}

export interface CopiaSemanaResponseDto {
  copiados: number;
  ignorados: { data: string; profissionalNome?: string | null; motivo: string }[];
}

/** Rótulo, classe do bloco na grade (do protótipo), horário e intervalo sugeridos de cada tipo. */
export const TIPOS_TURNO: Record<TipoTurno, { rotulo: string; classe: string; inicio: string; fim: string; intervalo: number; plantao: boolean; so24h: boolean }> = {
  MANHA: { rotulo: 'Manhã', classe: 'shift--morn', inicio: '07:00', fim: '13:15', intervalo: 15, plantao: false, so24h: false },
  TARDE: { rotulo: 'Tarde', classe: 'shift--aft', inicio: '13:00', fim: '19:00', intervalo: 15, plantao: false, so24h: false },
  DIURNO: { rotulo: 'Diurno (8h + 1h)', classe: 'shift--day', inicio: '08:00', fim: '17:00', intervalo: 60, plantao: false, so24h: false },
  NOITE: { rotulo: 'Noite', classe: 'shift--night', inicio: '19:00', fim: '07:00', intervalo: 60, plantao: true, so24h: true },
  PLANTAO_12H: { rotulo: 'Plantão 12h', classe: 'shift--12h', inicio: '07:00', fim: '19:00', intervalo: 0, plantao: true, so24h: true },
  PLANTAO_24H: { rotulo: 'Plantão 24h', classe: 'shift--12h', inicio: '07:00', fim: '07:00', intervalo: 0, plantao: true, so24h: true },
  SOBREAVISO: { rotulo: 'Sobreaviso', classe: 'shift--sob', inicio: '18:00', fim: '06:00', intervalo: 0, plantao: true, so24h: false },
  CAPACITACAO: { rotulo: 'Capacitação', classe: 'shift--training', inicio: '08:00', fim: '12:00', intervalo: 0, plantao: false, so24h: false },
};

/** Intervalos oferecidos na gaveta (CLT, art. 71: 15min acima de 4h, de 1h a 2h acima de 6h). */
export const INTERVALOS = [0, 15, 30, 60, 90, 120];

export type ModeloJornada = 'H40_8H' | 'H44_6X1' | 'H30_6H' | 'H20_4H' | 'H12X36';

export interface AplicacaoModeloResponseDto {
  criados: number;
  horas: number;
  ignorados: { data: string; profissionalNome?: string | null; motivo: string }[];
}

/**
 * Modelos de jornada (ADR-0107), espelho do backend: dias (0 = segunda) com as horas trabalhadas, início e intervalo
 * sugeridos e a jornada contratada que os sugere.
 */
export const MODELOS_JORNADA: Record<ModeloJornada, { rotulo: string; inicio: string; intervalo: number; jornada: number; plantao: boolean; dias: [number, number][] }> = {
  H40_8H: { rotulo: '40h semanais — 8h + 1h de intervalo', inicio: '08:00', intervalo: 60, jornada: 40, plantao: false, dias: [[0, 480], [1, 480], [2, 480], [3, 480], [4, 480]] },
  H44_6X1: { rotulo: '44h semanais — 6x1 (8h + 1h, sábado 4h)', inicio: '08:00', intervalo: 60, jornada: 44, plantao: false, dias: [[0, 480], [1, 480], [2, 480], [3, 480], [4, 480], [5, 240]] },
  H30_6H: { rotulo: '30h semanais — 6h + 15min', inicio: '07:00', intervalo: 15, jornada: 30, plantao: false, dias: [[0, 360], [1, 360], [2, 360], [3, 360], [4, 360]] },
  H20_4H: { rotulo: '20h semanais — 4h', inicio: '08:00', intervalo: 0, jornada: 20, plantao: false, dias: [[0, 240], [1, 240], [2, 240], [3, 240], [4, 240]] },
  H12X36: { rotulo: '12x36 — plantões de 12h em dias alternados', inicio: '07:00', intervalo: 0, jornada: 36, plantao: true, dias: [[0, 720], [2, 720], [4, 720], [6, 720]] },
};

/** Licença médica aparece só como licença: o motivo de saúde não vai para a escala (ADR-0105). */
export const AUSENCIAS: Record<TipoAfastamentoEscala, { rotulo: string; classe: string; badge: string }> = {
  FERIAS: { rotulo: 'Férias', classe: 'shift--vac', badge: 'badge--warn' },
  LICENCA_MEDICA: { rotulo: 'Licença', classe: 'shift--lic', badge: 'badge--alert' },
  LICENCA_PESSOAL: { rotulo: 'Licença', classe: 'shift--lic', badge: 'badge--alert' },
  OUTROS: { rotulo: 'Afastado', classe: 'shift--lic', badge: 'badge--alert' },
};
