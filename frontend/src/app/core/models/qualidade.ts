/** Qualidade, fatia Q1: segurança do paciente (ADR-0203). */
export type TipoIncidente = 'IDENTIFICACAO_PACIENTE' | 'COMUNICACAO' | 'MEDICACAO' | 'CIRURGIA' | 'INFECCAO' | 'QUEDA' | 'LESAO_POR_PRESSAO' | 'DIETA' | 'EQUIPAMENTO' | 'DOCUMENTACAO' | 'OUTRO';
export type NaturezaIncidente = 'CIRCUNSTANCIA_DE_RISCO' | 'QUASE_ERRO' | 'INCIDENTE_SEM_DANO' | 'EVENTO_ADVERSO';
export type GrauDano = 'NENHUM' | 'LEVE' | 'MODERADO' | 'GRAVE' | 'OBITO';
export type SituacaoIncidente = 'RECEBIDA' | 'EM_ANALISE' | 'CONCLUIDA' | 'DESCARTADA';
export type FatorContribuinte = 'PACIENTE' | 'PROFISSIONAL' | 'EQUIPE' | 'TAREFA' | 'AMBIENTE' | 'ORGANIZACAO' | 'EXTERNO';

export const TIPOS_INCIDENTE: Record<TipoIncidente, string> = {
  IDENTIFICACAO_PACIENTE: 'Identificação do paciente', COMUNICACAO: 'Comunicação', MEDICACAO: 'Medicação', CIRURGIA: 'Cirurgia ou procedimento',
  INFECCAO: 'Infecção relacionada à assistência', QUEDA: 'Queda', LESAO_POR_PRESSAO: 'Lesão por pressão', DIETA: 'Dieta', EQUIPAMENTO: 'Equipamento',
  DOCUMENTACAO: 'Documentação', OUTRO: 'Outro',
};
export const NATUREZAS: Record<NaturezaIncidente, string> = {
  CIRCUNSTANCIA_DE_RISCO: 'Circunstância de risco', QUASE_ERRO: 'Quase erro', INCIDENTE_SEM_DANO: 'Incidente sem dano', EVENTO_ADVERSO: 'Evento adverso',
};
export const DANOS: Record<GrauDano, string> = { NENHUM: 'Nenhum', LEVE: 'Leve', MODERADO: 'Moderado', GRAVE: 'Grave', OBITO: 'Óbito' };
export const SITUACOES_INCIDENTE: Record<SituacaoIncidente, string> = { RECEBIDA: 'Recebida', EM_ANALISE: 'Em análise', CONCLUIDA: 'Concluída', DESCARTADA: 'Descartada' };
export const FATORES: Record<FatorContribuinte, string> = {
  PACIENTE: 'Paciente', PROFISSIONAL: 'Profissional', EQUIPE: 'Equipe', TAREFA: 'Tarefa ou tecnologia', AMBIENTE: 'Ambiente', ORGANIZACAO: 'Organização', EXTERNO: 'Externo',
};

export interface ProtocoloIncidenteDto { uuid: string; numero: string; registradoEm: string; anonima: boolean }

export interface IncidenteDto {
  uuid: string; numero: string; unidadeId: string; unidadeNome: string; pacienteId: string | null; pacienteNome: string | null; ocorridoEm: string;
  local: string; tipo: TipoIncidente; danoPercebido: GrauDano | null; descricao: string; acaoImediata: string | null; anonima: boolean;
  notificadorNome: string | null; situacao: SituacaoIncidente; natureza: NaturezaIncidente | null; dano: GrauDano | null; neverEvent: boolean;
  fatores: FatorContribuinte[]; causaRaiz: string | null; licoes: string | null; motivoDescarte: string | null; analisadoEm: string | null;
  exigeCausaRaiz: boolean; notivisaObrigatoria: boolean; prazoNotivisa: string | null; numeroNotivisa: string | null; notivisaEm: string | null;
  notivisaVencida: boolean; registradoEm: string;
}

export interface PainelSegurancaDto {
  total: number; aguardandoAnalise: number; eventosAdversos: number; notivisaPendentes: number; notivisaVencidas: number;
  porTipo: { codigo: TipoIncidente; quantidade: number }[]; porDano: { codigo: GrauDano; quantidade: number }[];
  porNatureza: { codigo: NaturezaIncidente; quantidade: number }[];
}
