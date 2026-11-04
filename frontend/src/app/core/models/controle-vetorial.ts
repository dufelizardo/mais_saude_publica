// ── Controle de vetores, fatia V4a da Vigilância (ADR-0162) ──

export type TipoVistoriaVetorial = 'ROTINA' | 'LIRAA' | 'BLOQUEIO' | 'PONTO_ESTRATEGICO' | 'DENUNCIA';
export type ResultadoVistoriaVetorial = 'INSPECIONADA' | 'FECHADA' | 'RECUSADA';
export type TipoDeposito = 'A1' | 'A2' | 'B' | 'C' | 'D1' | 'D2' | 'E';
export type FaseFoco = 'LARVA' | 'PUPA' | 'LARVA_E_PUPA';
export type EspecieVetor = 'AEDES_AEGYPTI' | 'AEDES_ALBOPICTUS' | 'OUTRA' | 'NAO_IDENTIFICADA';
export type ResultadoLaboratorioFoco = 'AEDES_AEGYPTI' | 'AEDES_ALBOPICTUS' | 'OUTRA' | 'NEGATIVO';
export type SituacaoFoco = 'ATIVO' | 'TRATADO' | 'ELIMINADO';
export type TipoAcaoControleVetorial = 'BLOQUEIO_TRANSMISSAO' | 'NEBULIZACAO' | 'MUTIRAO_LIMPEZA' | 'EDUCATIVA' | 'OUTRA';
export type SituacaoAcaoControleVetorial = 'PLANEJADA' | 'REALIZADA' | 'CANCELADA';

export const TIPOS_VISTORIA: Record<TipoVistoriaVetorial, string> = {
  ROTINA: 'Rotina',
  LIRAA: 'Levantamento de índice (LIRAa)',
  BLOQUEIO: 'Bloqueio',
  PONTO_ESTRATEGICO: 'Ponto estratégico',
  DENUNCIA: 'Denúncia',
};

export const RESULTADOS_VISTORIA: Record<ResultadoVistoriaVetorial, { rotulo: string; classe: string }> = {
  INSPECIONADA: { rotulo: 'Inspecionado', classe: 'ok' },
  FECHADA: { rotulo: 'Fechado', classe: 'warn' },
  RECUSADA: { rotulo: 'Recusado', classe: 'alert' },
};

/** Os tipos de depósito do PNCD. */
export const TIPOS_DEPOSITO: Record<TipoDeposito, string> = {
  A1: 'A1 · caixa d’água elevada',
  A2: 'A2 · depósito no nível do solo',
  B: 'B · pequeno depósito móvel',
  C: 'C · depósito fixo',
  D1: 'D1 · pneu e material rodante',
  D2: 'D2 · lixo e sucata',
  E: 'E · depósito natural',
};

export const FASES_FOCO: Record<FaseFoco, string> = { LARVA: 'Larva', PUPA: 'Pupa', LARVA_E_PUPA: 'Larva e pupa' };

export const ESPECIES_VETOR: Record<EspecieVetor, string> = {
  AEDES_AEGYPTI: 'Aedes aegypti',
  AEDES_ALBOPICTUS: 'Aedes albopictus',
  OUTRA: 'Outra',
  NAO_IDENTIFICADA: 'Não identificada',
};

export const RESULTADOS_LABORATORIO: Record<ResultadoLaboratorioFoco, string> = {
  AEDES_AEGYPTI: 'Aedes aegypti',
  AEDES_ALBOPICTUS: 'Aedes albopictus',
  OUTRA: 'Outra espécie',
  NEGATIVO: 'Negativo',
};

export const SITUACOES_FOCO: Record<SituacaoFoco, { rotulo: string; classe: string }> = {
  ATIVO: { rotulo: 'Ativo', classe: 'alert' },
  TRATADO: { rotulo: 'Tratado', classe: 'ok' },
  ELIMINADO: { rotulo: 'Eliminado', classe: 'muted' },
};

export const TIPOS_ACAO_VETORIAL: Record<TipoAcaoControleVetorial, string> = {
  BLOQUEIO_TRANSMISSAO: 'Bloqueio de transmissão',
  NEBULIZACAO: 'Nebulização (UBV)',
  MUTIRAO_LIMPEZA: 'Mutirão de limpeza',
  EDUCATIVA: 'Ação educativa',
  OUTRA: 'Outra',
};

