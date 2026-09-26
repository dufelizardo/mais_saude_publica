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

export interface SecurityStatusResponseDto {
  securityEnabled: boolean;
}
