export type TipoIdentificadorLogin = 'CPF' | 'MATRICULA';

export interface LoginRequestDto {
  tipo: TipoIdentificadorLogin;
  identificador: string;
  senha: string;
  manterConectado: boolean;
}

export interface LoginResponseDto {
  token: string;
  expiraEm: string;
  nome: string;
  cpf: string;
  /** Senha provisória: vai direto para a troca (ADR-0069). */
  trocarSenha?: boolean;
}

export interface TrocaSenhaRequestDto {
  senhaAtual: string;
  novaSenha: string;
  manterConectado: boolean;
}

/** Quem está logado e o profissional de mesmo CPF, quando existe (ADR-0065). */
export interface UsuarioAtualResponseDto {
  cpf: string;
  nome: string;
  profissionalUuid?: string | null;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
  /** Papéis vigentes com o escopo (unidade nula = rede inteira) — ADR-0066. */
  acessos?: AcessoVigente[];
  /** Permissões efetivas (RECURSO.ACAO) — ADR-0066. */
  permissoes?: string[];
  /** Senha provisória pendente de troca (ADR-0069). */
  trocarSenha?: boolean;
}

export interface AcessoVigente {
  papelCodigo: string;
  papelNome: string;
  unidadeUuid?: string | null;
  unidadeNome?: string | null;
}

export interface SecurityStatusResponseDto {
  securityEnabled: boolean;
  /** Exigência de permissão por papel e escopo ligada (ADR-0067). */
  authorizationEnabled?: boolean;
  /** "Esqueci minha senha" por e-mail disponível neste ambiente (ADR-0081). */
  recuperacaoDeSenha?: boolean;
}

/** O que a interface pode mostrar: sem restrição, ou só o que as permissões do usuário liberam (ADR-0068). */
export interface AcessoDaInterface {
  restrito: boolean;
  permissoes: ReadonlySet<string>;
}