export const SITUACOES_ACAO_VETORIAL: Record<SituacaoAcaoControleVetorial, { rotulo: string; classe: string }> = {
  PLANEJADA: { rotulo: 'Planejada', classe: 'info' },
  REALIZADA: { rotulo: 'Realizada', classe: 'ok' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

/** A classificação do IIP pelo Ministério da Saúde. */
export const CLASSIFICACOES_IIP: Record<string, { rotulo: string; classe: string }> = {
  SATISFATORIO: { rotulo: 'Satisfatório', classe: 'ok' },
  ALERTA: { rotulo: 'Alerta', classe: 'warn' },
  RISCO: { rotulo: 'Risco', classe: 'alert' },
};

export interface ImovelVetorialDto {
  uuid: string;
  tipo: string;
  endereco: string;
  microareaId: string;
  microareaCodigo: string;
  areaId: string;
  areaNome: string;
  latitude: number | null;
  longitude: number | null;
  ultimaVistoria: string | null;
  ultimoResultado: ResultadoVistoriaVetorial | null;
  pendente: boolean;
}

export interface FocoVetorialDto {
  uuid: string;
  vistoriaId: string;
  imovelId: string;
  endereco: string;
  microareaCodigo: string;
  areaNome: string;
  latitude: number | null;
  longitude: number | null;
  data: string;
  tipoDeposito: TipoDeposito;
  fase: FaseFoco;
  especieSuspeita: EspecieVetor;
  amostraColetada: boolean;
  resultadoLaboratorio: ResultadoLaboratorioFoco | null;
  resultadoEm: string | null;
  situacao: SituacaoFoco;
  encerradoEm: string | null;
  observacao: string | null;
  contaNosIndices: boolean;
}

export interface VistoriaVetorialDto {
  uuid: string;
  imovelId: string;
  endereco: string;
  microareaCodigo: string;
  areaNome: string;
  data: string;
  cicloAno: number;
  cicloNumero: number;
  agenteMatricula: string;
  agenteNome: string;
  tipo: TipoVistoriaVetorial;
  resultado: ResultadoVistoriaVetorial;
  depositosInspecionados: number;
  depositosEliminados: number;
  depositosTratados: number;
  larvicida: string | null;
  larvicidaGramas: number | null;
  acaoId: string | null;
  observacao: string | null;
  focos: FocoVetorialDto[];
  registradoEm: string;
}

export interface VistoriaVetorialRequestDto {
  imovelId: string;
  data: string | null;
  agenteMatricula: string;
  tipo: TipoVistoriaVetorial;
  resultado: ResultadoVistoriaVetorial;
  depositosA1: number | null;
  depositosA2: number | null;
  depositosB: number | null;
  depositosC: number | null;
  depositosD1: number | null;
  depositosD2: number | null;
  depositosE: number | null;
  depositosEliminados: number | null;
  depositosTratados: number | null;
  larvicida: string | null;
  larvicidaGramas: number | null;
  acaoId: string | null;
  observacao: string | null;
  focos: { tipoDeposito: TipoDeposito; fase: FaseFoco; especieSuspeita: EspecieVetor; amostraColetada: boolean }[];
}

export interface LinhaIndiceVetorial {
  areaId: string | null;
  areaNome: string | null;
  imoveisVisitados: number;
  imoveisInspecionados: number;
  imoveisPendentes: number;
  imoveisPositivos: number;
  depositosPositivos: number;
  iip: number | null;
  ib: number | null;
  percentualPendencia: number | null;
  classificacao: 'SATISFATORIO' | 'ALERTA' | 'RISCO' | null;
}

export interface IndicesVetoriaisDto {
  ano: number;
  ciclo: number;
  inicio: string;
  fim: string;
  municipio: LinhaIndiceVetorial;
  areas: LinhaIndiceVetorial[];
}

export interface AcaoControleVetorialDto {
  uuid: string;
  tipo: TipoAcaoControleVetorial;
  descricao: string | null;
  microareas: string[];
  microareaIds: string[];
  dataPrevista: string;
  dataRealizada: string | null;
  responsavel: string;
  imoveisTrabalhados: number | null;
  vistorias: number;
  notificacaoId: string | null;
  agravoNome: string | null;
  surtoId: string | null;
  surtoNome: string | null;
  situacao: SituacaoAcaoControleVetorial;
  resultado: string | null;
  motivoCancelamento: string | null;
  registradoEm: string;
}

export interface AcaoControleVetorialRequestDto {
  tipo: TipoAcaoControleVetorial;
  descricao: string | null;
  microareaIds: string[];
  dataPrevista: string;
  responsavel: string;
  notificacaoId: string | null;
  surtoId: string | null;
}

export interface BloqueioPendenteDto {
  notificacaoId: string;
  agravoNome: string;
  dataNotificacao: string;
  dataPrimeirosSintomas: string | null;
  unidadeNome: string;
  microareaId: string | null;
  microareaCodigo: string | null;
  areaNome: string | null;
  localProvavel: string | null;
  latitude: number | null;
  longitude: number | null;
}
