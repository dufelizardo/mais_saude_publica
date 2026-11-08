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

export interface CampanhaComunicacaoDto {
  uuid: string;
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
