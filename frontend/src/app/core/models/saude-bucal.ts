/** Especialidades e Saúde Bucal (ADR-0134). */

export interface EspecialidadeDto {
  uuid: string;
  codigo: string;
  nome: string;
  odontologica: boolean;
  ativa: boolean;
}

export interface EspecialidadeRequestDto {
  codigo: string;
  nome: string;
  odontologica?: boolean;
  ativa?: boolean;
}

export type RiscoSaudeBucal = 'BAIXO' | 'MEDIO' | 'ALTO';
export type NecessidadeTratamentoOdontologico = 'SEM_NECESSIDADE' | 'ELETIVA' | 'PRIORITARIA' | 'URGENCIA';

export const RISCOS_SAUDE_BUCAL: Record<RiscoSaudeBucal, { rotulo: string; classe: string }> = {
  BAIXO: { rotulo: 'Risco baixo', classe: 'ok' },
  MEDIO: { rotulo: 'Risco médio', classe: 'warn' },
  ALTO: { rotulo: 'Risco alto', classe: 'alert' },
};

export const NECESSIDADES_TRATAMENTO: Record<NecessidadeTratamentoOdontologico, string> = {
  SEM_NECESSIDADE: 'Sem necessidade de tratamento',
  ELETIVA: 'Tratamento eletivo',
  PRIORITARIA: 'Tratamento prioritário',
  URGENCIA: 'Urgência',
};

export interface AvaliacaoOdontologicaDto {
  uuid: string;
  atendimentoId: string;
  pacienteId: string;
  profissionalMatricula: string;
  profissionalNome: string;
  dataHora: string;
  queixaPrincipal?: string | null;
  anamnese?: string | null;
  exameClinico?: string | null;
  diagnostico?: string | null;
  risco: RiscoSaudeBucal;
  necessidadeTratamento: NecessidadeTratamentoOdontologico;
  planoInicial?: string | null;
  observacoes?: string | null;
  retificacaoDeUuid?: string | null;
  motivoRetificacao?: string | null;
  retificado: boolean;
  retificadoPorUuid?: string | null;
  registradoEm: string;
}

export interface AvaliacaoOdontologicaRequestDto {
  atendimentoId: string;
  profissionalMatricula: string;
  dataHora?: string | null;
  queixaPrincipal?: string | null;
  anamnese?: string | null;
  exameClinico?: string | null;
  diagnostico?: string | null;
  risco: RiscoSaudeBucal;
  necessidadeTratamento: NecessidadeTratamentoOdontologico;
  planoInicial?: string | null;
  observacoes?: string | null;
  motivoRetificacao?: string | null;
}
