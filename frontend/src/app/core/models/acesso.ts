/** Papéis, permissões e escopo por unidade (ADR-0066, ADR-0067, ADR-0068). */

export type DimensaoPermissao = 'ADMINISTRACAO_DO_SISTEMA' | 'OPERACAO' | 'ACESSO_AO_DADO_DE_SAUDE';

export interface PermissaoResponseDto {
  uuid: string;
  codigo: string;
  descricao: string;
  dimensao: DimensaoPermissao;
}

export interface PapelResponseDto {
  uuid: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
  /** Semeado pelo catálogo; os demais foram criados pela administração. */
  padrao: boolean;
  permissoes: string[];
}

export interface PapelRequestDto {
  codigo: string;
  nome: string;
  descricao?: string | null;
  permissoes: string[];
}

export interface PapelAtualizacaoRequestDto {
  nome: string;
  descricao?: string | null;
  ativo: boolean;
  permissoes: string[];
}

export interface AtribuicaoAcessoResponseDto {
  uuid: string;
  usuarioUuid: string;
  usuarioNome: string;
  usuarioCpf: string;
  papelUuid: string;
  papelCodigo: string;
  papelNome: string;
  /** Nulo = rede inteira. */
  unidadeUuid?: string | null;
  unidadeNome?: string | null;
  inicio?: string | null;
  fim?: string | null;
  concedidoEm: string;
  concedidoPorCpf?: string | null;
  revogadoEm?: string | null;
  revogadoPorCpf?: string | null;
  motivoRevogacao?: string | null;
  vigente: boolean;
}

export interface AtribuicaoAcessoRequestDto {
  usuarioId: string;
  papelId: string;
  unidadeId?: string | null;
  inicio?: string | null;
  fim?: string | null;
}

export interface UsuarioResponseDto {
  uuid: string;
  cpf: string;
  nome: string;
  ativo: boolean;
  bloqueadoAte?: string | null;
  ultimoAcessoEm?: string | null;
  bloqueado: boolean;
}

export interface UsuarioRequestDto {
  cpf: string;
  nome: string;
  senha: string;
}

export interface UsuarioAtualizacaoRequestDto {
  nome: string;
  ativo: boolean;
}

export interface EscopoAcessoResponseDto {
  uuid: string;
  nome: string;
  tipo?: string | null;
  unidadeSuperiorUuid?: string | null;
}
