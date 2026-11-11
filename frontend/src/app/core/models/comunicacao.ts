export type TipoCanal = 'PORTAL' | 'APLICATIVO' | 'EMAIL' | 'SMS' | 'NOTIFICACAO' | 'PAINEL' | 'IMPRESSO' | 'EVENTO' | 'REDE_SOCIAL';
export type PrioridadeComunicado = 'NORMAL' | 'ALTA' | 'EMERGENCIAL';
export type PublicoComunicacao = 'POPULACAO' | 'PROFISSIONAIS';
export type SituacaoComunicado = 'RASCUNHO' | 'PUBLICADO' | 'ENCERRADO';
export type SituacaoConteudo = 'RASCUNHO' | 'EM_REVISAO' | 'APROVADO' | 'PUBLICADO' | 'EXPIRADO' | 'SUBSTITUIDO';
export type SituacaoPublicacao = 'PUBLICADA' | 'ENVIADA' | 'PARCIAL' | 'NAO_ENVIADA';

export const TIPOS_DE_CANAL: Record<TipoCanal, string> = {
  PORTAL: 'Portal',
  APLICATIVO: 'Aplicativo',
  EMAIL: 'E-mail',
  SMS: 'SMS',
  NOTIFICACAO: 'Notificação',
  PAINEL: 'Painel',
  IMPRESSO: 'Impresso',
  EVENTO: 'Evento',
  REDE_SOCIAL: 'Rede social',
};
export const PRIORIDADES: Record<PrioridadeComunicado, string> = { NORMAL: 'Normal', ALTA: 'Alta', EMERGENCIAL: 'Emergencial' };
export const PUBLICOS: Record<PublicoComunicacao, string> = { POPULACAO: 'População', PROFISSIONAIS: 'Profissionais' };
export const SITUACOES_COMUNICADO: Record<SituacaoComunicado, string> = { RASCUNHO: 'Rascunho', PUBLICADO: 'Publicado', ENCERRADO: 'Encerrado' };
export const SITUACOES_CONTEUDO: Record<SituacaoConteudo, string> = {
  RASCUNHO: 'Rascunho',
  EM_REVISAO: 'Em revisão',
  APROVADO: 'Aprovado',
  PUBLICADO: 'Publicado',
  EXPIRADO: 'Expirado',
  SUBSTITUIDO: 'Substituído',
};
export const SITUACOES_PUBLICACAO: Record<SituacaoPublicacao, string> = { PUBLICADA: 'Publicada', ENVIADA: 'Enviada', PARCIAL: 'Parcial', NAO_ENVIADA: 'Não enviada' };

