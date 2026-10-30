// ── Emergências e Desastres, fatia E1 (ADR-0156): evento, emergência, nível, território e impacto ──

export type CategoriaTipoEmergencia = 'DESASTRE' | 'SAUDE';
export type FonteEventoEmergencial = 'VIGILANCIA' | 'UNIDADE' | 'DEFESA_CIVIL' | 'GESTOR' | 'PROFISSIONAL' | 'SISTEMA_EXTERNO';
export type SituacaoEventoEmergencial = 'REGISTRADO' | 'MONITORADO' | 'DESCARTADO' | 'CONVERTIDO';
export type SituacaoEmergencia = 'PLANEJADA' | 'ATIVA' | 'EM_DESMOBILIZACAO' | 'ENCERRADA';
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
  perfil: PerfilTipoEmergencia;
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
  /** A coordenação (ADR-0158). */
  acoes: AcaoEmergencialDto[];
  acoesAtrasadas: number;
  mobilizacoes: MobilizacaoDto[];
  capacidades: CapacidadeAtivadaDto[];
  unidadesAfetadas: { uuid: string; nome: string; situacaoOperacional: string }[];
  /** A desmobilização (ADR-0159), quando iniciada, e o que falta para encerrar. */
  desmobilizacao: {
    iniciadaEm: string;
    responsavel: string;
    justificativa: string;
    mobilizacoesConcluidas: number;
    profissionaisLiberados: number;
    capacidadesDesativadas: number;
    planosDesativados: number;
    estruturasDesativadas: number;
  } | null;
  pendencias: string[];
  /** O perfil do tipo, os dados do evento de massa (também na lista) e as estruturas temporárias (ADR-0160). */
  perfil: PerfilTipoEmergencia;
  eventoMassa: EventoMassaDto | null;
  estruturas: EstruturaTemporariaDto[];
  /** O agravo da epidemia e as exposições ambientais (ADR-0161). */
  agravoId: string | null;
  agravoNome: string | null;
  exposicoes: ExposicaoAmbientalDto[];
}

// ── Coordenação (E3, ADR-0158) ──

export type PrioridadeAcaoEmergencial = 'BAIXA' | 'MEDIA' | 'ALTA' | 'URGENTE';
export type SituacaoAcaoEmergencial = 'PENDENTE' | 'EM_EXECUCAO' | 'CONCLUIDA' | 'CANCELADA';
export type TipoMobilizacao = 'PROFISSIONAL' | 'MEDICAMENTO' | 'INSUMO' | 'EQUIPAMENTO' | 'VEICULO' | 'LEITO' | 'ESPACO_FISICO';
export type SituacaoMobilizacao = 'SOLICITADA' | 'EM_ANDAMENTO' | 'CONCLUIDA' | 'CANCELADA';
export type RecursoCapacidade = 'ATENDIMENTOS_DIA' | 'LEITOS' | 'CONSULTORIOS' | 'OUTRO';

export const PRIORIDADES_ACAO: Record<PrioridadeAcaoEmergencial, { rotulo: string; classe: string }> = {
  BAIXA: { rotulo: 'Baixa', classe: 'muted' },
  MEDIA: { rotulo: 'Média', classe: 'info' },
  ALTA: { rotulo: 'Alta', classe: 'warn' },
  URGENTE: { rotulo: 'Urgente', classe: 'alert' },
};

