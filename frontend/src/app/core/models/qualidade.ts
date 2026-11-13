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

// ── Não conformidades e planos de ação (ADR-0204) ──

export type OrigemNaoConformidade = 'INCIDENTE' | 'INSPECAO_SANITARIA' | 'AUDITORIA_INTERNA' | 'RECLAMACAO' | 'INDICADOR' | 'OUTRA';
export type GravidadeNaoConformidade = 'MENOR' | 'MAIOR' | 'CRITICA';
export type SituacaoNaoConformidade = 'ABERTA' | 'EM_TRATAMENTO' | 'EM_VERIFICACAO' | 'ENCERRADA' | 'CANCELADA';
export type SituacaoAcaoQualidade = 'PENDENTE' | 'EM_ANDAMENTO' | 'CONCLUIDA' | 'CANCELADA';
export type CategoriaCausa = 'METODO' | 'MAQUINA' | 'MAO_DE_OBRA' | 'MATERIAL' | 'MEDICAO' | 'MEIO_AMBIENTE';

export const ORIGENS_NC: Record<OrigemNaoConformidade, string> = {
  INCIDENTE: 'Incidente de segurança', INSPECAO_SANITARIA: 'Inspeção sanitária', AUDITORIA_INTERNA: 'Auditoria interna', RECLAMACAO: 'Reclamação',
  INDICADOR: 'Indicador fora da meta', OUTRA: 'Outra',
};
export const GRAVIDADES_NC: Record<GravidadeNaoConformidade, string> = { MENOR: 'Menor', MAIOR: 'Maior', CRITICA: 'Crítica' };
export const SITUACOES_NC: Record<SituacaoNaoConformidade, string> = {
  ABERTA: 'Aberta', EM_TRATAMENTO: 'Em tratamento', EM_VERIFICACAO: 'Em verificação', ENCERRADA: 'Encerrada', CANCELADA: 'Cancelada',
};
export const SITUACOES_ACAO: Record<SituacaoAcaoQualidade, string> = { PENDENTE: 'Pendente', EM_ANDAMENTO: 'Em andamento', CONCLUIDA: 'Concluída', CANCELADA: 'Cancelada' };
export const CATEGORIAS_CAUSA: Record<CategoriaCausa, string> = {
  METODO: 'Método', MAQUINA: 'Máquina', MAO_DE_OBRA: 'Mão de obra', MATERIAL: 'Material', MEDICAO: 'Medição', MEIO_AMBIENTE: 'Meio ambiente',
};

export interface AcaoQualidadeDto {
  uuid: string; ciclo: number; oQue: string; porQue: string; responsavelMatricula: string; responsavelNome: string; onde: string | null; prazo: string;
  como: string | null; custo: number | null; situacao: SituacaoAcaoQualidade; atrasada: boolean; evidencia: string | null; concluidaEm: string | null;
  motivoCancelamento: string | null;
}

export interface NaoConformidadeDto {
  uuid: string; numero: string; unidadeId: string; unidadeNome: string; setorId: string | null; setorNome: string | null; origem: OrigemNaoConformidade;
  incidenteId: string | null; incidenteNumero: string | null; irregularidadeId: string | null; irregularidadeDescricao: string | null; descricao: string;
  correcaoImediata: string | null; gravidade: GravidadeNaoConformidade; identificadaEm: string; responsavelMatricula: string; responsavelNome: string;
  analiseCausa: string | null; categoriasCausa: CategoriaCausa[]; situacao: SituacaoNaoConformidade; ciclo: number; verificacaoPrevista: string | null;
  encerradaEm: string | null; motivoCancelamento: string | null; indicador: IndicadorQualidade | null; competencia: string | null;
  checklistNome: string | null; acoesAtrasadas: number; acoes: AcaoQualidadeDto[];
  verificacoes: { ciclo: number; eficaz: boolean; verificadaEm: string; observacao: string | null }[]; registradoEm: string;
}