export interface CanalComunicacaoDto {
  uuid: string;
  tipo: TipoCanal;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

export interface CanalComunicacaoRequestDto {
  tipo: TipoCanal;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

export interface PublicacaoDto {
  uuid: string;
  canalNome: string;
  publicadaEm: string;
  situacao: SituacaoPublicacao;
  enviados: number;
  falhas: number;
  detalhe?: string | null;
}

export interface ComunicadoDto {
  uuid: string;
  titulo: string;
  mensagem: string;
  prioridade: PrioridadeComunicado;
  publico: PublicoComunicacao;
  inicio: string;
  fim: string;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  canalIds: string[];
  situacao: SituacaoComunicado;
  /** Publicado e dentro do período. */
  vigente: boolean;
  publicadoEm?: string | null;
  encerradoEm?: string | null;
  motivoEncerramento?: string | null;
  publicacoes: PublicacaoDto[];
  /** Campanha de comunicação de onde saiu (ADR-0174). */
  campanhaId?: string | null;
  campanhaNome?: string | null;
}

export interface ComunicadoRequestDto {
  titulo: string;
  mensagem: string;
  prioridade: PrioridadeComunicado;
  publico: PublicoComunicacao;
  inicio: string;
  fim: string;
  unidadeId?: string | null;
  canalIds: string[];
  campanhaId?: string | null;
}

export interface ConteudoEducativoDto {
  uuid: string;
  grupoId: string;
  versao: number;
  titulo: string;
  resumo: string;
  texto: string;
  tema: string;
  publico: PublicoComunicacao;
  faixaEtaria?: string | null;
  validade: string;
  situacao: SituacaoConteudo;
  motivoDevolucao?: string | null;
  publicadoEm?: string | null;
  publicacoes: PublicacaoDto[];
}

export interface ConteudoEducativoRequestDto {
  titulo: string;
  resumo: string;
  texto: string;
  tema: string;
  publico: PublicoComunicacao;
  faixaEtaria?: string | null;
  validade: string;
}

export interface ComunicadoPublicoDto {
  titulo: string;
  mensagem: string;
  prioridade: PrioridadeComunicado;
  unidadeNome?: string | null;
  inicio: string;
  fim: string;
}

export interface ConteudoPublicoDto {
  titulo: string;
  resumo: string;
  texto: string;
  tema: string;
  faixaEtaria?: string | null;
  versao: number;
  validade: string;
}

// ── Campanhas de comunicação e material educativo (ADR-0174) ───────────────────────────────────────

export type SituacaoCampanhaComunicacao = 'PLANEJADA' | 'EM_ANDAMENTO' | 'ENCERRADA' | 'CANCELADA';
export type PublicoAlvoCampanha = 'POPULACAO_GERAL' | 'GESTANTES' | 'CRIANCAS' | 'ADOLESCENTES' | 'IDOSOS' | 'PROFISSIONAIS_SAUDE' | 'ACS';
export type FormatoMaterial = 'CARTILHA' | 'FOLDER' | 'CARTAZ' | 'INFOGRAFICO' | 'VIDEO' | 'AUDIO' | 'REDE_SOCIAL' | 'OUTRO';

export const SITUACOES_CAMPANHA: Record<SituacaoCampanhaComunicacao, string> = {
  PLANEJADA: 'Planejada',
  EM_ANDAMENTO: 'Em andamento',
  ENCERRADA: 'Encerrada',
  CANCELADA: 'Cancelada',
};
export const PUBLICOS_ALVO: Record<PublicoAlvoCampanha, string> = {
  POPULACAO_GERAL: 'População geral',
  GESTANTES: 'Gestantes',
  CRIANCAS: 'Crianças',
  ADOLESCENTES: 'Adolescentes',
  IDOSOS: 'Idosos',
  PROFISSIONAIS_SAUDE: 'Profissionais de saúde',
  ACS: 'Agentes comunitários de saúde',
};
export const FORMATOS_MATERIAL: Record<FormatoMaterial, string> = {
  CARTILHA: 'Cartilha',
  FOLDER: 'Folder',
  CARTAZ: 'Cartaz',
  INFOGRAFICO: 'Infográfico',
  VIDEO: 'Vídeo',
  AUDIO: 'Áudio',
  REDE_SOCIAL: 'Peça para redes sociais',
  OUTRO: 'Outro',
};

export interface ConteudoDaCampanhaDto {
  grupoId: string;
  uuid: string;
  titulo: string;
  versao: number;
  situacao: SituacaoConteudo;
  validade: string;
  vencido: boolean;
}

/** Educação em saúde (ADR-0195): o detalhe educativo da ação coletiva de Programas. */
export type NivelParticipacao = 'BAIXA' | 'MEDIA' | 'ALTA';

export const NIVEIS_PARTICIPACAO: Record<NivelParticipacao, string> = { BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta' };

export interface AcaoEducativaDto {
  acaoId: string;
  situacao: string;
  tema: string | null;
  campanhaComunicacaoId: string | null;
  campanhaComunicacaoNome: string | null;
  conteudos: { grupoId: string; uuid: string; titulo: string; versao: number; validade: string; vencido: boolean }[];
  materiais: { materialId: string; titulo: string; formato: string; quantidade: number; retirado: boolean }[];
  microareas: { microareaId: string; codigo: string; areaNome: string }[];
  publicoAlcancado: number | null;
  participacao: NivelParticipacao | null;
  avaliacao: string | null;
  avaliadoEm: string | null;
}

export interface AcaoEducativaRequestDto {
  tema: string;
  conteudoGrupoIds: string[];
  materiais: { materialId: string; quantidade: number }[];
  microareaIds: string[];
  campanhaComunicacaoId: string | null;
}

export interface AcaoEducativaDaCampanhaDto {
  acaoId: string;
  descricao: string | null;
  local: string | null;
  unidadeNome: string | null;
  dataPrevista: string;
  realizadaEm: string | null;
  situacao: string;
  tema: string;
  publicoAlcancado: number | null;
  materiaisDistribuidos: number;
  participacao: NivelParticipacao | null;
}

export interface CampanhaComunicacaoDto {
  uuid: string;
  /** C3 (ADR-0195): as ações educativas de Programas ligadas à campanha. */
  acoesEducativas?: AcaoEducativaDaCampanhaDto[];
  nome: string;
  objetivo: string;
  descricao?: string | null;
  inicio: string;
  fim: string;
  situacao: SituacaoCampanhaComunicacao;
  publicoAlvo: PublicoAlvoCampanha;
  faixaEtaria?: string | null;
  unidadeId?: string | null;
  unidadeNome?: string | null;
  territorios: { uuid: string; nome: string }[];
  temas: string[];
  canalIds: string[];
  conteudos: ConteudoDaCampanhaDto[];
  programaId?: string | null;
  programaNome?: string | null;
  campanhaOperacionalId?: string | null;
  campanhaOperacionalNome?: string | null;
  comunicados: { uuid: string; titulo: string; situacao: SituacaoComunicado; inicio: string; fim: string }[];
  alcance: { canal: string; publicacoes: number; enviados: number; falhas: number }[];
  iniciadaEm?: string | null;
  encerradaEm?: string | null;
  balanco?: string | null;
  motivoCancelamento?: string | null;
  /** C6 (ADR-0196): as metas da campanha. */
  metaPessoas?: number | null;
  metaAcoes?: number | null;
}

export interface CampanhaComunicacaoRequestDto {
  nome: string;
  objetivo: string;
  descricao?: string | null;
  inicio: string;
  fim: string;
  publicoAlvo: PublicoAlvoCampanha;
  faixaEtaria?: string | null;
  unidadeId?: string | null;
  territorioIds: string[];
  temas: string[];
  canalIds: string[];
  conteudoGrupoIds: string[];
  metaPessoas?: number | null;
  metaAcoes?: number | null;
}

// ── Avaliação da campanha (ADR-0196) ──

export type PublicoAtingido = 'SIM' | 'PARCIAL' | 'NAO';
export type SituacaoMetaComunicacao = 'ATINGIDA' | 'EM_ANDAMENTO' | 'ABAIXO' | 'SEM_META';

export const PUBLICOS_ATINGIDOS: Record<PublicoAtingido, string> = { SIM: 'Sim', PARCIAL: 'Parcialmente', NAO: 'Não' };
export const SITUACOES_META_COMUNICACAO: Record<SituacaoMetaComunicacao, string> = {
  ATINGIDA: 'Atingida',
  EM_ANDAMENTO: 'Em andamento',
  ABAIXO: 'Abaixo',
  SEM_META: 'Sem meta',
};

export interface MetaComunicacaoDto {
  meta?: number | null;
  valor: number;
  percentual?: number | null;
  situacao: SituacaoMetaComunicacao;
}

export interface AvaliacaoCampanhaDto {
  campanhaId: string;
  campanhaNome: string;
  situacao: SituacaoCampanhaComunicacao;
  inicio: string;
  fim: string;
  canais: { nome: string; tipo: TipoCanal; publicacoes: number; enviados: number; falhas: number; taxaEntrega?: number | null; semContagem: boolean; destaque: boolean }[];
  enviados: number;
  acoes: {
    previstas: number;
    realizadas: number;
    naoRealizadas: number;
    canceladas: number;
    pessoas: number;
    participacaoAlta: number;
    participacaoMedia: number;
    participacaoBaixa: number;
    semAvaliacao: number;
    materiais: { titulo: string; quantidade: number }[];
  };
  territorios: { uuid: string; nome: string; microareas: number; microareasComAcao: number; cobertura?: number | null; moradores: number; pessoas: number; menor: boolean }[];
  semTerritorio: { acoes: number; pessoas: number };
  cobertura?: number | null;
  metaPessoas: MetaComunicacaoDto;
  metaAcoes: MetaComunicacaoDto;
  efetividade?: {
    campanhaOperacional?: string | null;
    metaOperacional?: number | null;
    realizadoOperacional?: number | null;
    percentualOperacional?: number | null;
    programa?: string | null;
    de?: string | null;
    ate?: string | null;
    novosNoPeriodo?: number | null;
    anteriorDe?: string | null;
    anteriorAte?: string | null;
    novosNoPeriodoAnterior?: number | null;
  } | null;
  avaliacao?: {
    publicoAtingido: PublicoAtingido;
    resultados?: string | null;
    conclusoes: string;
    recomendacoes?: string | null;
    pessoasAlcancadas: number;
    acoesRealizadas: number;
    enviados: number;
    cobertura?: number | null;
    situacaoMetaPessoas: SituacaoMetaComunicacao;
    situacaoMetaAcoes: SituacaoMetaComunicacao;
    registradaEm: string;
    atualizadaEm: string;
  } | null;
}

export interface AvaliacaoCampanhaRequestDto {
  publicoAtingido: PublicoAtingido;
  resultados?: string | null;
  conclusoes: string;
  recomendacoes?: string | null;
}

export interface MaterialEducativoDto {
  uuid: string;
  conteudoGrupoId: string;
  formato: FormatoMaterial;
  titulo: string;
  descricao?: string | null;
  link?: string | null;
  registradoEm: string;
  retiradoEm?: string | null;
  motivoRetirada?: string | null;
}

export interface MaterialEducativoRequestDto {
  conteudoGrupoId: string;
  formato: FormatoMaterial;
  titulo: string;
  descricao?: string | null;
  link?: string | null;
}

export interface CampanhaComunicacaoPublicaDto {
  nome: string;
  objetivo: string;
  inicio: string;
  fim: string;
  temas: string[];
  conteudos: { titulo: string; resumo: string; materiais: { formato: FormatoMaterial; titulo: string; link?: string | null }[] }[];
}
