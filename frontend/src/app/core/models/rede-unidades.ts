/** Equipamentos de Saúde — ADRs 0101 e 0102. */

import { TipoUnidadeDeSaude } from './unidade-saude';

export type SituacaoOperacional = 'EM_OPERACAO' | 'EM_MANUTENCAO' | 'EM_OBRA' | 'INOPERANTE';
export type DiaSemana = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface RedeUnidadeResumoDto {
  uuid: string;
  nome: string;
  tipo: TipoUnidadeDeSaude;
  cnes?: string | null;
  situacaoOperacional: SituacaoOperacional;
  motivoSituacao?: string | null;
  previsaoRetorno?: string | null;
  ativo: boolean;
  funciona24h: boolean;
  municipio?: string | null;
  estado?: string | null;
  unidadeSuperiorId?: string | null;
  unidadeSuperiorNome?: string | null;
  supervisaoRegionalId?: string | null;
  supervisaoRegionalNome?: string | null;
  endereco?: string | null;
  telefone?: string | null;
  email?: string | null;
  profissionaisLotados: number;
  setores: number;
  leitos: number;
  leitosOcupados: number;
  comHorario: boolean;
}

export interface RedeUnidadesResponseDto {
  unidades: number;
  ativas: number;
  emOperacao: number;
  foraDeOperacao: number;
  porTipo: Partial<Record<TipoUnidadeDeSaude, number>>;
  rede: RedeUnidadeResumoDto[];
}

export interface TurnoHorarioDto {
  diaSemana: DiaSemana;
  abre: string;
  fecha: string;
}

export interface FichaUnidadeDto {
  resumo: RedeUnidadeResumoDto;
  cep?: string | null;
  logradouro?: string | null;
  numeroLogradouro?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade?: string | null;
  uf?: string | null;
  telefones: string[];
  responsavelCpf?: string | null;
  responsavelNome?: string | null;
  turnos: TurnoHorarioDto[];
  horarioFuncionamentoTexto: Partial<Record<DiaSemana, string>>;
  subordinadas: { uuid: string; nome: string; tipo: string; ativo: boolean }[];
  setoresDaUnidade: { uuid: string; nome: string; tipo: string; ativo: boolean }[];
  profissionais: { matricula: string; nome: string; cargo?: string | null; conselho?: string | null; jornadaSemanalHoras?: number | null }[];
  historico: {
    situacaoAnterior: SituacaoOperacional;
    situacao: SituacaoOperacional;
    motivo?: string | null;
    previsaoRetorno?: string | null;
    ocorridoEm: string;
    registradoPorCpf?: string | null;
    registradoPorNome?: string | null;
  }[];
}

export interface UnidadeCadastroRequestDto {
  nome: string;
  tipo: TipoUnidadeDeSaude;
  cnes?: string;
  unidadeSuperiorId?: string;
  supervisaoRegionalId?: string;
  endereco?: { cep?: string; logradouro?: string; numeroLogradouro?: string; complemento?: string; bairro?: string; cidade?: string; estado?: string };
  telefones?: string[];
  email?: string;
  responsavelCpf?: string;
}

export const SITUACOES_OPERACIONAIS: Record<SituacaoOperacional, { rotulo: string; classe: string }> = {
  EM_OPERACAO: { rotulo: 'Em operação', classe: 'ok' },
  EM_MANUTENCAO: { rotulo: 'Em manutenção', classe: 'warn' },
  EM_OBRA: { rotulo: 'Em obra', classe: 'warn' },
  INOPERANTE: { rotulo: 'Inoperante', classe: 'alert' },
};

export const DIAS_SEMANA: { id: DiaSemana; rotulo: string }[] = [
  { id: 'MONDAY', rotulo: 'Segunda' },
  { id: 'TUESDAY', rotulo: 'Terça' },
  { id: 'WEDNESDAY', rotulo: 'Quarta' },
  { id: 'THURSDAY', rotulo: 'Quinta' },
  { id: 'FRIDAY', rotulo: 'Sexta' },
  { id: 'SATURDAY', rotulo: 'Sábado' },
  { id: 'SUNDAY', rotulo: 'Domingo' },
];

/** Rótulo, sigla do marcador e família de cor de cada tipo (marcador da lista, como no protótipo). */
export const TIPOS_UNIDADE: Record<TipoUnidadeDeSaude, { rotulo: string; sigla: string; familia: string }> = {
  UBS: { rotulo: 'UBS', sigla: 'UBS', familia: '' },
  HOSPITAL: { rotulo: 'Hospital', sigla: 'H', familia: 't-upa' },
  UPA: { rotulo: 'UPA', sigla: 'UPA', familia: 't-upa' },
  LABORATORIO: { rotulo: 'Laboratório', sigla: 'LAB', familia: 't-ceo' },
  CAPS: { rotulo: 'CAPS', sigla: 'CAPS', familia: 't-caps' },
  CENTRO_ESPECIALIDADES: { rotulo: 'Centro de especialidades', sigla: 'CE', familia: 't-ceo' },
  CENTRO_REABILITACAO: { rotulo: 'Centro de reabilitação', sigla: 'CER', familia: 't-ceo' },
  POLICLINICA: { rotulo: 'Policlínica', sigla: 'POL', familia: 't-ceo' },
  FEDERAL: { rotulo: 'Federal', sigla: 'FED', familia: 't-gestao' },
  ESTADUAL: { rotulo: 'Estadual', sigla: 'EST', familia: 't-gestao' },
  MUNICIPAL: { rotulo: 'Municipal', sigla: 'MUN', familia: 't-gestao' },
  REGIONAL: { rotulo: 'Regional', sigla: 'REG', familia: 't-gestao' },
};

export const NIVEIS_DE_GESTAO: TipoUnidadeDeSaude[] = ['FEDERAL', 'ESTADUAL', 'MUNICIPAL', 'REGIONAL'];
