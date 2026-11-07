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