export interface MinhaAcaoDto { naoConformidadeId: string; numero: string; unidadeNome: string; naoConformidadeDescricao: string; acao: AcaoQualidadeDto }

export interface SugestaoNcDto {
  origem: OrigemNaoConformidade; referenciaId: string; referencia: string; unidadeId: string; unidadeNome: string; descricao: string; data: string;
  indicador: IndicadorQualidade | null; checklistId: string | null;
}

export interface PainelNcDto {
  total: number; emAberto: number; acoesAtrasadas: number; verificadas: number; eficaciaNaPrimeira: number | null;
  porOrigem: { codigo: OrigemNaoConformidade; quantidade: number }[]; porGravidade: { codigo: GravidadeNaoConformidade; quantidade: number }[];
  porSituacao: { codigo: SituacaoNaoConformidade; quantidade: number }[];
}

// ── Indicadores de qualidade e auditoria de protocolos (ADR-0205) ──

export type IndicadorQualidade = 'TAXA_QUEDAS' | 'INCIDENCIA_LESAO_PRESSAO' | 'REINTERNACAO_30_DIAS' | 'EVENTOS_ADVERSOS_GRAVES' | 'TEMPO_ATE_MEDICO'
  | 'ABSENTEISMO' | 'ADESAO_PROTOCOLO';
export type SituacaoIndicador = 'DENTRO' | 'FORA' | 'SEM_META' | 'SEM_BASE';
export type RespostaItem = 'CONFORME' | 'NAO_CONFORME' | 'NAO_SE_APLICA';

export const INDICADORES_QUALIDADE: Record<IndicadorQualidade, string> = {
  TAXA_QUEDAS: 'Taxa de quedas', INCIDENCIA_LESAO_PRESSAO: 'Incidência de lesão por pressão', REINTERNACAO_30_DIAS: 'Reinternação em 30 dias',
  EVENTOS_ADVERSOS_GRAVES: 'Eventos adversos graves', TEMPO_ATE_MEDICO: 'Tempo até o médico', ABSENTEISMO: 'Absenteísmo',
  ADESAO_PROTOCOLO: 'Adesão ao protocolo',
};
export const SITUACOES_INDICADOR: Record<SituacaoIndicador, string> = { DENTRO: 'Dentro da meta', FORA: 'Fora da meta', SEM_META: 'Sem meta', SEM_BASE: 'Sem base' };
export const RESPOSTAS_ITEM: Record<RespostaItem, string> = { CONFORME: 'Conforme', NAO_CONFORME: 'Não conforme', NAO_SE_APLICA: 'Não se aplica' };

export interface LinhaIndicadorDto {
  indicador: IndicadorQualidade; checklistId: string | null; nome: string; medida: string; menorMelhor: boolean; valor: number | null;
  numerador: number | null; denominador: number | null; meta: number | null; situacao: SituacaoIndicador; serie: { competencia: string; valor: number | null }[];
}
export interface PainelQualidadeDto { unidadeId: string | null; abrangencia: string; competencia: string; linhas: LinhaIndicadorDto[] }
export interface MetaQualidadeDto {
  uuid: string; unidadeId: string; unidadeNome: string; indicador: IndicadorQualidade; indicadorNome: string; checklistId: string | null;
  checklistNome: string | null; valor: number; vigenteDesde: string; vigenteAte: string | null;
}
export interface ChecklistDto { uuid: string; codigo: string; nome: string; ativo: boolean; versaoId: string; versao: number; itens: { codigo: string; texto: string }[] }
export interface AuditoriaProtocoloDto {
  uuid: string; unidadeId: string; unidadeNome: string; setorNome: string | null; checklistId: string; checklistNome: string; versao: number;
  dataAuditoria: string; conformes: number; aplicaveis: number; adesao: number; observacao: string | null; respostas: Record<string, RespostaItem>;
  registradoEm: string;
}
