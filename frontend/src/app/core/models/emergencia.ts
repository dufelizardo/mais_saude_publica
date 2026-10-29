// ── Emergências e Desastres, fatia E1 (ADR-0156): evento, emergência, nível, território e impacto ──

export type CategoriaTipoEmergencia = 'DESASTRE' | 'SAUDE';
export type FonteEventoEmergencial = 'VIGILANCIA' | 'UNIDADE' | 'DEFESA_CIVIL' | 'GESTOR' | 'PROFISSIONAL' | 'SISTEMA_EXTERNO';
export type SituacaoEventoEmergencial = 'REGISTRADO' | 'MONITORADO' | 'DESCARTADO' | 'CONVERTIDO';
export type SituacaoEmergencia = 'ATIVA' | 'ENCERRADA';
export type NivelImpacto = 'BAIXO' | 'MODERADO' | 'ALTO' | 'CRITICO';
export type TipoDecretoEmergencia = 'SITUACAO_DE_EMERGENCIA' | 'CALAMIDADE_PUBLICA';

export const FONTES_EVENTO: Record<FonteEventoEmergencial, string> = {
  VIGILANCIA: 'Vigilância',
  UNIDADE: 'Unidade de saúde',
  DEFESA_CIVIL: 'Defesa Civil',
  GESTOR: 'Gestor',
  PROFISSIONAL: 'Profissional',
  SISTEMA_EXTERNO: 'Sistema externo',
};

export const SITUACOES_EVENTO: Record<SituacaoEventoEmergencial, { rotulo: string; classe: string }> = {
  REGISTRADO: { rotulo: 'Registrado', classe: 'info' },
  MONITORADO: { rotulo: 'Monitorado', classe: 'warn' },
  DESCARTADO: { rotulo: 'Descartado', classe: 'muted' },
  CONVERTIDO: { rotulo: 'Virou emergência', classe: 'alert' },
};

export const NIVEIS_IMPACTO: Record<NivelImpacto, { rotulo: string; classe: string }> = {
  BAIXO: { rotulo: 'Baixo', classe: 'ok' },
  MODERADO: { rotulo: 'Moderado', classe: 'info' },
  ALTO: { rotulo: 'Alto', classe: 'warn' },
  CRITICO: { rotulo: 'Crítico', classe: 'alert' },
};

export const DECRETOS: Record<TipoDecretoEmergencia, string> = {
  SITUACAO_DE_EMERGENCIA: 'Situação de emergência',
  CALAMIDADE_PUBLICA: 'Estado de calamidade pública',
};

/** A cor do nível de resposta pela ordem (0 alerta … 3 máxima). */
export function classeDoNivel(ordem: number): string {
  return ordem >= 3 ? 'alert' : ordem === 2 ? 'warn' : ordem === 1 ? 'info' : 'muted';
}

export interface TipoEmergenciaDto {
  uuid: string;
  codigo: string;
  nome: string;
  categoria: CategoriaTipoEmergencia;
  cobrade: string | null;
  ativo: boolean;
}

export interface NivelEmergenciaDto {
  uuid: string;
  ordem: number;
  nome: string;
  descricao: string | null;
  ativo: boolean;
}

export interface EventoEmergencialDto {
  uuid: string;
  tipoId: string;
  tipoNome: string;
  cobrade: string | null;
  ocorridoEm: string;
  local: string | null;
  geometria: string | null;
  fonte: FonteEventoEmergencial;
  descricao: string;
  surtoId: string | null;
  surtoNome: string | null;
  situacao: SituacaoEventoEmergencial;
  motivoDescarte: string | null;
  emergenciaId: string | null;
  registradoEm: string;
}

export interface EmergenciaDto {
  uuid: string;
  nome: string;
  eventoId: string;
  tipoId: string;
  tipoNome: string;
  categoria: CategoriaTipoEmergencia;
  cobrade: string | null;
  nivelId: string;
  nivelOrdem: number;
  nivelNome: string;
  situacao: SituacaoEmergencia;
  inicio: string;
  fim: string | null;
  motivoEncerramento: string | null;
  responsavelMatricula: string | null;
  responsavelNome: string | null;
  ultimoImpacto: NivelImpacto | null;
  ultimaAvaliacaoEm: string | null;
  geometria: string | null;
  microareas: { uuid: string; codigo: string; nome: string | null; areaNome: string; geometria: string | null }[];
  espinNumero: string | null;
  espinData: string | null;
  decretoTipo: TipoDecretoEmergencia | null;
  decretoNumero: string | null;
  decretoData: string | null;
  niveis: { uuid: string; nivelAnterior: string | null; nivelNovo: string; nivelNovoOrdem: number; justificativa: string; mudadoEm: string; mudadoPorCpf: string | null }[];
  avaliacoes: {
    uuid: string;
    avaliadaEm: string;
    populacaoAfetada: number | null;
    unidadesAfetadas: string[];
    profissionaisIndisponiveis: number | null;
    leitosIndisponiveis: number | null;
    medicamentosCriticos: string | null;
    equipamentos: string | null;
    transporte: string | null;
    infraestrutura: string | null;
    nivelImpacto: NivelImpacto;
    observacao: string | null;
  }[];
}

export interface SugestaoImpactoDto {
  populacaoAfetada: number | null;
  microareas: number;
  unidades: { uuid: string; nome: string; situacaoOperacional: string; leitos: number; leitosIndisponiveis: number }[];
  leitosIndisponiveis: number;
}

export interface AvaliacaoImpactoRequestDto {
  populacaoAfetada: number | null;
  unidadeIds: string[];
  profissionaisIndisponiveis: number | null;
  leitosIndisponiveis: number | null;
  medicamentosCriticos: string | null;
  equipamentos: string | null;
  transporte: string | null;
  infraestrutura: string | null;
  nivelImpacto: NivelImpacto;
  observacao: string | null;
}
