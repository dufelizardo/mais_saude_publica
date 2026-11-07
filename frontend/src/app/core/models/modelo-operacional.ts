/** Modelo operacional das unidades — ADR-0117 (fatia MO1 da ADR-0115). */

import { TipoUnidadeDeSaude } from './unidade-saude';

export interface PerfilOperacionalDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
  capacidades: number;
  unidades: number;
}

export interface PerfilOperacionalRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

export interface CapacidadeAssistencialDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
  /** Nulos: transversal. */
  perfilId?: string | null;
  perfilCodigo?: string | null;
  perfilNome?: string | null;
  unidades: number;
}

export interface CapacidadeAssistencialRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  perfilId?: string | null;
  ativo: boolean;
}

export interface PerfilDaUnidadeDto {
  /** Id da associação. */
  uuid: string;
  perfilId: string;
  codigo: string;
  nome: string;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface CapacidadeDaUnidadeDto {
  /** Id da associação. */
  uuid: string;
  capacidadeId: string;
  codigo: string;
  nome: string;
  perfilId?: string | null;
  perfilNome?: string | null;
  inicio: string;
  fim?: string | null;
  observacao?: string | null;
  motivoFim?: string | null;
}

export interface ModeloOperacionalUnidadeDto {
  unidadeId: string;
  unidadeNome: string;
  tipo: TipoUnidadeDeSaude;
  nivelDeGestao: boolean;
  perfis: PerfilDaUnidadeDto[];
  capacidades: CapacidadeDaUnidadeDto[];
  perfisEncerrados: PerfilDaUnidadeDto[];
  capacidadesEncerradas: CapacidadeDaUnidadeDto[];
}

/** Tipo de serviço do catálogo (ADR-0165). */
export interface TipoServicoDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  capacidadeExigidaId?: string | null;
  capacidadeExigidaNome?: string | null;
  ativo: boolean;
  /** Serviços vigentes deste tipo. */
  servicos: number;
}

export interface TipoServicoRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  capacidadeExigidaId?: string | null;
  ativo: boolean;
}

export interface SetorRefDto {
  uuid: string;
  nome: string;
}

/** Serviço da unidade (ADR-0165); os leitos vêm dos setores. */
export interface ServicoDaUnidadeDto {
  uuid: string;
  tipoId: string;
  tipoCodigo: string;
  tipoNome: string;
  nome: string;
  responsavelMatricula?: string | null;
  responsavelNome?: string | null;
  vinteQuatroHoras: boolean;
  horario?: string | null;
  setores: SetorRefDto[];
  leitos: number;
  leitosLivres: number;
  leitosOcupados: number;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface ServicosDaUnidadeDto {
  unidadeId: string;
  servicos: ServicoDaUnidadeDto[];
  encerrados: ServicoDaUnidadeDto[];
  setoresLivres: SetorRefDto[];
}

export interface ServicoUnidadeRequestDto {
  tipoId?: string | null;
  nome: string;
  responsavelMatricula?: string | null;
  vinteQuatroHoras: boolean;
  horario?: string | null;
  setorIds: string[];
  inicio?: string | null;
}

export type TipoPontoOperacional = 'BASE' | 'PONTO_FIXO' | 'PONTO_PROGRAMADO';

export const TIPOS_DE_PONTO: { valor: TipoPontoOperacional; rotulo: string; sigla: string }[] = [
  { valor: 'BASE', rotulo: 'Base', sigla: 'B' },
  { valor: 'PONTO_FIXO', rotulo: 'Ponto fixo', sigla: 'F' },
  { valor: 'PONTO_PROGRAMADO', rotulo: 'Ponto programado', sigla: 'P' },
];

export type DiaDaSemana = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export const DIAS_DA_SEMANA: { valor: DiaDaSemana; rotulo: string }[] = [
  { valor: 'MONDAY', rotulo: 'Seg' },
  { valor: 'TUESDAY', rotulo: 'Ter' },
  { valor: 'WEDNESDAY', rotulo: 'Qua' },
  { valor: 'THURSDAY', rotulo: 'Qui' },
  { valor: 'FRIDAY', rotulo: 'Sex' },
  { valor: 'SATURDAY', rotulo: 'Sáb' },
  { valor: 'SUNDAY', rotulo: 'Dom' },
];

/** Ponto operacional da unidade (ADR-0166). */
export interface PontoDaUnidadeDto {
  uuid: string;
  tipo: TipoPontoOperacional;
  nome: string;
  referencia?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface RotaDaUnidadeDto {
  uuid: string;
  nome: string;
  dias: DiaDaSemana[];
  horaInicio?: string | null;
  horaFim?: string | null;
  pontos: { uuid: string; nome: string }[];
  inicio: string;
  fim?: string | null;
  motivoFim?: string | null;
}

export interface PosicaoDaUnidadeDto {
  uuid: string;
  pontoId: string;
  pontoNome: string;
  desde: string;
  observacao?: string | null;
}

export interface PontosDaUnidadeDto {
  unidadeId: string;
  /** Tem o perfil Atenção móvel vigente. */
  movel: boolean;
  pontos: PontoDaUnidadeDto[];
  pontosEncerrados: PontoDaUnidadeDto[];
  rotas: RotaDaUnidadeDto[];
  rotasEncerradas: RotaDaUnidadeDto[];
  /** A mais recente primeiro; a primeira é a localização atual. */
  posicoes: PosicaoDaUnidadeDto[];
}

export interface PontoRequestDto {
  tipo?: TipoPontoOperacional | null;
  nome: string;
  referencia?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  inicio?: string | null;
}

export interface RotaRequestDto {
  nome: string;
  dias: DiaDaSemana[];
  horaInicio?: string | null;
  horaFim?: string | null;
  pontoIds: string[];
}

export interface LocaisDeRealizacaoDto {
  pontos: { uuid: string; nome: string; tipo: TipoPontoOperacional }[];
  domicilioId?: string | null;
  domicilioDescricao?: string | null;
}

/** Rótulo do grupo das capacidades sem perfil. */
export const GRUPO_TRANSVERSAL = 'Transversais';
