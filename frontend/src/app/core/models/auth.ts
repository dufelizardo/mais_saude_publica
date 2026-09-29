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
}

/** Quem está logado e o profissional de mesmo CPF, quando existe (ADR-0065). */
export interface UsuarioAtualResponseDto {
  cpf: string;
  nome: string;
  profissionalUuid?: string | null;
  profissionalMatricula?: string | null;
  profissionalNome?: string | null;
}

export interface SecurityStatusResponseDto {
  securityEnabled: boolean;
}