export const SITUACOES_ACAO: Record<SituacaoAcaoEmergencial, { rotulo: string; classe: string }> = {
  PENDENTE: { rotulo: 'Pendente', classe: 'info' },
  EM_EXECUCAO: { rotulo: 'Em execução', classe: 'warn' },
  CONCLUIDA: { rotulo: 'Concluída', classe: 'ok' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export const TIPOS_MOBILIZACAO: Record<TipoMobilizacao, string> = {
  PROFISSIONAL: 'Profissional',
  MEDICAMENTO: 'Medicamento',
  INSUMO: 'Insumo',
  EQUIPAMENTO: 'Equipamento',
  VEICULO: 'Veículo',
  LEITO: 'Leito',
  ESPACO_FISICO: 'Espaço físico',
};

export const SITUACOES_MOBILIZACAO: Record<SituacaoMobilizacao, { rotulo: string; classe: string }> = {
  SOLICITADA: { rotulo: 'Solicitada', classe: 'info' },
  EM_ANDAMENTO: { rotulo: 'Em andamento', classe: 'warn' },
  CONCLUIDA: { rotulo: 'Concluída', classe: 'ok' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'muted' },
};

export const RECURSOS_CAPACIDADE: Record<RecursoCapacidade, string> = {
  ATENDIMENTOS_DIA: 'Atendimentos por dia',
  LEITOS: 'Leitos',
  CONSULTORIOS: 'Consultórios',
  OUTRO: 'Outro',
};

export interface AcaoEmergencialDto {
  uuid: string;
  descricao: string;
  responsavel: string;
  unidadeNome: string | null;
  prioridade: PrioridadeAcaoEmergencial;
  prazo: string | null;
  situacao: SituacaoAcaoEmergencial;
  atrasada: boolean;
  planoCodigo: string | null;
  iniciadaEm: string | null;
  concluidaEm: string | null;
  motivoCancelamento: string | null;
  observacao: string | null;
}

export interface MobilizacaoDto {
  uuid: string;
  tipo: TipoMobilizacao;
  origemNome: string | null;
  destinoId: string;
  destinoNome: string;
  profissionalMatricula: string | null;
  profissionalNome: string | null;
  recurso: string;
  quantidade: number | null;
  responsavel: string | null;
  inicio: string;
  fim: string | null;
  situacao: SituacaoMobilizacao;
  transferenciaId: string | null;
  transferenciaStatus: string | null;
  autorizaEscala: boolean;
  motivoCancelamento: string | null;
}

export interface CapacidadeAtivadaDto {
  uuid: string;
  capacidadeId: string;
  unidadeNome: string;
  recurso: RecursoCapacidade;
  normal: number;
  emergencial: number;
  quantidade: number;
  situacao: 'ATIVA' | 'DESATIVADA';
  inicio: string;
  fim: string | null;
  justificativa: string;
  justificativaFim: string | null;
}

export interface CapacidadeEmergencialDto {
  uuid: string;
  unidadeId: string;
  unidadeNome: string;
  recurso: RecursoCapacidade;
  normal: number;
  emergencial: number;
  ativadaAgora: number;
  leitosAtivos: number | null;
  observacao: string | null;
  ativa: boolean;
  atualizadaEm: string;
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

// ── Relatório pós-evento (E5, ADR-0159) ──

export const SITUACOES_EMERGENCIA: Record<SituacaoEmergencia, { rotulo: string; classe: string }> = {
  PLANEJADA: { rotulo: 'Planejada', classe: 'info' },
  ATIVA: { rotulo: 'Ativa', classe: 'alert' },
  EM_DESMOBILIZACAO: { rotulo: 'Em desmobilização', classe: 'warn' },
  ENCERRADA: { rotulo: 'Encerrada', classe: 'muted' },
};

export interface RelatorioPosEventoDto {
  uuid: string | null;
  emergenciaId: string;
  situacao: 'RASCUNHO' | 'FINALIZADO' | null;
  resumo: string | null;
  problemas: string | null;
  licoes: string | null;
  recomendacoes: { uuid: string; descricao: string; planoCodigo: string | null; planoRevisaoId: string | null; planoRevisaoVersao: number | null }[];
  planosUsados: { codigo: string; nome: string; versao: number }[];
  indicadores: {
    duracaoHoras: number;
    horasAteAtivacao: number;
    nivelMaximo: string;
    mudancasDeNivel: number;
    acoesTotal: number;
    acoesConcluidas: number;
    acoesCanceladas: number;
    acoesComPrazo: number;
    acoesConcluidasNoPrazo: number;
    percentualNoPrazo: number | null;
    mobilizacoes: number;
    profissionaisMobilizados: number;
    mobilizacoesPorTipo: { chave: string; quantidade: number }[];
    capacidadeAtivada: number;
    populacaoAfetada: number | null;
    unidadesAfetadas: number;
    leitosIndisponiveis: number | null;
    estruturas: number;
    atendimentosEstruturas: number;
    remocoesEstruturas: number;
    casosGravesEstruturas: number;
    obitosEstruturas: number;
    publicoEstimado: number | null;
    casosNotificados: number | null;
    casosConfirmados: number | null;
    obitosAgravo: number | null;
    exposicoes: number;
    populacaoExpostaMaxima: number | null;
  };
  criadoEm: string | null;
  atualizadoEm: string | null;
  finalizadoEm: string | null;
}

// ── Eventos especializados (E6a, ADR-0160) ──

export type PerfilTipoEmergencia = 'GERAL' | 'EVENTO_DE_MASSA' | 'EPIDEMIA' | 'DESASTRE_AMBIENTAL';
export type NaturezaEventoMassa = 'ESPORTIVO' | 'SHOW' | 'FESTA_POPULAR' | 'RELIGIOSO' | 'MANIFESTACAO' | 'OUTRO';
export type RiscoEventoMassa = 'BAIXO' | 'MEDIO' | 'ALTO';
export type TipoEstruturaTemporaria = 'POSTO_MEDICO' | 'TENDA_HIDRATACAO' | 'HOSPITAL_DE_CAMPANHA' | 'OUTRO';
export type SituacaoEstruturaTemporaria = 'PLANEJADA' | 'EM_OPERACAO' | 'DESATIVADA';

export const PERFIS_TIPO: Record<PerfilTipoEmergencia, string> = {
  GERAL: 'Geral',
  EVENTO_DE_MASSA: 'Evento de massa',
  EPIDEMIA: 'Epidemia',
  DESASTRE_AMBIENTAL: 'Desastre ambiental',
};

export const NATUREZAS_EVENTO_MASSA: Record<NaturezaEventoMassa, string> = {
  ESPORTIVO: 'Esportivo',
  SHOW: 'Show',
  FESTA_POPULAR: 'Festa popular ou carnaval',
  RELIGIOSO: 'Religioso',
  MANIFESTACAO: 'Manifestação',
  OUTRO: 'Outro',
};

export const RISCOS_EVENTO_MASSA: Record<RiscoEventoMassa, { rotulo: string; classe: string }> = {
  BAIXO: { rotulo: 'Risco baixo', classe: 'ok' },
  MEDIO: { rotulo: 'Risco médio', classe: 'warn' },
  ALTO: { rotulo: 'Risco alto', classe: 'alert' },
};

export const TIPOS_ESTRUTURA: Record<TipoEstruturaTemporaria, string> = {
  POSTO_MEDICO: 'Posto médico',
  TENDA_HIDRATACAO: 'Tenda de hidratação',
  HOSPITAL_DE_CAMPANHA: 'Hospital de campanha',
  OUTRO: 'Outra estrutura',
};

export const SITUACOES_ESTRUTURA: Record<SituacaoEstruturaTemporaria, { rotulo: string; classe: string }> = {
  PLANEJADA: { rotulo: 'Planejada', classe: 'info' },
  EM_OPERACAO: { rotulo: 'Em operação', classe: 'ok' },
  DESATIVADA: { rotulo: 'Desativada', classe: 'muted' },
};

export interface EventoMassaDto {
  natureza: NaturezaEventoMassa;
  local: string;
  inicioPrevisto: string;
  fimPrevisto: string;
  publicoEstimado: number;
  publicoMaximo: number | null;
  organizador: string | null;
  contato: string | null;
  risco: RiscoEventoMassa;
  observacao: string | null;
  atualizadoEm: string;
}

export type EventoMassaRequestDto = Omit<EventoMassaDto, 'atualizadoEm'>;

export interface EstruturaTemporariaDto {
  uuid: string;
  tipo: TipoEstruturaTemporaria;
  nome: string;
  local: string;
  latitude: number | null;
  longitude: number | null;
  unidadeReferenciaId: string;
  unidadeReferenciaNome: string;
  leitos: number;
  atendimentosDia: number | null;
  ambulancias: number;
  horario: string | null;
  situacao: SituacaoEstruturaTemporaria;
  ativadaEm: string | null;
  desativadaEm: string | null;
  motivoDesativacao: string | null;
  atendimentos: number;
  remocoes: number;
  casosGraves: number;
  obitos: number;
  boletins: { uuid: string; data: string; atendimentos: number; remocoes: number; casosGraves: number; obitos: number; observacao: string | null; registradoEm: string }[];
}

export interface EstruturaTemporariaRequestDto {
  tipo: TipoEstruturaTemporaria;
  nome: string;
  local: string;
  latitude: number | null;
  longitude: number | null;
  unidadeReferenciaId: string;
  leitos: number | null;
  atendimentosDia: number | null;
  ambulancias: number | null;
  horario: string | null;
}

export interface BoletimEstruturaRequestDto {
  data: string;
  atendimentos: number;
  remocoes: number | null;
  casosGraves: number | null;
  obitos: number | null;
  observacao: string | null;
}

// ── Epidemia e desastre ambiental (E6b, ADR-0161) ──

export type MeioExposicaoAmbiental = 'AGUA' | 'AR' | 'SOLO' | 'PRODUTO_QUIMICO' | 'OUTRO';

export const MEIOS_EXPOSICAO: Record<MeioExposicaoAmbiental, string> = {
  AGUA: 'Água',
  AR: 'Ar',
  SOLO: 'Solo',
  PRODUTO_QUIMICO: 'Produto químico',
  OUTRO: 'Outro',
};

export interface ExposicaoAmbientalDto {
  uuid: string;
  avaliadaEm: string;
  meio: MeioExposicaoAmbiental;
  agente: string;
  fonte: string | null;
  populacaoExposta: number | null;
  unidades: string[];
  medidas: string | null;
  recomendacao: string | null;
}

export interface ExposicaoAmbientalRequestDto {
  meio: MeioExposicaoAmbiental;
  agente: string;
  fonte: string | null;
  populacaoExposta: number | null;
  unidadeIds: string[];
  medidas: string | null;
  recomendacao: string | null;
}

export interface PainelEpidemiaDto {
  agravoId: string;
  agravoNome: string;
  doSurto: boolean;
  desde: string;
  ate: string;
  naAreaAfetada: boolean;
  notificados: number;
  confirmados: number;
  descartados: number;
  emInvestigacao: number;
  obitos: number;
  semanaAtual: number;
  semanaAnterior: number;
  curva: { ano: number; semana: number; inicio: string; casos: number }[];
}
