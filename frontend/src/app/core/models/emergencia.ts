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
  /** Os planos de contingência ativados (ADR-0157), os ativos primeiro. */
  planos: PlanoAtivoDto[];
}

export interface PlanoAtivoDto {
  ativacaoId: string;
  planoId: string;
  codigo: string;
  nome: string;
  versao: number;
  situacao: 'ATIVA' | 'DESATIVADA';
  ativadaEm: string;
  desativadaEm: string | null;
  cobreNivelAtual: boolean;
  registros: { tipo: 'ATIVADO' | 'NIVEL_ALTERADO' | 'DESATIVADO'; nivelNome: string; nivelCoberto: boolean; autoridade: string | null; justificativa: string; registradoEm: string }[];
  acoesDoNivel: { descricao: string; responsavel: string; prazoHoras: number | null; prazoEm: string | null; unidadeNome: string | null }[];
}

// ── Plano de contingência (E2, ADR-0157) ──

export type SituacaoPlanoContingencia = 'RASCUNHO' | 'VIGENTE' | 'SUBSTITUIDA' | 'REVOGADA';

export const SITUACOES_PLANO: Record<SituacaoPlanoContingencia, { rotulo: string; classe: string }> = {
  RASCUNHO: { rotulo: 'Rascunho', classe: 'info' },
  VIGENTE: { rotulo: 'Vigente', classe: 'ok' },
  SUBSTITUIDA: { rotulo: 'Substituída', classe: 'muted' },
  REVOGADA: { rotulo: 'Revogada', classe: 'muted' },
};

export const TIPOS_REGISTRO_ATIVACAO: Record<PlanoAtivoDto['registros'][number]['tipo'], string> = {
  ATIVADO: 'Ativado',
  NIVEL_ALTERADO: 'Nível alterado',
  DESATIVADO: 'Desativado',
};

export interface PlanoContingenciaDto {
  uuid: string;
  codigo: string;
  versao: number;
  nome: string;
  tipoId: string;
  tipoNome: string;
  objetivo: string;
  vigenciaInicio: string;
  vigenciaFim: string | null;
  situacao: SituacaoPlanoContingencia;
  niveis: { uuid: string; ordem: number; nome: string }[];
  unidades: { uuid: string; nome: string }[];
  criterios: { uuid: string; nivelId: string; nivelNome: string; descricao: string }[];
  acoes: { uuid: string; nivelId: string; nivelNome: string; descricao: string; responsavel: string; prazoHoras: number | null; unidadeId: string | null; unidadeNome: string | null }[];
  recursos: { descricao: string; quantidade: number | null }[];
  contatos: { nome: string; papel: string | null; telefone: string | null }[];
  criadoEm: string;
  publicadoEm: string | null;
  encerradoEm: string | null;
  motivoRevogacao: string | null;
  versoes: { uuid: string; versao: number; situacao: SituacaoPlanoContingencia; publicadoEm: string | null; encerradoEm: string | null }[];
}

export interface PlanoContingenciaRequestDto {
  codigo?: string | null;
  nome: string;
  tipoId: string;
  objetivo: string;
  vigenciaInicio: string;
  vigenciaFim: string | null;
  nivelIds: string[];
  unidadeIds: string[];
  criterios: { nivelId: string; descricao: string }[];
  acoes: { nivelId: string; descricao: string; responsavel: string; prazoHoras: number | null; unidadeId: string | null }[];
  recursos: { descricao: string; quantidade: number | null }[];
  contatos: { nome: string; papel: string | null; telefone: string | null }[];
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
