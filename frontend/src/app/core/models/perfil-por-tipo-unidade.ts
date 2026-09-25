import { TipoUnidadeDeSaude } from './unidade-saude';

export interface PerfilPorTipoUnidadeRequestDto {
  tipo: TipoUnidadeDeSaude;
  perfilAdministrativoId: string;
}

export interface PerfilPorTipoUnidadeResponseDto {
  uuid: string;
  tipo: TipoUnidadeDeSaude;
  perfilAdministrativoUuid: string;
  perfilAdministrativoCodigo: string;
  perfilAdministrativoNome: string;
}
